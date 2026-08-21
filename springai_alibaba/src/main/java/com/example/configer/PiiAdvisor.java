package com.example.configer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PiiAdvisor implements BaseAdvisor {
    
    @Autowired
    private ChatClient piiChatClient;

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String cleaned = piiChatClient.prompt()
                             .user("请对这些内容脱敏："+chatClientRequest.prompt().getUserMessage().getText())
                             .call()
                             .content(); ;
        // 将脱敏的消息重新封装成为用户消息
        // 将消息封装到请求中进行返回
        return chatClientRequest
                .mutate()
                .prompt(Prompt.builder().content(cleaned).messages().build())
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

}