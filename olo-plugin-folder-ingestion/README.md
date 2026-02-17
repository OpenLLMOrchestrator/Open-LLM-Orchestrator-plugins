# olo-plugin-folder-ingestion

Folder ingestion plugin for Open LLM Orchestrator. Ingests folder contents into tokenizedChunks.

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-folder-ingestion.jar` and, if `oloZip` ran, the corresponding .olo package.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
