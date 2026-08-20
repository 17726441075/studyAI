package com.example.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class LangChain4jController {

    @Resource
    private QwenChatModel qwenChatModel ;

    @Resource
    private QwenStreamingChatModel qwenStreamingChatModel ;

    @RequestMapping("/chat/{question}")
    public Object chat(@PathVariable String question){
        System.out.println(question);
        return qwenChatModel.chat(question);
    }

    @RequestMapping(value =  "/streamchat/{question}",produces = "text/stream;charset=utf-8")
    public Flux<String> streamchat(@PathVariable String question){
        System.out.println(question);
        return Flux.create(tmp->{
            qwenStreamingChatModel.chat(question, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse){
                    tmp.next(partialResponse) ;
                }
                @Override
                public void onCompleteResponse(ChatResponse arg0) {
                    tmp.complete(); 
                }
                @Override
                public void onError(Throwable arg0) {
                    throw new UnsupportedOperationException("Unimplemented method 'onError'");
                }
            });
        }) ;
    }

}
