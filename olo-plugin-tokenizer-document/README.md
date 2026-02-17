# olo-plugin-tokenizer-document

Document tokenizer plugin for Open LLM Orchestrator. Produces chunks from documents.

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-tokenizer-document.jar` and, if `oloZip` ran, the corresponding .olo package.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
