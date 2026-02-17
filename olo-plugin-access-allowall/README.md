# olo-plugin-access-allowall

Allow-all access control plugin for Open LLM Orchestrator. Optional allowKey check.

## Build

```bash
gradle build
```

Optional .olo package:

```bash
gradle oloZip
```

Output: `build/libs/olo-plugin-access-allowall.jar` and, if `oloZip` ran, `build/distributions/olo-plugin-access-allowall-1.0.0.olo`.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
