package com.example.configer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.service.ToolService;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;


@Configuration
public class AIconfiger {
    
    public interface AI {
        @SystemMessage("""
                你是一个python专家，熟练掌握python和相关所有的技术栈，用户提问时你会结合自身的知识，进行深度思考并回答并且不会胡乱编造事实！
                """)
        String chat(@MemoryId Integer id,@UserMessage String question);
        @SystemMessage("""
                你的名字叫小助，你是一个java专家，熟练掌握java和相关所有的技术栈，用户提问时你会结合自身的知识，进行深度思考并简洁易懂地回答，回答时会抓住核心，不会胡乱编造事实！
                """)
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
