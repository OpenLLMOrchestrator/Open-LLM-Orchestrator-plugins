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

## Environment variables (configuration)

All configuration uses **environment variables with default values**. The user sets the env var to override; if unset, the plugin uses the default.

| Plugin / area | Env var | Default | Description |
|---------------|---------|---------|-------------|
| **Ollama** (all LLM Ollama plugins) | `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama API base URL |
| | `OLLAMA_MODEL` | `llama3.2:latest` | Default model |
| | `OLLAMA_TIMEOUT_SECONDS` | `300` | HTTP timeout (seconds) |
| **Simple Guardrail** | `GUARDRAIL_MAX_LENGTH` | `10000` | Max content length when not in input |
| | `GUARDRAIL_BLOCKLIST_WORDS` | (empty) | Comma-separated blocklist when not in input |
| **Simple Prompt Builder** | `PROMPT_DEFAULT_TEMPLATE` | `Question: {question}\n\nContext:\n{context}` | Default template when input.template is empty |
| **Folder Ingestion** | `FOLDER_INGESTION_DEFAULT_EXTENSIONS` | `.txt,.md,.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.csv,.odt,.ods,.odp,.rtf,.html,.htm,.xml,.json` | Default file extensions when input.fileExtensions is empty (common doc formats) |
| **Answer Format** | `ANSWER_FORMAT_PREFIX` | `ANS: "` | Prefix for formatted output line (or use template file in plugin data dir) |
| **All plugins (shared)** | `OLO_PLUGIN_DATA_DIR` | `olo-data` | Root directory for per-plugin data; in container set e.g. `/data/olo` |

Other plugins take configuration from input only or have no backend settings.

## Common plugin data directory (container / host)

A **single root directory** is shared by all plugins and can be set with **`OLO_PLUGIN_DATA_DIR`** (default: `olo-data`, relative to process working directory). In a container, set it to e.g. **`/data/olo`** so that all plugin data lives under a known path and the runtime can mount volumes or ensure file availability there.

- **Per-plugin subfolder:** Each plugin has a dedicated subfolder: `<OLO_PLUGIN_DATA_DIR>/<pluginId>/`. The plugin id is from `@OloPlugin` (e.g. `com.openllm.plugin.folder.ingestion`). So folder ingestion uses `<base>/com.openllm.plugin.folder.ingestion/`, answer format uses `<base>/com.openllm.plugin.output.answerformat/`, etc.
- **Relative paths:** Plugins that accept relative paths (e.g. folder path for RAG uploads, template paths for formatting) resolve them against their plugin subfolder. This way:
  - **Upload / RAG:** The runtime can place uploaded files under the plugin’s subfolder (e.g. `uploads/`) and the plugin reads from there; file availability is guaranteed at that location.
  - **Templates:** Response-format or prompt templates can be stored under the plugin’s subfolder (e.g. `templates/prefix.txt`, `templates/response.mustache`) and the plugin loads them from there.
- **API:** Use **`PluginDataPaths`** (in `olo-annotations`) in plugin code:
  - `PluginDataPaths.getBaseDir()` – root path
  - `PluginDataPaths.getPluginDir(pluginId)` – plugin’s subfolder
  - `PluginDataPaths.resolve(pluginId, relativePath)` – resolve e.g. `"uploads"` or `"templates/prefix.txt"` under the plugin dir
  - `PluginDataPaths.ensurePluginDirExists(pluginId)` – create the plugin dir if needed

**Plugins using the shared data dir today:** Folder Ingestion (relative `folderPath` is resolved under its plugin dir); Answer Format (optional `templates/prefix.txt` in its plugin dir overrides the default prefix).

## Gaps / notes

- **plugin-contract dependency**: All plugins depend on `com.openllm:plugin-contract`; the Worker repo (or a published artifact) must provide it. Not part of this repo.
- **LLM plugins**: Validation that calls real models (Ollama) requires Ollama to be running and configured; sampleInput is still useful for the UI to pre-fill the form.
- **Folder / path plugins**: FolderIngestionPlugin uses file paths; validation may need a test directory or a mock. sampleInputDescription can explain that.
- **Stub plugins**: Stubs are for demos and contract compliance; adding minimal sampleInput to a few (e.g. StubModelPlugin, SampleEchoPlugin) would demonstrate the validation flow.
