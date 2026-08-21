package com.example.configer;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
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

    @Bean
    public ChatClient promptTemplateChatClient(){
        String render = PromptTemplate.builder()
                                      .template("你是小{zhiye}，你精通{part}领域的相关所有知识，用户提问时，你会深度思考并简要回答，不会说不符合事实的说！")
                                      .build()
                                      .render(Map.of("zhiye","P","part","python")); 
        return ChatClient.builder(dashScopeChatModel)
                .defaultSystem(render)
                .build() ;
    }

}
