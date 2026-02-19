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
package com.openllmorchestrator.worker.plugin.folder;

import com.openllmorchestrator.worker.contract.ContractCompatibility;
import com.openllmorchestrator.worker.contract.PluginContext;
import com.openllmorchestrator.worker.contract.PlannerInputDescriptor;
import com.openllmorchestrator.worker.contract.PluginTypeDescriptor;
import com.openllmorchestrator.worker.contract.PluginTypes;
import com.openllmorchestrator.olo.OloPlugin;
import com.openllmorchestrator.olo.PluginDataPaths;
import com.openllmorchestrator.worker.contract.CapabilityHandler;
import com.openllmorchestrator.worker.contract.CapabilityResult;

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
 * Reads all files from a folder (optionally filtered by extension) and outputs
 * them as tokenizedChunks for the vector DB plugin to store.
 * <p>
 * Input: "folderPath" (required). When relative, resolved against the plugin data dir
 * (env OLO_PLUGIN_DATA_DIR / plugin id) so uploads/RAG files are at a known location in container. Optional "fileExtensions", "recursive".
 */
@OloPlugin(
    id = "com.openllm.plugin.folder.ingestion",
    name = "Folder Ingestion",
    version = "1.0.0",
    description = "Reads files from a folder and outputs tokenizedChunks for vector DB storage.",
    capability = { "INGESTION" },
    inputs = {
        @OloPlugin.Input(name = "folderPath", type = "string", required = true, description = "Path to folder"),
        @OloPlugin.Input(name = "fileExtensions", type = "string", required = false, description = "Comma-separated extensions e.g. .txt,.md,.pdf,.doc,.docx,.csv (default: common doc formats)"),
        @OloPlugin.Input(name = "recursive", type = "boolean", required = false, description = "Include subdirectories")
    },
    outputs = { @OloPlugin.Output(name = "tokenizedChunks", type = "array", description = "Chunks from ingested files") }
)
public final class FolderIngestionPlugin implements CapabilityHandler, ContractCompatibility, PlannerInputDescriptor, PluginTypeDescriptor {

    private static final String CONTRACT_VERSION = "0.0.1";
    public static final String NAME = "com.openllmorchestrator.worker.plugin.folder.FolderIngestionPlugin";
    /** Plugin id for resolving relative folderPath against shared plugin data dir. */
    private static final String PLUGIN_ID = "com.openllm.plugin.folder.ingestion";
    /** Default doc formats: text, markdown, PDF, Office (doc, docx, ppt, pptx, xls, xlsx), CSV, OpenDocument, RTF, web. Binary formats (e.g. pdf, doc) are read as UTF-8; for proper text extraction a prior conversion step or dedicated library may be needed. */
    private static final String DEFAULT_EXTENSIONS = ".txt,.md,.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.csv,.odt,.ods,.odp,.rtf,.html,.htm,.xml,.json";

    private static Set<String> getDefaultExtensionSet() {
        String v = System.getenv("FOLDER_INGESTION_DEFAULT_EXTENSIONS");
        if (v != null && !v.isBlank()) {
            return parseExtensions(v.trim());
        }
        return parseExtensions(DEFAULT_EXTENSIONS);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public CapabilityResult execute(PluginContext context) {
        Map<String, Object> input = context.getOriginalInput();
        String folderPath = input != null ? (String) input.get("folderPath") : null;
        if (folderPath == null || folderPath.isBlank()) {
            context.putOutput("error", "input.folderPath is required");
            context.putOutput("tokenizedChunks", List.<Map<String, Object>>of());
            return CapabilityResult.builder().capabilityName(NAME).data(new HashMap<>(context.getCurrentPluginOutput())).build();
        }

        Set<String> extensions = parseExtensions(input != null ? (String) input.get("fileExtensions") : null);
        boolean recursive = input != null && Boolean.TRUE.equals(input.get("recursive"));

        Path base;
        if (Paths.get(folderPath).isAbsolute()) {
            base = Paths.get(folderPath).normalize();
        } else {
            base = PluginDataPaths.resolve(PLUGIN_ID, folderPath).toAbsolutePath().normalize();
        }

        List<Map<String, Object>> chunks = new ArrayList<>();
        try {
            List<Path> files = listFiles(base, recursive, extensions);
            for (int i = 0; i < files.size(); i++) {
                Path file = files.get(i);
                String relativePath = base.relativize(file).toString();
                String text = Files.readString(file, StandardCharsets.UTF_8);
                Map<String, Object> chunk = new HashMap<>();
                chunk.put("path", relativePath);
                chunk.put("text", text);
                chunk.put("index", i);
                chunks.add(chunk);
            }
        } catch (IOException e) {
            context.putOutput("error", "Failed to read folder: " + e.getMessage());
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
    public java.util.Set<String> getRequiredInputFieldsForPlanner() {
        return Set.of("folderPath", "fileExtensions", "recursive");
    }

    @Override
    public String getPlannerDescription() {
        return "Filter: ingest folder files into tokenizedChunks for vector store.";
    }

    @Override
    public String getPluginType() {
        return PluginTypes.FILTER;
    }

    private static Set<String> parseExtensions(String fileExtensions) {
        if (fileExtensions == null || fileExtensions.isBlank()) {
            return getDefaultExtensionSet();
        }
        return Stream.of(fileExtensions.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith(".") ? s : "." + s)
                .collect(Collectors.toSet());
    }

    private static List<Path> listFiles(Path base, boolean recursive, Set<String> extensions) throws IOException {
        if (!Files.isDirectory(base)) {
            return List.of();
        }
        List<Path> out = new ArrayList<>();
        if (recursive) {
            try (Stream<Path> walk = Files.walk(base)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> extensions.stream().anyMatch(ext -> p.toString().toLowerCase().endsWith(ext)))
                        .forEach(out::add);
            }
        } else {
            try (Stream<Path> list = Files.list(base)) {
                list.filter(Files::isRegularFile)
                        .filter(p -> extensions.stream().anyMatch(ext -> p.toString().toLowerCase().endsWith(ext)))
                        .forEach(out::add);
            }
        }
        out.sort(Path::compareTo);
        return out;
    }
}
