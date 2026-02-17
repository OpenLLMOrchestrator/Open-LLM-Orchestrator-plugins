# olo-plugin-observability-passthrough

Pass-through observability plugin for Open LLM Orchestrator.

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-observability-passthrough.jar` and, if `oloZip` ran, the corresponding .olo package.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
