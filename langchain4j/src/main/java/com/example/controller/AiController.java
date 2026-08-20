package com.example.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import jakarta.annotation.Resource;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private QwenChatModel qwenChatModel ;


    @RequestMapping("/chat/{question}")
    public Object chat(@PathVariable String question){
        System.out.println(question);
        return qwenChatModel.chat(question);
    }


}
