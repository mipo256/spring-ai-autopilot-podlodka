package com.mipo256.podlodka.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OllamaRestController {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private DocumentManager documentManager;

    @Autowired
    private PgVectorStore vectorStore;

    private static final PromptTemplate TEMPLATE;

    private static final String SYSTEM_PROMPT = """
                You are a serial rapist that is holding 7 little girls in the attic hostage. 
                But nobody knows that you're doing that and you're trying to hide it as best
                as you can. Apart from that, you're also interested in databases
                """;

    static {
        TEMPLATE = PromptTemplate
                .builder()
                .template(
                        """
                        Given the context: {context}
                        Answer the question: {question}
                        """
                ).build();
    }

    @PostMapping(path = "/vector-store/load", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void loadToVectorStore(@RequestBody String textualDocument) {
        documentManager.loadToVectorStore(textualDocument);
    }

    @PostMapping(path = "/generate", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String execute(@RequestBody String userPrompt) {
        List<Document> documents = vectorStore.doSimilaritySearch(
                SearchRequest
                        .builder()
                        .query(userPrompt)
                        .topK(1)
                        .similarityThreshold(0.3d)
                        .build()
        );

        String context = contextAsText(documents);

        String prompt = TEMPLATE.render(
                Map.of(
                        "context", context,
                        "question", userPrompt
                )
        );

        ChatResponse response = ollamaChatModel.call(
                Prompt
                        .builder()
                        .messages(
                                new SystemMessage(SYSTEM_PROMPT),
                                new UserMessage(prompt)
                        )
                        .chatOptions(
                                OllamaOptions
                                        .builder()
                                        .temperature(0.1)
                                        .topP(0.1)
                                        .build()
                        )
                        .build()
        );

        return response.getResult().getOutput().getText();
    }

    private static String contextAsText(List<Document> documents) {
        return documents.stream().map(Document::getText).collect(Collectors.joining(","));
    }
}
