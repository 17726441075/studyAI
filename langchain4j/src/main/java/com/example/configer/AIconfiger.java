package com.example.configer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.service.ToolService;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;


@Configuration
public class AIconfiger {
    
    public interface AI {
        String chat(@MemoryId Integer id,@UserMessage String question);
        TokenStream streamChat(@MemoryId Integer id,@UserMessage String question) ;
    }

    @Bean
    public AI assistant(QwenChatModel qwenChatModel,QwenStreamingChatModel qwenStreamingChatModel,ToolService toolService){
        return AiServices.builder(AI.class)
                         .tools(toolService)
                         .chatModel(qwenChatModel)
                         .streamingChatModel(qwenStreamingChatModel)
                         .chatMemoryProvider(id->new HashMapChatMemory(id.toString(), 10))
                         .build() ;
    }

}
