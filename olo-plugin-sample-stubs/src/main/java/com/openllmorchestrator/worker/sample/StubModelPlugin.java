/*
 * Copyright 2026 Open LLM Orchestrator contributors.
 */
package com.openllmorchestrator.worker.sample;

import com.openllmorchestrator.worker.contract.ContractCompatibility;
import com.openllmorchestrator.worker.contract.PlannerInputDescriptor;
import com.openllmorchestrator.worker.contract.PluginContext;
import com.openllmorchestrator.worker.contract.PluginTypeDescriptor;
import com.openllmorchestrator.worker.contract.PluginTypes;
import com.openllmorchestrator.olo.OloPlugin;
import com.openllmorchestrator.worker.contract.CapabilityHandler;
import com.openllmorchestrator.worker.contract.CapabilityResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Stub model plugin: echoes question as response. No Ollama. For demos and contract-only plugins module. */
@OloPlugin(
    id = "com.openllm.plugin.sample.stub.model",
    name = "Stub Model",
    version = "1.0.0",
    description = "Stub model: echo question as response; no LLM. For demos.",
    category = "MODEL",
    inputs = {
        @OloPlugin.Input(name = "question", type = "string", required = false, description = "User question"),
        @OloPlugin.Input(name = "messages", type = "array", required = false, description = "Chat messages")
    },
    outputs = {
        @OloPlugin.Output(name = "result", type = "string", description = "Stub response"),
        @OloPlugin.Output(name = "response", type = "string", description = "Alias for result")
    },
    sampleInput = "{\"question\":\"What is 2+2?\"}",
    sampleInputDescription = "Provide a question; stub echoes a response without calling an LLM."
)
public final class StubModelPlugin implements CapabilityHandler, ContractCompatibility, PlannerInputDescriptor, PluginTypeDescriptor {

    private static final String CONTRACT_VERSION = "0.0.1";
    public static final String NAME = "com.openllmorchestrator.worker.sample.StubModelPlugin";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public CapabilityResult execute(PluginContext context) {
        Map<String, Object> input = context.getOriginalInput();
        String question = (String) input.get("question");
        if (question == null || question.isBlank()) {
            question = "(no question)";
        }
        String response = "Stub response to: " + question;
        context.putOutput("response", response);
        context.putOutput("result", response);
        return CapabilityResult.builder().capabilityName(NAME).output(new HashMap<>(context.getCurrentPluginOutput())).build();
    }

    @Override
    public String getRequiredContractVersion() {
        return CONTRACT_VERSION;
    }

    @Override
    public Set<String> getRequiredInputFieldsForPlanner() {
        return Set.of("question", "messages", "modelId", "retrievedChunks");
    }

    @Override
    public String getPlannerDescription() {
        return "Model (stub): echo question as response; no LLM.";
    }

    @Override
    public String getPluginType() {
        return PluginTypes.MODEL;
    }
}
