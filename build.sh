#!/usr/bin/env sh
# Build all projects standalone (no root Gradle project). Each project is independent.
# Use when repos are distributed: run from this tree or from a repo that only has a subset.
# Requires: Gradle (gradle) on PATH, or add a gradlew in each project dir.
# Output: build/plugins/ (JARs), build/olo/ (.olo packages), build/Open-LLM-Orchestrator-plugins-<version>.zip (all .olo for GitHub release).

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

RELEASE_VERSION="${RELEASE_VERSION:-1.0.0}"

gradle_in() {
  d="$1"; shift
  (cd "$d" && if [ -x ./gradlew ]; then ./gradlew "$@"; else gradle "$@"; fi)
}

echo "Cleaning root build folder..."
rm -rf build

echo "[1/6] Publishing plugin-contract from Worker if present..."
if [ -d "../Open-LLM-Orchestrator-Worker" ]; then
  (cd "../Open-LLM-Orchestrator-Worker" && if [ -x ./gradlew ]; then ./gradlew :plugin-contract:publishToMavenLocal; else gradle :plugin-contract:publishToMavenLocal; fi) || true
else
  echo "       Skipped - Worker not at ../Open-LLM-Orchestrator-Worker. Ensure plugin-contract is in mavenLocal."
fi

echo "[2/6] Building and publishing olo-annotations..."
gradle_in olo-annotations publishToMavenLocal || { echo "FAILED: olo-annotations"; exit 1; }

echo "[3/6] Building and publishing olo-processor..."
gradle_in olo-processor publishToMavenLocal || { echo "FAILED: olo-processor"; exit 1; }

echo "[4/6] Building and publishing olo-plugin-llm-ollama..."
gradle_in olo-plugin-llm-ollama clean publishToMavenLocal || { echo "FAILED: olo-plugin-llm-ollama"; exit 1; }
mkdir -p build/plugins build/olo
gradle_in olo-plugin-llm-ollama oloZip 2>/dev/null && cp -f olo-plugin-llm-ollama/build/distributions/*.olo build/olo/ 2>/dev/null || true

echo "[5/6] Building plugin projects and collecting outputs..."

cp -f olo-plugin-llm-ollama/build/libs/olo-plugin-llm-ollama*.jar build/plugins/ 2>/dev/null || true

for dir in olo-plugin-access-allowall olo-plugin-caching-memory olo-plugin-vectordb-retrieval olo-plugin-llm-mistral olo-plugin-llm-phi3 olo-plugin-llm-gemma2 olo-plugin-llm-qwen2 olo-plugin-tokenizer-document olo-plugin-folder-ingestion olo-plugin-output-answerformat olo-plugin-memory-context olo-plugin-tool-echo olo-plugin-guardrail-simple olo-plugin-prompt-simple olo-plugin-observability-passthrough olo-plugin-sample-stubs; do
  if [ -d "$dir" ]; then
    if gradle_in "$dir" clean build; then
      cp -f "$dir/build/libs/${dir}"*.jar build/plugins/ 2>/dev/null || true
    fi
    gradle_in "$dir" oloZip 2>/dev/null && cp -f "$dir/build/distributions/${dir}"*.olo build/olo/ 2>/dev/null || true
  fi
done

echo "[6/6] Creating release zip (all .olo binaries)..."
ZIPNAME="Open-LLM-Orchestrator-plugins-${RELEASE_VERSION}.zip"
if ls build/olo/*.olo 1>/dev/null 2>&1; then
  (cd build/olo && zip -r "../${ZIPNAME}" *.olo)
  echo "  build/${ZIPNAME} - All .olo packages (upload to GitHub Releases)"
fi

echo ""
echo "Done. Outputs:"
echo "  build/plugins/  - Plugin JARs"
echo "  build/olo/      - .olo packages"
echo "  build/${ZIPNAME} - Release zip for GitHub"
