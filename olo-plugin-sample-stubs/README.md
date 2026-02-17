# olo-plugin-sample-stubs

Sample echo plugin and stub implementations for Open LLM Orchestrator: filter, retrieval, model, refinement, condition, evaluation, feedback, learning, dataset-build, train-trigger, model-registry.

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-sample-stubs.jar` and, if `oloZip` ran, the corresponding .olo package.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

## License

Apache License 2.0.
