package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = Main.class)
public class SearchUpgradeTest {
    
    @Autowired
    private ChatClient retrievalChatClient;

    @Test
    public void queryTest(@Autowired VectorStore milvusVectorStore) {

        String response = retrievalChatClient.prompt("介绍无线蓝牙耳机").call().content();
        log.info(response);
    
    }

   
}