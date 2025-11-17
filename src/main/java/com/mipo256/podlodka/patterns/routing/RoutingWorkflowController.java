package com.mipo256.podlodka.patterns.routing;

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
@RequestMapping(path = "/patterns/route")
public class RoutingWorkflowController {

    private final RoutingWorkflowService routingWorkflowService;

    public RoutingWorkflowController() {
        this.routingWorkflowService = new RoutingWorkflowService(
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
                )
        );
    }

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public void route(@RequestBody String prompt) {
        routingWorkflowService.route(prompt);
    }

}
