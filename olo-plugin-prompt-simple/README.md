# olo-plugin-prompt-simple

Simple prompt builder plugin for Open LLM Orchestrator. Builds prompts from question, context, and result.

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-prompt-simple.jar` and, if `oloZip` ran, the corresponding .olo package.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
