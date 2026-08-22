package com.example.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tool")
public class ToolController {

    @Autowired
    private ChatClient toolChatClient ;

    @RequestMapping(value = "/chat/{question}")
    public Object chat(@PathVariable String question){
        String content = toolChatClient.prompt()
                                        .user(question)
                                        .call()
                                        .content();
        return content;
    }

}
