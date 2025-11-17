package com.mipo256.podlodka.patterns.parallelization;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/patterns/parallelization")
public class ParallelizationWorkflowController {

    private final ParallelizationWorkflowService parallelizationWorkflowService;

    public ParallelizationWorkflowController() {
        this.parallelizationWorkflowService = new ParallelizationWorkflowService(
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
                                                .model("deepseek-r1:latest")
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

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public boolean parallelize(@RequestBody String prompt) {
        return parallelizationWorkflowService.parallelizationWorkflow(prompt);
    }

}
