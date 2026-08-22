package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = Main.class)
public class JsonOutputTest {

    // 编译器会自动生成：构造器、getter、equals()、hashCode()、toString()
    public record Product(
            String name,        // 商品名称
            Double price,      // 商品价格
            String category    // 商品分类
    ) {}

    @Test
    public void testChatClientOutput(@Autowired DashScopeChatModel dashScopeChatModel) {
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel).build();

        Product product = chatClient.prompt()
                                    .user("推荐1款无线蓝牙耳机")
                                    .call()
                                    .entity(Product.class);  // 指定输出类型
                            
        log.info(product.toString());
    }
}
