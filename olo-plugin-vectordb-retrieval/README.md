# olo-plugin-vectordb-retrieval

Vector store plugin for Open LLM Orchestrator. Store or retrieve chunks (vector DB retrieval).

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-vectordb-retrieval.jar` and `build/distributions/olo-plugin-vectordb-retrieval-1.0.0.olo`.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
