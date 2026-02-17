# olo-plugin-caching-memory

In-memory cache plugin for Open LLM Orchestrator. Get/set by cacheKey.

## Build

```bash
gradle build
```

Optional .olo package:

```bash
gradle oloZip
```

Output: `build/libs/olo-plugin-caching-memory.jar` and, if `oloZip` ran, `build/distributions/olo-plugin-caching-memory-1.0.0.olo`.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
