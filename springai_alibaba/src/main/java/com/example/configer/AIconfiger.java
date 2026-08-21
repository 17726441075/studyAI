package com.example.configer;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.memory.redis.JedisRedisChatMemoryRepository;


@Configuration
public class AIconfiger {
    
    @Autowired
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

    
    @Bean
    public ChatClient promptTemplateResourceChatClient(@Value("classpath:prompt.yml")Resource resource){
        String render = PromptTemplate.builder()
                                      .resource(resource)
                                      .build()
                                      .render(Map.of("zhiye","C","part","C++")); 
        return ChatClient.builder(dashScopeChatModel)
                .defaultSystem(render)
                .build();
    }

    @Bean
    public ChatMemory chatMemory(@Value("${spring.data.redis.host}") String host,
                                 @Value("${spring.data.redis.port}") int port,
                                 @Value("${spring.data.redis.password}") String password){
        JedisRedisChatMemoryRepository redisChatMemoryRepository = JedisRedisChatMemoryRepository.builder()
                                                                                                .host(host) // 添加reids的主机
                                                                                                .port(port) // 添加redis的端口
                                                                                                // .user 配置了用户的需要在这里添加用户名
                                                                                                .password(password)
                                                                                                .build();
        return MessageWindowChatMemory.builder()
                                    .maxMessages(10) // 最大消息数
                                    .chatMemoryRepository(redisChatMemoryRepository)
                                    .build();
    }

}
