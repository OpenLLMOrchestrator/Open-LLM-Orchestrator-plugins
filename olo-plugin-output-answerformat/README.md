# olo-plugin-output-answerformat

Answer format plugin for Open LLM Orchestrator. Formats output as `ANS: "..."`.

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-output-answerformat.jar` and, if `oloZip` ran, the corresponding .olo package.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
