package com.example.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/ai")
public class ChatController {
    
    @Resource 
    private DashScopeChatModel dashScopeModel ;
    
    @RequestMapping(value = "/chat/{question}")
    public Object chat(@PathVariable String question){
        return dashScopeModel.call(question) ;
    }

    @RequestMapping(value = "/streamchat/{question}",produces = "text/event-stream;charset=utf-8")
    public Flux<String> streamchat(@PathVariable String question){
        return dashScopeModel.stream(question) ;
    }

}
