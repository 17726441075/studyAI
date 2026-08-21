package com.example.configer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;


@Configuration
@RequiredArgsConstructor
public class AIconfiger {
    
    @Resource
    private DashScopeChatModel dashScopeChatModel ;

    @Bean
    public ChatClient promptChatClient(){
        return ChatClient.builder(dashScopeChatModel)
                .defaultSystem("你是小J，你精通java领域的相关所有知识，用户提问时，你会深度思考并简要回答，不会说不符合事实的说！")         
                .build() ;
    }

}
