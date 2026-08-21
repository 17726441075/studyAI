package com.example.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/prompt")
public class PromptController {

    @Resource
    private ChatClient  promptChatClient;

    @RequestMapping(value = "/streamchat/{question}",produces = "text/stream;charset=utf-8")
    public Flux<String> streamchat(@PathVariable String question){
        return promptChatClient.prompt().user(question).stream().content() ;
    }

    @Resource
    private ChatClient  promptTemplateChatClient;

    @RequestMapping(value = "/streamchat2/{question}",produces = "text/stream;charset=utf-8")
    public Flux<String> streamchat2(@PathVariable String question){
        return promptTemplateChatClient.prompt().user(question).stream().content() ;
    }

}
