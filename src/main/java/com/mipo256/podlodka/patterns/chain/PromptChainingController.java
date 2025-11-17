package com.mipo256.podlodka.patterns.chain;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mipo256.podlodka.patterns.chain.PromptChainingPatternService.*;

@RestController
@RequestMapping(path = "/patterns/prompt-chaining")
public class PromptChainingController {

    private final PromptChainingPatternService promptChainingPattern;

    public PromptChainingController() {
        this.promptChainingPattern = new PromptChainingPatternService(
                ChatClient.create(
                        OllamaChatModel
                                .builder()
                                .ollamaApi(
                                        OllamaApi
                                                .builder()
                                                .baseUrl("http://localhost:11434")
                                                .build()
                                )
                                .defaultOptions(
                                        OllamaOptions
                                                .builder()
                                                .model("qwen3-vl:4b")
                                                .build()
                                )
                                .build()
                ),
                ChatClient.create(
                        OllamaChatModel
                                .builder()
                                .ollamaApi(
                                        OllamaApi
                                                .builder()
                                                .baseUrl("http://localhost:11434")
                                                .build()
                                )
                                .defaultOptions(
                                        OllamaOptions
                                                .builder()
                                                .model("llama3.1:8b")
                                                .build()
                                )
                                .build()
                )
        );
    }

    @PostMapping(consumes = MediaType.IMAGE_PNG_VALUE)
    public ClassificationResult chain(@RequestBody byte[] image) {
        return promptChainingPattern.promptChainingEvaluation(image);
    }
}
