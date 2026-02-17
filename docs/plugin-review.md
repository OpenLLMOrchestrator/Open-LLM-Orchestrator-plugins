# Plugin review – functionality and UI validation

This document reviews all plugin projects for: **contract implementation**, **SPI registration**, **@OloPlugin** usage, and **optional sample input** for standalone validation in the planned UI.

## Execution model (summary)

- Every plugin implements **CapabilityHandler**: `CapabilityResult execute(PluginContext context)`.
- Input: `context.getOriginalInput()` → `Map<String, Object>` (keys match plugin’s expected inputs).
- Output: plugin calls `context.putOutput(key, value)` and returns `CapabilityResult` (often with `context.getCurrentPluginOutput()`).
- Discovery: **Java SPI** – each JAR lists its handler class(es) in `META-INF/services/com.openllmorchestrator.worker.contract.CapabilityHandler` (and often `StageHandler`).
- **@OloPlugin** on the handler class supplies metadata for the UI and for `plugin.yaml` (id, name, version, description, category, inputs, outputs, icons, and optionally **sampleInput** / **sampleInputDescription** for validation).

## Per-project review

| Project | Handler class(es) | SPI (CapabilityHandler) | @OloPlugin | Sample input for validation |
|--------|-------------------|--------------------------|------------|-----------------------------|
| **olo-plugin-access-allowall** | AllowAllAccessControlPlugin | ✓ | ✓ | Can add `sampleInput` (e.g. `{"allowKey":"optional"}`) |
| **olo-plugin-caching-memory** | InMemoryCachingPlugin | ✓ | ✓ | Can add (e.g. cacheKey, get/set payload) |
| **olo-plugin-vectordb-retrieval** | VectorStoreRetrievalPlugin | ✓ | ✓ | Can add (question or tokenizedChunks) |
| **olo-plugin-llm-ollama** | Llama32ChatPlugin, Llama32ModelPlugin, Llama32FixedChatPlugin | ✓ (all 3) | ✓ on all 3 | LLM plugins: sampleInput can be `{"question":"What is 2+2?"}`; requires Ollama at runtime |
| **olo-plugin-llm-mistral** | MistralChatPlugin (or similar) | ✓ | ✓ | Same pattern as ollama fixed-model |
| **olo-plugin-llm-phi3** | Phi3ChatPlugin | ✓ | ✓ | Same |
| **olo-plugin-llm-gemma2** | Gemma2_2bChatPlugin | ✓ | ✓ | Same |
| **olo-plugin-llm-qwen2** | Qwen2_1_5bChatPlugin | ✓ | ✓ | Same |
| **olo-plugin-tokenizer-document** | DocumentTokenizerPlugin | ✓ | ✓ | Can add `{"document":"Sample text..."}` |
| **olo-plugin-folder-ingestion** | FolderIngestionPlugin | ✓ | ✓ | Path-based; sampleInput may need a test path or mock |
| **olo-plugin-output-answerformat** | AnswerFormatPlugin | ✓ | ✓ | Can add (e.g. result to format) |
| **olo-plugin-memory-context** | ContextMemoryPlugin | ✓ | ✓ | Can add (key/value for request-scoped memory) |
| **olo-plugin-tool-echo** | EchoToolPlugin | ✓ | ✓ | ✓ **sampleInput** + **sampleInputDescription** set (example) |
| **olo-plugin-guardrail-simple** | SimpleGuardrailPlugin | ✓ | ✓ | Can add (text, maxLength, blocklist) |
| **olo-plugin-prompt-simple** | SimplePromptBuilderPlugin | ✓ | ✓ | Can add (question, context, result) |
| **olo-plugin-observability-passthrough** | PassThroughObservabilityPlugin | ✓ | ✓ | Pass-through; sampleInput optional |
| **olo-plugin-sample-stubs** | SampleEchoPlugin + 11 stubs | ✓ (all 12) | ✓ on all 12 | Stubs can add minimal sampleInput for demos |

## Functional status

- **Contract**: All listed plugins implement `CapabilityHandler` and use `PluginContext` / `CapabilityResult`; they are consistent with the contract.
- **SPI**: Each project’s `META-INF/services/com.openllmorchestrator.worker.contract.CapabilityHandler` lists the exact handler class name(s) for that JAR. Multi-handler projects (e.g. olo-plugin-llm-ollama, olo-plugin-sample-stubs) list all public handler classes.
- **@OloPlugin**: Every handler class that should appear in the catalog is annotated with `@OloPlugin` (at least `id`; name defaults to class name, category to `"CUSTOM"`). Abstract or utility classes (e.g. FixedModelChatPlugin, OllamaModelResolver) are not annotated.
- **Build**: Each project has its own `settings.gradle` / `build.gradle` and builds independently; the root `build.bat` / `build.sh` build and collect all plugins. No root Gradle project.

## Validation in the planned UI

- **Purpose**: Let the user run a plugin **as an individual unit** with sample input and see the output, so they can confirm the plugin works before using it in a pipeline.
- **Mechanism**: The UI reads `plugin.yaml` from the .olo package (or from the built JAR’s generated `olo/plugin.yaml`). If **sampleInput** is present, it is a JSON object. The UI (or a small runner) can:
  1. Instantiate the class named in **className** (from the same JAR).
  2. Build a `PluginContext` whose `getOriginalInput()` returns the parsed **sampleInput** map.
  3. Call `plugin.execute(context)` and display the result (and optionally `context.getCurrentPluginOutput()`).
- **Adding sample input**: In the plugin class, add to `@OloPlugin`:
  - `sampleInput = "{\"question\":\"Your sample question\"}"` (escape quotes in Java).
  - `sampleInputDescription = "Optional note for the validation form."`
- **Example**: **olo-plugin-tool-echo** already sets `sampleInput` and `sampleInputDescription`; its `plugin.yaml` will contain these fields so the UI can offer a “Validate” action with pre-filled input.

## Gaps / notes

- **plugin-contract dependency**: All plugins depend on `com.openllm:plugin-contract`; the Worker repo (or a published artifact) must provide it. Not part of this repo.
- **LLM plugins**: Validation that calls real models (Ollama) requires Ollama to be running and configured; sampleInput is still useful for the UI to pre-fill the form.
- **Folder / path plugins**: FolderIngestionPlugin uses file paths; validation may need a test directory or a mock. sampleInputDescription can explain that.
- **Stub plugins**: Stubs are for demos and contract compliance; adding minimal sampleInput to a few (e.g. StubModelPlugin, SampleEchoPlugin) would demonstrate the validation flow.
