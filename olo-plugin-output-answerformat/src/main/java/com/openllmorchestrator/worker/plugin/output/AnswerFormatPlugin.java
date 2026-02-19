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
package com.openllmorchestrator.worker.plugin.output;

import com.openllmorchestrator.worker.contract.ContractCompatibility;
import com.openllmorchestrator.worker.contract.PluginContext;
import com.openllmorchestrator.worker.contract.PlannerInputDescriptor;
import com.openllmorchestrator.worker.contract.PluginTypeDescriptor;
import com.openllmorchestrator.worker.contract.PluginTypes;
import com.openllmorchestrator.olo.OloPlugin;
import com.openllmorchestrator.olo.PluginDataPaths;
import com.openllmorchestrator.worker.contract.CapabilityHandler;
import com.openllmorchestrator.worker.contract.CapabilityResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Post-process stage that renders the model output as a single line: ANS: "&lt;response&gt;".
 * Reads "result" or "response" from accumulated output and writes "output" in that format.
 * Prefix can be overridden by env ANSWER_FORMAT_PREFIX or by a file in the plugin data dir:
 * {@code <OLO_PLUGIN_DATA_DIR>/com.openllm.plugin.output.answerformat/templates/prefix.txt}.
 */
@OloPlugin(
    id = "com.openllm.plugin.output.answerformat",
    name = "Answer Format",
    version = "1.0.0",
    description = "Renders model output as ANS: \"<response>\"; reads result or response from accumulated output.",
    capability = { "OUTPUT" },
    inputs = {},
    outputs = { @OloPlugin.Output(name = "output", type = "string", description = "Formatted answer line") }
)
public final class AnswerFormatPlugin implements CapabilityHandler, ContractCompatibility, PlannerInputDescriptor, PluginTypeDescriptor {

    private static final String CONTRACT_VERSION = "0.0.1";
    public static final String NAME = "com.openllmorchestrator.worker.plugin.output.AnswerFormatPlugin";
    private static final String PLUGIN_ID = "com.openllm.plugin.output.answerformat";
    private static final String PREFIX = "ANS: \"";
    private static final String TEMPLATE_RELATIVE_PATH = "templates/prefix.txt";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public CapabilityResult execute(PluginContext context) {
        Map<String, Object> accumulated = context.getAccumulatedOutput();
        String text = null;
        if (accumulated != null) {
            Object result = accumulated.get("result");
            if (result != null && result.toString() != null) {
                text = result.toString().trim();
            }
            if (text == null || text.isEmpty()) {
                Object response = accumulated.get("response");
                if (response != null && response.toString() != null) {
                    text = response.toString().trim();
                }
            }
        }
        if (text == null) {
            text = "";
        }
        String prefix = resolvePrefix();
        String formatted = prefix + escapeQuotes(text) + "\"";
        context.putOutput("output", formatted);
        return CapabilityResult.builder().capabilityName(NAME).data(new HashMap<>(context.getCurrentPluginOutput())).build();
    }

    @Override
    public String getRequiredContractVersion() {
        return CONTRACT_VERSION;
    }

    @Override
    public Set<String> getRequiredInputFieldsForPlanner() {
        return Set.of("result", "response");
    }

    @Override
    public String getPlannerDescription() {
        return "Refinement: format result/response as ANS: \"...\".";
    }

    @Override
    public String getPluginType() {
        return PluginTypes.REFINEMENT;
    }

    /** Resolve prefix: file in plugin data dir (templates/prefix.txt) &gt; env ANSWER_FORMAT_PREFIX &gt; default. */
    private static String resolvePrefix() {
        try {
            Path templatePath = PluginDataPaths.resolve(PLUGIN_ID, TEMPLATE_RELATIVE_PATH);
            if (Files.isRegularFile(templatePath)) {
                String fromFile = Files.readString(templatePath, StandardCharsets.UTF_8).trim();
                if (!fromFile.isEmpty()) return fromFile;
            }
        } catch (Exception ignored) { /* fall through to env/default */ }
        return getEnv("ANSWER_FORMAT_PREFIX", PREFIX);
    }

    private static String getEnv(String key, String defaultValue) {
        String v = System.getenv(key);
        if (v != null && !v.isBlank()) return v.trim();
        return defaultValue;
    }

    private static String escapeQuotes(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
