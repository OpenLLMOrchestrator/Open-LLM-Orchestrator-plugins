# olo-plugin-guardrail-simple

Simple guardrail plugin for Open LLM Orchestrator. Max length and optional blocklist.

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-guardrail-simple.jar` and, if `oloZip` ran, the corresponding .olo package.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
