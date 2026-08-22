package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = Main.class)
public class ToolTest {
    
    @Autowired
    private ChatClient toolChatClient ;


    @Test
    public void test01() {
        String question = "查询广州的天气";
        
        String content = toolChatClient.prompt()
                .user(question)
                .call()
                .content();
                
        log.info(content);
    }

}
