# Open LLM Orchestrator – Plugins

Each plugin is a **separate project** (one plugin per project), named `olo-plugin-<pluginType>-<pluginName>`. There is **no root Gradle project**; building is done only via **`build.bat`** (Windows) or **`build.sh`** (Unix/macOS). Projects are standalone and can be moved to different repos and released independently.

## Prerequisites

- **Java 21**
- **Gradle** on PATH (or add a `gradlew` in each project directory)
- **Plugin contract**: if [Open-LLM-Orchestrator-Worker](https://github.com/your-org/Open-LLM-Orchestrator-Worker) is in a sibling directory (`../Open-LLM-Orchestrator-Worker`), the build script will publish its `plugin-contract` to Maven local. Otherwise publish from the Worker repo: `gradle :plugin-contract:publishToMavenLocal`.

## Layout

```
Open-LLM-Orchestrator-plugins/
├── build.bat                 # Build all (Windows): publish OLO + ollama, build plugins → build/
├── build.sh                  # Build all (Unix/macOS), same logic
├── gradle.properties         # Optional: oloFromMaven, oloAnnotationsVersion, oloProcessorVersion
├── build/
│   ├── plugins/              # All plugin JARs
│   ├── olo/                  # .olo packages (one per plugin project)
│   └── Open-LLM-Orchestrator-plugins-<version>.zip   # All .olo binaries (for GitHub Releases)
├── olo-annotations/          # Shared @OloPlugin library (standalone project)
├── olo-processor/            # Annotation processor (plugin.yaml) (standalone project)
└── olo-plugin-<type>-<name>/ # One project per plugin
    ├── settings.gradle       # rootProject.name (for standalone use)
    ├── build.gradle
    └── src/...
```

## Projects (one plugin each)

| Project | Description |
|--------|-------------|
| `olo-plugin-access-allowall` | Allow-all access control (optional allowKey check). |
| `olo-plugin-caching-memory` | In-memory get/set by cacheKey. |
| `olo-plugin-vectordb-retrieval` | Vector store: store or retrieve chunks. (Has @OloPlugin + oloZip.) |
| `olo-plugin-llm-ollama` | Ollama core: chat + RAG + Llama32 fixed (OllamaModelResolver, Llama32ChatPlugin, Llama32ModelPlugin, Llama32FixedChatPlugin). |
| `olo-plugin-llm-mistral` | Fixed-model Mistral chat (depends on olo-plugin-llm-ollama). |
| `olo-plugin-llm-phi3` | Fixed-model Phi3 chat (depends on olo-plugin-llm-ollama). |
| `olo-plugin-llm-gemma2` | Fixed-model Gemma2:2b chat (depends on olo-plugin-llm-ollama). |
| `olo-plugin-llm-qwen2` | Fixed-model Qwen2:1.5b chat (depends on olo-plugin-llm-ollama). |
| `olo-plugin-tokenizer-document` | Document tokenizer → chunks. |
| `olo-plugin-folder-ingestion` | Folder ingestion → tokenizedChunks. |
| `olo-plugin-rag-file-ingestion` | RAG: read named files from env-configured folder (OLO_RAG_DATA_DIR or shared/rag), tokenize → tokenizedChunks for vector DB. |
| `olo-plugin-output-answerformat` | Answer format: ANS: "...". |
| `olo-plugin-memory-context` | Request-scoped key-value memory. |
| `olo-plugin-tool-echo` | Echo tool (testing/template). |
| `olo-plugin-guardrail-simple` | Max length and optional blocklist. |
| `olo-plugin-prompt-simple` | Simple prompt builder (question, context, result). |
| `olo-plugin-observability-passthrough` | Pass-through observability. |
| `olo-plugin-sample-stubs` | Sample echo + stub plugins (filter, retrieval, model, refinement, evaluation, feedback, learning, dataset-build, train-trigger, model-registry, condition). |

## Build

**Build everything and collect outputs into `build/`** (no root Gradle; script-only):

- **Windows:** `build.bat`
- **Unix/macOS:** `./build.sh` (or `sh build.sh`)

The script (1) **removes the root `build/` folder**, (2) publishes plugin-contract from the Worker if present, (3) builds and publishes olo-annotations and olo-processor to Maven local, (4) builds and publishes olo-plugin-llm-ollama and collects its JAR and .olo, (5) builds each plugin project and copies JARs and .olo into `build/plugins/` and `build/olo/`, (6) creates **`build/Open-LLM-Orchestrator-plugins-<version>.zip`** containing all `.olo` binaries for release.

Optional: set **`RELEASE_VERSION`** (e.g. `1.0.0`) before running to control the zip name; default is `1.0.0`.

Outputs:

- **`build/plugins/`** – JAR for each plugin (e.g. `olo-plugin-access-allowall.jar`).
- **`build/olo/`** – `.olo` packages for projects that define `oloZip` (e.g. `olo-plugin-vectordb-retrieval-1.0.0.olo`).
- **`build/Open-LLM-Orchestrator-plugins-<version>.zip`** – All .olo packages in one archive; upload this as the **binary** asset for a [GitHub Release](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository).

**Build notes:** The script runs `clean build` (and `clean publishToMavenLocal` for ollama) so each project's output is fresh. Only artifacts whose names start with the project name (e.g. `olo-plugin-vectordb-retrieval*.jar`) are copied into `build/plugins/` and `build/olo/`, so legacy-named artifacts (e.g. old `plugin-vectordb`) are not collected. You may see Java deprecation or Gradle deprecation warnings; the build still succeeds. To inspect: compile with `-Xlint:deprecation` and run Gradle with `--warning-mode all`.

**Build a single project** (from that project’s directory):

```batch
cd olo-plugin-vectordb-retrieval
gradle build
gradle oloZip
```

## OLO annotation and .olo packages

- **`olo-annotations`**: `@OloPlugin` (id, name, version, description, capability, inputs, outputs) for UI designer metadata.
- **`olo-processor`**: Generates `plugin.yaml` from `@OloPlugin` at compile time.
- **.olo package**: ZIP (`.olo`) with `plugin.yaml`, `plugin.jar`, `icons/`, `README.md`, `LICENSE`, `checksums.sha256`. See [docs/plugin-yaml-schema.md](docs/plugin-yaml-schema.md).

Projects that use `@OloPlugin` and define the `oloZip` task (e.g. `olo-plugin-vectordb-retrieval`) produce a `.olo` file in `build/olo/` when you run `build.bat` or `build.sh`.

For a **review of all plugins** (functionality, SPI, annotations), **environment variables** (config with defaults), **common plugin data directory** (env `OLO_PLUGIN_DATA_DIR`, per-plugin subfolder for uploads/templates), and how to **validate a plugin as an individual unit** in the planned UI, see [docs/plugin-review.md](docs/plugin-review.md) and [docs/plugin-yaml-schema.md](docs/plugin-yaml-schema.md#validation-as-individual-unit-planned-ui).

## Releasing binaries on GitHub

1. Run **`build.bat`** or **`build.sh`** (optionally set `RELEASE_VERSION=1.2.0`).
2. In your GitHub repo, go to **Releases** → **Draft a new release**; tag e.g. `v1.2.0`.
3. Attach **`build/Open-LLM-Orchestrator-plugins-<version>.zip`** as the release binary asset.
4. Users download the zip and extract the `.olo` files for use in the Worker or pipeline UI (drag-and-drop plugins).

## CI – Release on commit

The workflow **`.github/workflows/release-on-commit.yml`** runs on every push to `main` (or `master`) and on **workflow_dispatch** (manual run). It:

1. Builds the repo with Java 21 and **`./build.sh`** (version = `1.0.0-<run_number>`).
2. Creates or updates a **Continuous** release (tag: `continuous`) and uploads **`Open-LLM-Orchestrator-plugins-<version>.zip`** as a prerelease asset.

Each run adds a new asset to the same release so you always have the latest commit’s binaries under **Releases → Continuous**.

**For CI to succeed**, the build must resolve **plugin-contract** (e.g. from Maven local or a published artifact). If the Worker repo is not available in CI:

- Publish **plugin-contract** from the Worker repo to a Maven repo (e.g. GitHub Packages or Maven Central) and point this repo’s build to it, or
- Add a prior job that checks out the Worker repo (using a token with repo access), runs `:plugin-contract:publishToMavenLocal`, and passes the Gradle user home as a cache to the build job.

## Using with the Worker

1. Run **`build.bat`** or **`build.sh`**.
2. Copy JARs from **`build/plugins/`** into the Worker’s `plugins/` directory.
3. Worker discovers handlers via Java SPI (`META-INF/services`) from those JARs.

## olo-annotations and olo-processor (standalone / Maven)

Both are **individual projects** with their own `settings.gradle` and `build.gradle`, so they can be moved to separate repos and published to Maven.

- **olo-annotations**: `@OloPlugin`, `PackageFormat`. No dependencies. Publishes as `com.openllm:olo-annotations:0.0.1` (or set `oloVersion` in `gradle.properties`).
- **olo-processor**: Annotation processor for `plugin.yaml`. Depends on olo-annotations (project in monorepo, or Maven when in own repo). Publishes as `com.openllm:olo-processor:0.0.1`.

**Publish to Maven local (from this repo):**
```batch
cd olo-annotations && gradle publishToMavenLocal && cd ..
cd olo-processor && gradle publishToMavenLocal && cd ..
```

**Publish to a remote repo:** Set env `MAVEN_REPO_URL`, `GITHUB_ACTOR`, `GITHUB_TOKEN`, then run `gradle publish` in each project directory.

**Use OLO from Maven:** After moving olo-annotations and olo-processor to their own repos and publishing, set in each plugin’s `gradle.properties` (or root `gradle.properties` if you keep one for shared defaults):
```properties
oloFromMaven=true
oloAnnotationsVersion=0.0.1
oloProcessorVersion=0.0.1
```
Plugin projects that depend on OLO (e.g. `olo-plugin-vectordb-retrieval`) resolve `com.openllm:olo-annotations` and `com.openllm:olo-processor` from Maven when not built as part of a root that includes those projects. The build script publishes OLO and ollama to Maven local first, so plugins resolve them when built standalone.

**Use olo-processor in its own repo:** Copy `olo-processor/` to a new repo. Its `build.gradle` already uses `com.openllm:olo-annotations` from Maven when `project(':olo-annotations')` is not present. Add `mavenLocal()` (or your repo) so it can resolve the dependency.

## Moving a plugin to another repo

Copy the project directory (e.g. `olo-plugin-vectordb-retrieval/`) into the new repo. It already has its own `settings.gradle` and `build.gradle`. In the new repo, either:

- Publish **olo-annotations** and **olo-processor** and depend on them via Maven (`oloFromMaven=true` and the Maven coords in that plugin’s `build.gradle`), or
- Add them as sibling projects / composite build and use `project(':olo-annotations')` and `project(':olo-processor')`.

Depend on **plugin-contract** via Maven (`com.openllm:plugin-contract:0.0.1`) or `includeBuild` to the Worker repo.

## Legacy `plugin-*` directories

The old `plugin-access`, `plugin-vectordb`, etc. directories are superseded by the `olo-plugin-*` projects. You can remove them after confirming the new build works.

## License

Apache License 2.0 (see LICENSE).
