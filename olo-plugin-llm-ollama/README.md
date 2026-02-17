# olo-plugin-llm-ollama

Ollama LLM plugin for Open LLM Orchestrator. Chat, RAG, and fixed-model variants (OllamaModelResolver, Llama32ChatPlugin, Llama32ModelPlugin, Llama32FixedChatPlugin).

## Build

```bash
gradle build
gradle oloZip
```

Output: `build/libs/olo-plugin-llm-ollama.jar` and `build/distributions/olo-plugin-llm-ollama-1.0.0.olo`.

## Dependencies

- **plugin-contract** (`com.openllm:plugin-contract`)
- **Jackson** (databind)
- **olo-annotations** / **olo-processor** (compile-only / annotation processor)

Other fixed-model plugins (llm-mistral, llm-phi3, llm-gemma2, llm-qwen2) may depend on this project when built in the monorepo.

## License

Apache License 2.0.
