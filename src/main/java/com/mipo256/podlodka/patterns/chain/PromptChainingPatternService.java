package com.mipo256.podlodka.patterns.chain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

public class PromptChainingPatternService {
    private static final Logger log = LoggerFactory.getLogger(PromptChainingPatternService.class);

    // Image prompts start

    private final PromptTemplate IMAGE_USER_PROMPT_TEMPLATE = new PromptTemplate("""
            Please, analyze the provided image. Explain your reasoning.
            """);

    private final PromptTemplate IMAGE_SYSTEM_PROMPT_TEMPLATE = new PromptTemplate("""
            You are the inspector, that analyzes the input images, looking for the signs of:
            
            - Violence
            - Crime
            - Terrorism
            
            Your job is to either extract the message out of the image, or extract the overall vibe and
            explain your feelings and reasoning about this image.
            """);

    // Image prompts end

    // Generative prompts start

    private final PromptTemplate CLASSIFICATION_USER_PROMPT_TEMPLATE = new PromptTemplate("""
            Please, analyze the reasoning provided by your peer about the particular image and classify it. 
            """);

    private final PromptTemplate CLASSIFICATION_SYSTEM_PROMPT_TEMPLATE = new PromptTemplate("""
            You are the inspector, that analyzes the description of the image. The description has been
            provided by your peer model. The peer model already analyzed the image and prodiced a summary
            of what it thinks it sees on the image.
            
            Your job is to make the final decision whether or not this content is VALID or INVALID. The 
            content is deemed INVALID if the image contains sings of:
            
            - Violence
            - Crime
            - Terrorism
            
            Your job is to make the final decision.
            """);

    // Generative prompts end


    private final ChatClient visionModel;
    private final ChatClient classificationModel;

    public PromptChainingPatternService(ChatClient visionModel, ChatClient classificationModel) {
        this.visionModel = visionModel;
        this.classificationModel = classificationModel;
    }

    public ClassificationResult promptChainingEvaluation(byte[] array) {
        ChatClient.CallResponseSpec result = visionModel.prompt(
                Prompt
                        .builder()
                        .messages(
                                new SystemMessage(IMAGE_SYSTEM_PROMPT_TEMPLATE.render()),
                                UserMessage
                                        .builder()
                                        .text(IMAGE_USER_PROMPT_TEMPLATE.render())
                                        .media(
                                                new Media(
                                                        MediaType.IMAGE_PNG,
                                                        new ByteArrayResource(array)
                                                )
                                        )
                                        .build()
                        )
                        .build()
        ).call();

        String generation = result.content(); // result from first LLM

        log.info("Prompt Chain. Generation 1: {}", generation);

        ClassificationResult entity = classificationModel.prompt(
                Prompt
                        .builder()
                        .messages(new SystemMessage(CLASSIFICATION_SYSTEM_PROMPT_TEMPLATE.render()))
                        .messages(new UserMessage(CLASSIFICATION_USER_PROMPT_TEMPLATE.render()))
                        .build()
        ).call().entity(new BeanOutputConverter<>(ClassificationResult.class));

        log.info("Prompt Chain. Generation 2: {}", entity);

        return entity;
    }

    public record ClassificationResult(Result result) {

    }

    enum Result {
        VALID,
        INVALID
    }
}
