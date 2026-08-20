package com.example.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.configer.AIconfiger;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

@RequestMapping("/memory")
@RestController
public class ChatMemoryController {


    @Resource
    private AIconfiger.AI ai ;

     @RequestMapping("/chat/{question}")
    public Object chat(@PathVariable String question){
        System.out.println(question);
        return ai.chat(question);
    }

    @RequestMapping(value =  "/streamchat/{question}",produces = "text/stream;charset=utf-8")
    public Flux<String> streamchat(@PathVariable String question){
        System.out.println(question);
        
        TokenStream stream = ai.streamChat(question); 
        return Flux.create(tmp->{
            stream.onPartialResponse(tmp::next)
                  .onCompleteResponse(res->tmp.complete())
                  .onError(tmp::error)
                  .start();
        }) ;
    }

}
