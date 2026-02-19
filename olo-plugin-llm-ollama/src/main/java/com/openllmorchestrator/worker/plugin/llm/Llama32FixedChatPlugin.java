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
package com.openllmorchestrator.worker.plugin.llm;

import com.openllmorchestrator.olo.OloPlugin;

/** Fixed-model chat plugin for llama3.2. Used in query-all-models ASYNC pipeline. */
@OloPlugin(
    id = "com.openllm.plugin.llm.ollama.llama32fixed",
    name = "Llama3.2 Fixed (Ollama)",
    version = "1.0.0",
    description = "Fixed-model Llama3.2 chat via Ollama; for query-all-models ASYNC pipeline.",
    capability = { "MODEL" },
    inputs = {
        @OloPlugin.Input(name = "messages", type = "array", required = false, description = "Chat messages"),
        @OloPlugin.Input(name = "question", type = "string", required = false, description = "Question")
    },
    outputs = {
        @OloPlugin.Output(name = "result", type = "string", description = "Model response"),
        @OloPlugin.Output(name = "modelLabel", type = "string", description = "Model label for merge")
    }
)
public final class Llama32FixedChatPlugin extends FixedModelChatPlugin {
    public static final String NAME = "Llama32FixedChatPlugin";
    @Override public String name() { return NAME; }
    @Override protected String getModelId() { return "llama3.2:latest"; }
    @Override protected String getModelLabel() { return "llama3.2"; }
}
