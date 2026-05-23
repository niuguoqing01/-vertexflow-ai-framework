# VertexFlow AI RAG 中文使用指南

本文档介绍 VertexFlow AI Framework 中 RAG、文档加载、文档切分、向量检索和引用来源返回的使用方式。

---

## 1. RAG 是什么？

RAG，全称 Retrieval-Augmented Generation，检索增强生成。

它的基本流程是：

```text
用户问题
  -> 向量检索相关文档
  -> 构造上下文
  -> 调用大模型
  -> 返回基于知识库的回答
```

---

## 2. 创建 RagEngine

```java
RagEngine rag = RagBuilder.create()
        .chatModel(model)
        .embeddingModel(new SimpleTextEmbedding(256))
        .splitter(new MarkdownDocumentSplitter(300, 50))
        .options(RagOptions.defaults()
                .setTopK(3)
                .setReturnSources(true))
        .build();
```

---

## 3. 添加文档

```java
rag.addDocument(new Document("doc1", """
        VertexFlow AI Framework 是一个面向 Java 开发者的轻量级 AI 应用开发框架。
        它支持 ChatModel、Memory、RAG、Tool Calling 和 Spring Boot Starter。
        """));
```

---

## 4. 普通问答

```java
String answer = rag.ask("VertexFlow AI Framework 是什么？");

System.out.println(answer);
```

---

## 5. 返回引用来源

```java
RagAnswer answer = rag.askWithSources("VertexFlow AI Framework 支持哪些能力？");

System.out.println(answer.content());

for (RagSource source : answer.sources()) {
    System.out.println("documentId: " + source.documentId());
    System.out.println("chunkId: " + source.chunkId());
    System.out.println("score: " + source.score());
    System.out.println("content: " + source.content());
}
```

---

## 6. TextFileDocumentLoader

从本地文本文件加载文档：

```java
Document document = TextFileDocumentLoader.loadFile(
        "docs/vertexflow-intro.txt"
);

rag.addDocument(document);
```

---

## 7. DirectoryDocumentLoader

批量加载目录下的 `.txt` 和 `.md` 文件：

```java
List<Document> documents = DirectoryDocumentLoader.loadDirectory("docs");

for (Document document : documents) {
    rag.addDocument(document);
}
```

---

## 8. FixedSizeDocumentSplitter

按固定长度切分文档：

```java
DocumentSplitter splitter = new FixedSizeDocumentSplitter(300, 50);

List<DocumentChunk> chunks = splitter.split(document);
```

---

## 9. MarkdownDocumentSplitter

按 Markdown 标题结构切分文档：

```java
DocumentSplitter splitter = new MarkdownDocumentSplitter(500, 80);

List<DocumentChunk> chunks = splitter.split(document);
```

适合处理：

```text
# 一级标题
## 二级标题
### 三级标题
```

---

## 10. VectorStore 抽象

```java
VectorStore vectorStore = new InMemoryVectorStore(new SimpleTextEmbedding(256));
```

未来可以扩展：

- QdrantVectorStore
- MilvusVectorStore
- PgVectorStore
- RedisVectorStore

---

## 11. RagOptions

```java
RagOptions options = RagOptions.defaults()
        .setTopK(3)
        .setReturnSources(true);
```

参数说明：

| 参数 | 说明 |
|---|---|
| topK | 检索返回的文档片段数量 |
| returnSources | 是否返回引用来源 |

---

## 12. 推荐实践

RAG 推荐组合：

```java
RagEngine rag = RagBuilder.create()
        .chatModel(model)
        .embeddingModel(new SimpleTextEmbedding(256))
        .splitter(new MarkdownDocumentSplitter(500, 80))
        .options(RagOptions.defaults().setTopK(3).setReturnSources(true))
        .build();
```

生产环境建议后续替换：

```text
SimpleTextEmbedding -> 真实 EmbeddingModel
InMemoryVectorStore -> Qdrant / Milvus / pgvector
```