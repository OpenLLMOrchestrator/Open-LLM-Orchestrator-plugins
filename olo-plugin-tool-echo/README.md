# olo-plugin-tool-echo

Echo tool plugin for Open LLM Orchestrator. Testing and template use.

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-tool-echo.jar` and, if `oloZip` ran, the corresponding .olo package.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
