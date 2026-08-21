package com.example.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.configer.AIconfiger;

import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

@RequestMapping("/memory")
@RestController
public class ChatMemoryController {


    @Resource
    private AIconfiger.AI ai ;

     @RequestMapping("/chat/{question}/{mid}")
    public Object chat(@PathVariable("question") String question,@PathVariable("mid") Integer mid){
        System.out.println(question);
        return ai.chat(mid,question);
    }

    @RequestMapping(value =  "/streamchat/{question}/{mid}",produces = "text/stream;charset=utf-8")
    public Flux<String> streamchat(@PathVariable("question")  String question,@PathVariable("mid") Integer mid){
        System.out.println(question);
        
        TokenStream stream = ai.streamChat(mid,question); 
        return Flux.create(tmp->{
            stream.onPartialResponse(tmp::next)
                  .onCompleteResponse(res->tmp.complete())
                  .onError(tmp::error)
                  .start();
        }) ;
    }

}
