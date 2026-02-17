# olo-processor

Annotation processor for Open LLM Orchestrator plugins. Scans `@OloPlugin` on plugin classes and generates **`olo/plugin.yaml`** at compile time (used by .olo packaging and the UI).

## Behavior

- Discovers all types annotated with `@OloPlugin` in the compiled project.
- Writes a single **`plugin:`** block or a **`plugins:`** array into `build/classes/java/main/olo/plugin.yaml`.
- Fills id, name, version, description, category, className, inputs, outputs, and icon paths (class-based names + default fallbacks).

## Dependencies

- **olo-annotations** (compile): from project `:olo-annotations` in monorepo, or `com.openllm:olo-annotations` from Maven.

## Build

From this directory:

```bash
gradle build
gradle publishToMavenLocal
```

When in the monorepo, publish **olo-annotations** first:  
`cd ../olo-annotations && gradle publishToMavenLocal && cd ../olo-processor && gradle build`

## Artifact

- **Group:** `com.openllm`
- **Artifact:** `olo-processor`
- **Version:** `0.0.1` (or `oloVersion` in `gradle.properties`)

Used as `annotationProcessor` by plugin projects; not a runtime dependency.

## License

Apache License 2.0.
