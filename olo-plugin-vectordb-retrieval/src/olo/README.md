# Vector Store Retrieval Plugin

Store document chunks (document pipeline) or retrieve chunks by question (QA/RAG pipeline).

## Inputs

- **question** (string) – User question for retrieval.
- **tokenizedChunks** (array) – Chunks from tokenizer to store.

## Outputs

- **retrievedChunks** (array) – Retrieved chunks for RAG.
- **stored** (boolean) – True if chunks were stored.
- **chunkCount** (integer) – Number of chunks stored.

## License

Apache-2.0
