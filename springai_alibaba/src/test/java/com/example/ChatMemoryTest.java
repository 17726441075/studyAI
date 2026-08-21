package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = Main.class)
public class ChatMemoryTest {
    
    @Test
    public void chatMemoryTest(@Autowired DashScopeChatModel chatModel){
            // 构建一个ChatMemory对象用来存储对话的消息内容
            ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
            String id = IdUtil.simpleUUID(); // 唯一标识符
            // 第一轮对话
            // 使用唯一标识符对不同的对话进行隔离，将用户的消息存储在下来
            chatMemory.add(id, new UserMessage("我36岁是一个java全栈工程师"));
            String text = chatModel.call(
                                        Prompt.builder()
                                             .messages(chatMemory.get(id))   //在ChatMemory中将所有的消息都查询出来
                                             .build()
                                    )
                                    .getResult()
                                    .getOutput()    
                                    .getText();
            // 将AI返回的消息进程存储
            chatMemory.add(id, new AssistantMessage(text));
            log.info(text);
            // 第二轮对话
            // 将用户第二次的对话存储到ChatMemory中
            chatMemory.add(id, new UserMessage("我的工作是什么"));
            String text2 = chatModel.call(
                                        Prompt.builder()
                                        // 再次从ChatMemory中查找到所有存储的消息
                                        .messages(chatMemory.get(id))
                                        .build()
                                    )
                                    .getResult()
                                    .getOutput()
                                    .getText();
            log.info(text2);
    }

    @Test
    public void redisChatMemoryTest(@Autowired ChatMemory redisChatMemory,@Autowired DashScopeChatModel chatModel){
            // 构建一个ChatMemory对象用来存储对话的消息内容
            ChatMemory chatMemory = redisChatMemory;
            String id = IdUtil.simpleUUID(); // 唯一标识符
            // 第一轮对话
            // 使用唯一标识符对不同的对话进行隔离，将用户的消息存储在下来
            chatMemory.add(id, new UserMessage("我36岁是一个java全栈工程师"));
            String text = chatModel.call(
                                        Prompt.builder()
                                             .messages(chatMemory.get(id))   //在ChatMemory中将所有的消息都查询出来
                                             .build()
                                    )
                                    .getResult()
                                    .getOutput()    
                                    .getText();
            // 将AI返回的消息进程存储
            chatMemory.add(id, new AssistantMessage(text));
            log.info(text);
            // 第二轮对话
            // 将用户第二次的对话存储到ChatMemory中
            chatMemory.add(id, new UserMessage("我的工作是什么"));
            String text2 = chatModel.call(
                                        Prompt.builder()
                                        // 再次从ChatMemory中查找到所有存储的消息
                                        .messages(chatMemory.get(id))
                                        .build()
                                    )
                                    .getResult()
                                    .getOutput()
                                    .getText();
            log.info(text2);
    }

    @Test
    public void jdbcChatMemoryTest(@Autowired ChatMemory jdbcChatMemory,@Autowired DashScopeChatModel chatModel){
            // 构建一个ChatMemory对象用来存储对话的消息内容
            ChatMemory chatMemory = jdbcChatMemory;
            String id = IdUtil.simpleUUID(); // 唯一标识符
            // 第一轮对话
            // 使用唯一标识符对不同的对话进行隔离，将用户的消息存储在下来
            chatMemory.add(id, new UserMessage("我36岁是一个java全栈工程师"));
            String text = chatModel.call(
                                        Prompt.builder()
                                             .messages(chatMemory.get(id))   //在ChatMemory中将所有的消息都查询出来
                                             .build()
                                    )
                                    .getResult()
                                    .getOutput()    
                                    .getText();
            // 将AI返回的消息进程存储
            chatMemory.add(id, new AssistantMessage(text));
            log.info(text);
            // 第二轮对话
            // 将用户第二次的对话存储到ChatMemory中
            chatMemory.add(id, new UserMessage("我的工作是什么"));
            String text2 = chatModel.call(
                                        Prompt.builder()
                                        // 再次从ChatMemory中查找到所有存储的消息
                                        .messages(chatMemory.get(id))
                                        .build()
                                    )
                                    .getResult()
                                    .getOutput()
                                    .getText();
            chatMemory.add(id, new AssistantMessage(text2));                        
            log.info(text2);
    }

}
