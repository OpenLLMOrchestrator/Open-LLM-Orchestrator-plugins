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

/** Fixed-model chat plugin for phi3. Used in query-all-models ASYNC pipeline. */
@OloPlugin(
    id = "com.openllm.plugin.llm.phi3",
    name = "Phi3 (Ollama)",
    version = "1.0.0",
    description = "Fixed-model Phi3 chat via Ollama; for query-all-models ASYNC pipeline.",
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
public final class Phi3ChatPlugin extends FixedModelChatPlugin {
    public static final String NAME = "Phi3ChatPlugin";
    @Override public String name() { return NAME; }
    @Override protected String getModelId() { return "phi3:latest"; }
    @Override protected String getModelLabel() { return "phi3"; }
}
