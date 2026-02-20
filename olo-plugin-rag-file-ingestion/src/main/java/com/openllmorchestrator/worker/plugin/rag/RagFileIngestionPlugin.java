/*
 * Copyright 2026 Open LLM Orchestrator contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.openllmorchestrator.worker.plugin.rag;

import com.openllmorchestrator.olo.OloPlugin;
import com.openllmorchestrator.olo.PluginDataPaths;
import com.openllmorchestrator.worker.contract.CapabilityHandler;
import com.openllmorchestrator.worker.contract.CapabilityResult;
import com.openllmorchestrator.worker.contract.ContractCompatibility;
import com.openllmorchestrator.worker.contract.PlannerInputDescriptor;
import com.openllmorchestrator.worker.contract.PluginContext;
import com.openllmorchestrator.worker.contract.PluginTypeDescriptor;
import com.openllmorchestrator.worker.contract.PluginTypes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RAG file ingestion: takes file names as input, reads files from a shared RAG folder
 * (configured via environment), tokenizes content, and outputs tokenizedChunks for the
 * vector DB plugin to store.
 * <p>
 * Read location:
 * <ul>
 *   <li><b>OLO_RAG_DATA_DIR</b> – if set, used as the full path to the RAG file root (no subfolder).</li>
 *   <li>If unset: <b>OLO_PLUGIN_DATA_DIR</b> (or default "olo-data") + subfolder from <b>OLO_RAG_SUBFOLDER</b> (default "rag").</li>
 * </ul>
 * Input: <b>fileNames</b> – array of file names, or comma-separated string. Files are resolved under the RAG path.
 * Output: <b>tokenizedChunks</b> – list of { path, text, index } for downstream vector DB storage.
 */
@OloPlugin(
    id = "com.openllm.plugin.rag.file.ingestion",
    name = "RAG File Ingestion",
    version = "1.0.0",
    description = "Reads named files from the configured RAG folder (env OLO_RAG_DATA_DIR or shared/rag), tokenizes, and outputs tokenizedChunks for vector DB.",
    capability = { "INGESTION" },
    inputs = {
        @OloPlugin.Input(name = "fileNames", type = "array", required = true, description = "File names (or comma-separated string) under the RAG folder")
    },
    outputs = { @OloPlugin.Output(name = "tokenizedChunks", type = "array", description = "Chunks from ingested files for vector DB") }
)
public final class RagFileIngestionPlugin implements CapabilityHandler, ContractCompatibility, PlannerInputDescriptor, PluginTypeDescriptor {

    /** Environment variable for the RAG data root. If set, files are read directly from this path. */
    public static final String ENV_RAG_DATA_DIR = "OLO_RAG_DATA_DIR";
    /** When OLO_RAG_DATA_DIR is not set, subfolder under OLO_PLUGIN_DATA_DIR (default "rag"). Override with this env. */
    public static final String ENV_RAG_SUBFOLDER = "OLO_RAG_SUBFOLDER";

    private static final String CONTRACT_VERSION = "0.0.1";
    public static final String NAME = "com.openllmorchestrator.worker.plugin.rag.RagFileIngestionPlugin";
    private static final String DEFAULT_RAG_SUBFOLDER = "rag";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public CapabilityResult execute(PluginContext context) {
        Map<String, Object> input = context.getOriginalInput();
        Object fileNamesObj = input != null ? input.get("fileNames") : null;
        List<String> fileNames = parseFileNames(fileNamesObj);
        if (fileNames.isEmpty()) {
            context.putOutput("error", "input.fileNames is required (array or comma-separated string)");
            context.putOutput("tokenizedChunks", List.<Map<String, Object>>of());
            return CapabilityResult.builder().capabilityName(NAME).data(new HashMap<>(context.getCurrentPluginOutput())).build();
        }

        Path ragBase = getRagBasePath();
        List<Map<String, Object>> chunks = new ArrayList<>();
        StringBuilder errorMsg = new StringBuilder();
        int index = 0;
        for (String fileName : fileNames) {
            if (fileName == null || fileName.isBlank()) continue;
            String trimmed = fileName.trim();
            if (trimmed.isEmpty() || trimmed.contains("..")) {
                continue;
            }
            Path file = ragBase.resolve(trimmed).normalize();
            if (!file.startsWith(ragBase)) {
                errorMsg.append("Path escape: ").append(trimmed).append("; ");
                continue;
            }
            try {
                if (!Files.isRegularFile(file)) {
                    errorMsg.append("Not a file or missing: ").append(trimmed).append("; ");
                    continue;
                }
                String text = Files.readString(file, StandardCharsets.UTF_8);
                Map<String, Object> chunk = new HashMap<>();
                chunk.put("path", trimmed);
                chunk.put("text", text);
                chunk.put("index", index++);
                chunks.add(chunk);
            } catch (IOException e) {
                errorMsg.append("Read failed ").append(trimmed).append(": ").append(e.getMessage()).append("; ");
            }
        }
        if (errorMsg.length() > 0) {
            context.putOutput("error", errorMsg.toString());
        }
        context.putOutput("tokenizedChunks", chunks);
        context.putOutput("fileCount", chunks.size());
        return CapabilityResult.builder().capabilityName(NAME).data(new HashMap<>(context.getCurrentPluginOutput())).build();
    }

    @Override
    public String getRequiredContractVersion() {
        return CONTRACT_VERSION;
    }

    @Override
    public Set<String> getRequiredInputFieldsForPlanner() {
        return Set.of("fileNames");
    }

    @Override
    public String getPlannerDescription() {
        return "RAG: read named files from configured RAG folder, tokenize, output tokenizedChunks for vector DB.";
    }

    @Override
    public String getPluginType() {
        return PluginTypes.FILTER;
    }

    /**
     * Resolves the base path for RAG files from environment.
     * OLO_RAG_DATA_DIR = full path; else OLO_PLUGIN_DATA_DIR (or default) + OLO_RAG_SUBFOLDER (default "rag").
     */
    static Path getRagBasePath() {
        String ragDir = System.getenv(ENV_RAG_DATA_DIR);
        if (ragDir != null && !ragDir.isBlank()) {
            return Paths.get(ragDir.trim()).normalize().toAbsolutePath();
        }
        String subfolder = System.getenv(ENV_RAG_SUBFOLDER);
        if (subfolder == null || subfolder.isBlank()) {
            subfolder = DEFAULT_RAG_SUBFOLDER;
        } else {
            subfolder = subfolder.trim().replace('\\', '/');
            if (subfolder.contains("..") || subfolder.startsWith("/")) {
                subfolder = DEFAULT_RAG_SUBFOLDER;
            }
        }
        return PluginDataPaths.getBaseDir().resolve(subfolder).normalize().toAbsolutePath();
    }

    private static List<String> parseFileNames(Object fileNamesObj) {
        if (fileNamesObj == null) return List.of();
        if (fileNamesObj instanceof List<?> list) {
            return list.stream()
                .filter(e -> e != null)
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        }
        if (fileNamesObj instanceof String s && !s.isBlank()) {
            return Stream.of(s.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList());
        }
        return List.of();
    }
}
