package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = Main.class)
public class ChatTest {
    

    @Resource
    private DashScopeChatModel dashScopeChatModel ;

    @Test
    public void test1(){
        System.out.println("sssssssssssssdfdfsdfd");
        String res = dashScopeChatModel.call("你好") ;
        log.info(res);

    }


}
