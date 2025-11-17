package com.mipo256.podlodka.patterns.parallelization;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

public class ParallelizationWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(ParallelizationWorkflowService.class);

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

    private final PromptTemplate LLAMA_SYSTEM_PROMPT = new PromptTemplate("""
            You are the inspector, that analyzes the incoming transaction for recreational risk management. In other
            word, is it possible that this transaction would hurt the reputation of our banking institution or not.
            
            You job is for each transaction to make a decision - should we approve it or not:

            - 'VALID' for approve
            - 'INVALID' for halting the transaction.
            
            If you find anything that seems to you suspicious - command to halt the transaction. 
            """);

    // Image prompts end


    private final ChatClient deepseekR1;
    private final ChatClient llama;

    public ParallelizationWorkflowService(ChatClient deepseekR1, ChatClient llama) {
        this.deepseekR1 = deepseekR1;
        this.llama = llama;
    }

    public boolean parallelizationWorkflow(String prompt) {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        // Structured concurrency is still in preview in Java 25
        Future<ClassificationResult> first = executorService.submit(() -> generateFirst(prompt));
        Future<ClassificationResult> second = executorService.submit(() -> generateSecond(prompt));

        try {
            ClassificationResult resultFirst = first.get();
            ClassificationResult resultSecond = second.get();

            return resultFirst.result() == Result.VALID
                    && resultSecond.result() == Result.VALID;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private ClassificationResult generateSecond(String prompt) {
        log.info("Parallelization. Generation 2");
        return llama.prompt(
                Prompt
                        .builder()
                        .messages(
                                new SystemMessage(LLAMA_SYSTEM_PROMPT.render()),
                                new UserMessage(USER_PROMPT_TEMPLATE.render(Map.of("content", prompt)))
                        )
                        .build()
        ).call().entity(new BeanOutputConverter<>(ClassificationResult.class));
    }

    private ClassificationResult generateFirst(String prompt) {
        log.info("Parallelization. Generation 1");
        return deepseekR1.prompt(
                Prompt
                        .builder()
                        .messages(
                                new SystemMessage(DEEPSEEK_SYSTEM_PROMPT.render()),
                                new UserMessage(USER_PROMPT_TEMPLATE.render(Map.of("content", prompt)))
                        )
                        .build()
        ).call().entity(new BeanOutputConverter<>(ClassificationResult.class));
    }

    public record ClassificationResult(Result result) {

    }

    enum Result {
        VALID,
        INVALID
    }
}
