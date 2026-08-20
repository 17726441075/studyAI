package com.example.configer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;


@Configuration
public class AIconfiger {
    
    public interface AI {
        String chat(String question);
        TokenStream streamChat(String question) ;
    }

    @Bean
    public AI assistant(QwenChatModel qwenChatModel,QwenStreamingChatModel qwenStreamingChatModel){
        return AiServices.builder(AI.class)
                         .chatModel(qwenChatModel)
                         .streamingChatModel(qwenStreamingChatModel)
                         .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                         .build() ;
    }

}
