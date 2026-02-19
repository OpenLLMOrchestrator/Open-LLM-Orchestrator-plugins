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
package com.openllmorchestrator.worker.plugin.guardrail;

import com.openllmorchestrator.worker.contract.ContractCompatibility;
import com.openllmorchestrator.worker.contract.PluginContext;
import com.openllmorchestrator.worker.contract.PlannerInputDescriptor;
import com.openllmorchestrator.worker.contract.PluginTypeDescriptor;
import com.openllmorchestrator.worker.contract.PluginTypes;
import com.openllmorchestrator.olo.OloPlugin;
import com.openllmorchestrator.worker.contract.CapabilityHandler;
import com.openllmorchestrator.worker.contract.CapabilityResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * GUARDRAIL plugin: optional max length and blocklist. Reads question or content from input/accumulated;
 * if length exceeds maxLength (input.maxLength or 10000) or contains blocklist term (input.blocklistWords comma-separated),
 * sets guardrailTriggered and filteredContent.
 */
@OloPlugin(
    id = "com.openllm.plugin.guardrail.simple",
    name = "Simple Guardrail",
    version = "1.0.0",
    description = "Optional max length and blocklist; sets guardrailTriggered and filteredContent when triggered.",
    capability = { "GUARDRAIL" },
    inputs = {
        @OloPlugin.Input(name = "question", type = "string", required = false, description = "Content to check"),
        @OloPlugin.Input(name = "maxLength", type = "integer", required = false, description = "Max allowed length"),
        @OloPlugin.Input(name = "blocklistWords", type = "string", required = false, description = "Comma-separated blocked terms")
    },
    outputs = {
        @OloPlugin.Output(name = "guardrailTriggered", type = "boolean", description = "True if guardrail fired"),
        @OloPlugin.Output(name = "filteredContent", type = "string", description = "Filtered content when triggered")
    }
)
public final class SimpleGuardrailPlugin implements CapabilityHandler, ContractCompatibility, PlannerInputDescriptor, PluginTypeDescriptor {

    private static final String CONTRACT_VERSION = "0.0.1";
    public static final String NAME = "com.openllmorchestrator.worker.plugin.guardrail.SimpleGuardrailPlugin";
    private static final int DEFAULT_MAX_LENGTH = 10000;

    private static int getEnvInt(String key, int defaultValue) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) return defaultValue;
        try {
            int n = Integer.parseInt(v.trim());
            return n > 0 ? n : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String getEnv(String key, String defaultValue) {
        String v = System.getenv(key);
        if (v != null && !v.isBlank()) return v.trim();
        return defaultValue != null ? defaultValue : "";
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public CapabilityResult execute(PluginContext context) {
        Map<String, Object> input = context.getOriginalInput();
        Map<String, Object> accumulated = context.getAccumulatedOutput();
        String content = null;
        if (input != null && input.get("question") != null) {
            content = String.valueOf(input.get("question"));
        }
        if (content == null && accumulated != null && accumulated.get("result") != null) {
            content = String.valueOf(accumulated.get("result"));
        }
        if (content == null) {
            content = "";
        }
        int defaultMax = getEnvInt("GUARDRAIL_MAX_LENGTH", DEFAULT_MAX_LENGTH);
        int maxLen = input != null && input.get("maxLength") instanceof Number
                ? ((Number) input.get("maxLength")).intValue() : defaultMax;
        if (maxLen <= 0) {
            maxLen = defaultMax;
        }
        String blocklistStr = input != null ? (String) input.get("blocklistWords") : null;
        if (blocklistStr == null || blocklistStr.isBlank()) {
            blocklistStr = getEnv("GUARDRAIL_BLOCKLIST_WORDS", "");
        }
        boolean triggered = false;
        String filtered = content;
        if (content.length() > maxLen) {
            triggered = true;
            filtered = content.substring(0, maxLen);
        }
        if (blocklistStr != null && !blocklistStr.isBlank()) {
            String lower = content.toLowerCase();
            for (String word : blocklistStr.split(",")) {
                String w = word.trim().toLowerCase();
                if (!w.isEmpty() && lower.contains(w)) {
                    triggered = true;
                    filtered = filtered.replaceAll("(?i)" + java.util.regex.Pattern.quote(word.trim()), "[REDACTED]");
                }
            }
        }
        context.putOutput("guardrailTriggered", triggered);
        context.putOutput("filteredContent", filtered);
        return CapabilityResult.builder().capabilityName(NAME).data(new HashMap<>(context.getCurrentPluginOutput())).build();
    }

    @Override
    public String getRequiredContractVersion() {
        return CONTRACT_VERSION;
    }

    @Override
    public Set<String> getRequiredInputFieldsForPlanner() {
        return Set.of("question", "result", "maxLength", "blocklistWords");
    }

    @Override
    public String getPlannerDescription() {
        return "Guardrail: max length and optional blocklist; sets guardrailTriggered and filteredContent.";
    }

    @Override
    public String getPluginType() {
        return PluginTypes.GUARDRAIL;
    }
}
