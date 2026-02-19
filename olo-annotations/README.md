# olo-annotations

Annotations and constants for Open LLM Orchestrator plugins. Used by plugin modules to declare metadata (`@OloPlugin`) for the UI designer and for packaging.

## Contents

- **`@OloPlugin`** – Plugin descriptor: id, name, version, description, capability (multi-valued), inputs, outputs, optional icon paths.
- **`PackageFormat`** – Constants for .olo package layout (icon suffixes, default asset names, path helpers).
- **`PluginDataPaths`** – Shared data directory for plugins: env `OLO_PLUGIN_DATA_DIR` (default `olo-data`), per-plugin subfolder by plugin id, and helpers to resolve relative paths (uploads, templates) so file availability is guaranteed in container.

No runtime dependencies. Publish to Maven for use by **olo-processor** and all **olo-plugin-*** projects.

## Build

```bash
gradle build
gradle publishToMavenLocal
```

## Publish

- **Maven local:** `gradle publishToMavenLocal`
- **Remote:** Set `MAVEN_REPO_URL`, `GITHUB_ACTOR`, `GITHUB_TOKEN`, then `gradle publish`

## Artifact

- **Group:** `com.openllm`
- **Artifact:** `olo-annotations`
- **Version:** `0.0.1` (or `oloVersion` in `gradle.properties`)

## License

Apache License 2.0.
