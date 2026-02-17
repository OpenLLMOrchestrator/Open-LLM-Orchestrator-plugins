# olo-plugin-llm-mistral

Fixed-model Mistral chat plugin for Open LLM Orchestrator. Depends on olo-plugin-llm-ollama.

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-llm-mistral.jar` and, if `oloZip` ran, the corresponding .olo package.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-plugin-llm-ollama** (when in monorepo) or equivalent base
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
