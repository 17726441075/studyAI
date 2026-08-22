package baize.code.java.ai.adviser;

import baize.code.java.ai.memory.CustomizationMemory;
import baize.code.java.service.SessionService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CustomiZationMemeoryAdviser implements BaseChatMemoryAdvisor {
    @Resource
    private CustomizationMemory customizationMemory;
    @Resource
    private SessionService sessionService;
    private final PromptTemplate systemPromptTemplate = new PromptTemplate("{instructions}\n\nUse the conversation memory from the MEMORY section to provide accurate answers.\n\n---------------------\nMEMORY:\n{memory}\n---------------------\n\n");

    /**
     * 发送消息之前触发
     * 从聊天中提取发送的消息
     * 根据会话ID查询历史对话记录
     * 首先从Redis中查询 查最近的10条
     * 如果Redis没有 则从mysql中查询10条
     * 将当前的记忆同步到redis中
     * 将当前记忆整合到新的提示词中prompt
     * 返回增强的prompt给ChatClientRequest对象
     *
     * @param chatClientRequest
     * @param advisorChain
     * @return
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        Map<String, Object> context = chatClientRequest.context();
        //从请求上下文中获取会话ID
        String conversationId = getConversationId(chatClientRequest.context(), "default");
        if("default".equals(conversationId)) {
            throw new RuntimeException("会话ID不能为空");
        }
        //根据会话ID获取对应的上下文记忆
        List<Message> memoryMessage = customizationMemory.get(conversationId);
        //将记忆添加到新的提示词prompt中
        String memory = memoryMessage.stream().filter((m) -> m.getMessageType() == MessageType.USER || m.getMessageType() == MessageType.ASSISTANT).map((m) -> {
            // 将身份和对应的内容拼接
            String identity = String.valueOf(m.getMessageType());
            return identity + ":" + m.getText();
        }).collect(Collectors.joining(System.lineSeparator()));
        //获取系统提示词
        SystemMessage systemMessage = chatClientRequest.prompt().getSystemMessage();
        //将系统提示词和记忆整合到新的提示词中
        String newSystemText = this.systemPromptTemplate.render(Map.of("instructions", systemMessage, "memory", memory));
        //复制一个聊天请求出来 并将增加的提示词添加新的聊天中
        ChatClientRequest processChatClientRequest = chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentSystemMessage(newSystemText))
                .build();
        //提取用户的信息用于存储
        UserMessage userMessage = chatClientRequest.prompt().getUserMessage();
        this.customizationMemory.add(conversationId, userMessage);

        //将新的记忆进行返回
        return processChatClientRequest;

    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        //将AI返回的内容进行存储
        String conversationId = getConversationId(chatClientResponse.context(), "default");
        //从响应中提取AI的回复
       if("default".equals(conversationId)){
           throw new RuntimeException("会话ID不能为空");
       }
       //获取AI返回的消息
        ChatResponse chatResponse = chatClientResponse.chatResponse();
        if(chatResponse==null){
            throw new RuntimeException("AI返回的消息为空");
        }
        StringBuilder content = new StringBuilder();
        for (Generation result : chatResponse.getResults()) {
            content.append(result.getOutput().getText());
        }
        AssistantMessage assistantMessage = new AssistantMessage(content.toString());
        //将AI返回的消息存储在本地
         this.customizationMemory.add(conversationId,assistantMessage);
         return chatClientResponse;
    }

    //整合执行流程
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
      //先执行before方法
        ChatClientRequest precessedRequest = this.before(chatClientRequest, streamAdvisorChain);
        //继续下游的流式调用
        Flux<ChatClientResponse> chatClientResponseFlux = streamAdvisorChain.nextStream(precessedRequest);
        return new ChatClientMessageAggregator().aggregateChatClientResponse(
                chatClientResponseFlux,
                aggreatedResponse->{
                    this.after(aggreatedResponse,streamAdvisorChain);
                }
        );
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
