# olo-plugin-rag-file-ingestion

RAG file ingestion plugin for Open LLM Orchestrator. Takes file names as input, reads files from a configured shared RAG folder, tokenizes content, and outputs **tokenizedChunks** for the vector DB plugin to store.

## Configuration (environment)

- **OLO_RAG_DATA_DIR** – If set, used as the full path to the RAG file root. All `fileNames` are resolved under this directory.
- **OLO_RAG_SUBFOLDER** – When `OLO_RAG_DATA_DIR` is not set, RAG files are read from `OLO_PLUGIN_DATA_DIR` (or default `olo-data`) plus this subfolder. Default: `rag`.
- **OLO_PLUGIN_DATA_DIR** – Shared plugin data root (used when `OLO_RAG_DATA_DIR` is unset). Default: `olo-data`.

Example: set `OLO_RAG_DATA_DIR=/data/rag` to read from `/data/rag`. Or leave unset to use `olo-data/rag` (or `OLO_PLUGIN_DATA_DIR/rag`).

## Input

- **fileNames** – Array of file names, or a comma-separated string. Files are resolved under the RAG base path (no path traversal).

## Output

- **tokenizedChunks** – List of `{ path, text, index }` for downstream vector DB storage.
- **fileCount** – Number of files read.
- **error** – Set if any file could not be read or path was invalid.

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-rag-file-ingestion-all.jar` and the corresponding .olo package.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (annotation processor)

## License

Apache License 2.0.
