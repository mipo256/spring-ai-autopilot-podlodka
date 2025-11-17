package com.mipo256.podlodka.patterns.routing;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

public class RoutingWorkflowService {

    private final PromptTemplate USER_PROMPT_TEMPLATE = new PromptTemplate("""
            Please, analyze the incoming transaction. Explain your reasoning and 
            provide a final verdict - should we approve it:
            
            {content}
            """);

    private final PromptTemplate DEEPSEEK_SYSTEM_PROMPT = new PromptTemplate("""
            You are the inspector, that analyzes the incoming transaction for the subject of being fraud.
            
            You job is for each transaction to make a decision - should we approve it or not:

            - 'VALID' for approve
            - 'INVALID' for halting the transaction.
            
            If you find anything that seems to you suspicious - command to halt the transaction. 
            """);


    // Image prompts end

    private final ChatClient deepseekR1;

    public RoutingWorkflowService(ChatClient deepseekR1) {
        this.deepseekR1 = deepseekR1;
    }

    public void route(String prompt) {
        ClassificationResult first = deepseekR1.prompt(
                Prompt
                        .builder()
                        .messages(
                                new SystemMessage(DEEPSEEK_SYSTEM_PROMPT.render()),
                                new UserMessage(USER_PROMPT_TEMPLATE.render(Map.of("content", prompt)))
                        )
                        .build()
        ).call().entity(new BeanOutputConverter<>(ClassificationResult.class));

        switch (first.result()) {
            case VALID -> System.out.println("Transaction is valid. Proceeding...");
            case INVALID -> System.out.println("Transaction is invalid valid. Another code path is taken. M");
        }
    }

    public record ClassificationResult(Result result) {

    }

    enum Result {
        VALID,
        INVALID
    }
}
