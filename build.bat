@echo off
REM Build all projects standalone (no root Gradle project). Each project is independent.
REM Use when repos are distributed: run from this tree or from a repo that only has a subset.
REM Requires: Gradle (gradle) or gradlew in each project - or use a single gradle/gradlew from PATH.
REM Output: build\plugins\ (JARs), build\olo\ (.olo packages), build\Open-LLM-Orchestrator-plugins-<version>.zip (all .olo for GitHub release).

set ROOT=%~dp0
cd /d "%ROOT%"

set GRADLE_CMD=gradle
if exist "gradlew.bat" set GRADLE_CMD=gradlew.bat

if not defined RELEASE_VERSION set RELEASE_VERSION=1.0.0

echo Cleaning root build folder...
if exist "build" rmdir /s /q "build"

echo [1/6] Publishing plugin-contract from Worker if present...
if exist "..\Open-LLM-Orchestrator-Worker" (
  pushd "..\Open-LLM-Orchestrator-Worker"
  if exist gradlew.bat (call gradlew.bat :plugin-contract:publishToMavenLocal) else (gradle :plugin-contract:publishToMavenLocal)
  popd
) else (
  echo        Skipped - Worker not at ../Open-LLM-Orchestrator-Worker. Ensure plugin-contract is in mavenLocal.
)

echo [2/6] Building and publishing olo-annotations...
pushd olo-annotations
call %GRADLE_CMD% publishToMavenLocal
if errorlevel 1 (popd & echo BUILD FAILED & exit /b 1)
popd

echo [3/6] Building and publishing olo-processor...
pushd olo-processor
call %GRADLE_CMD% publishToMavenLocal
if errorlevel 1 (popd & echo BUILD FAILED & exit /b 1)
popd

echo [4/6] Building and publishing olo-plugin-llm-ollama...
pushd olo-plugin-llm-ollama
call %GRADLE_CMD% clean publishToMavenLocal
if errorlevel 1 (popd & echo BUILD FAILED & exit /b 1)
call %GRADLE_CMD% oloZip 2>nul
if not errorlevel 1 (
  if not exist "%ROOT%build\olo" mkdir "%ROOT%build\olo"
  copy /Y "build\distributions\*.olo" "%ROOT%build\olo\" 2>nul
)
popd

echo [5/6] Building plugin projects and collecting outputs...
if not exist "build\plugins" mkdir "build\plugins"
if not exist "build\olo" mkdir "build\olo"

copy /Y "olo-plugin-llm-ollama\build\libs\olo-plugin-llm-ollama*-all.jar" "build\plugins\" 2>nul

for /d %%P in (olo-plugin-access-allowall olo-plugin-caching-memory olo-plugin-vectordb-retrieval olo-plugin-llm-mistral olo-plugin-llm-phi3 olo-plugin-llm-gemma2 olo-plugin-llm-qwen2 olo-plugin-tokenizer-document olo-plugin-folder-ingestion olo-plugin-output-answerformat olo-plugin-memory-context olo-plugin-tool-echo olo-plugin-guardrail-simple olo-plugin-prompt-simple olo-plugin-observability-passthrough olo-plugin-sample-stubs) do (
  if exist "%%P" (
    pushd "%%P"
    call %GRADLE_CMD% clean build
    if not errorlevel 1 (
      copy /Y "build\libs\%%P*-all.jar" "%ROOT%build\plugins\" 2>nul
    )
    call %GRADLE_CMD% oloZip 2>nul
    if not errorlevel 1 (
      copy /Y "build\distributions\%%P*.olo" "%ROOT%build\olo\" 2>nul
    )
    popd
  )
)

echo [6/6] Creating release zip (all .olo binaries)...
set ZIPNAME=Open-LLM-Orchestrator-plugins-%RELEASE_VERSION%.zip
set ZIPPATH=%ROOT%build\%ZIPNAME%
pushd build\olo
powershell -NoProfile -Command "if (Get-ChildItem -Filter *.olo -ErrorAction SilentlyContinue) { Compress-Archive -Path *.olo -DestinationPath '%ZIPPATH%' -Force }"
popd
if exist "build\%ZIPNAME%" echo   build\%ZIPNAME% - All .olo packages ^(upload to GitHub Releases^)

echo.
echo Done. Outputs:
echo   build\plugins\  - Plugin JARs
echo   build\olo\      - .olo packages
echo   build\%ZIPNAME%  - Release zip for GitHub
exit /b 0
