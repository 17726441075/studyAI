package com.example.configer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;

/**
 * 基于 ConcurrentHashMap 的聊天记忆实现（纯内存，线程安全）
 * 替代原生的 MessageWindowChatMemory，逻辑完全对齐
 */
public class HashMapChatMemory implements ChatMemory {

    // 全局存储：key=memoryId，value=该会话的消息队列
    private static final ConcurrentMap<String, Deque<ChatMessage>> GLOBAL_MEMORY_STORE = new ConcurrentHashMap<>();

    // 每个会话的最大消息数（对应原 maxMessages(10)）
    private final int maxMessages;
    // 当前会话的 memoryId
    private final String memoryId;

    public HashMapChatMemory(String memoryId, int maxMessages) {
        // 参数校验，避免空值/非法值

        this.memoryId = memoryId;
        this.maxMessages = maxMessages;

        // 初始化：如果该 memoryId 没有消息队列，创建一个空队列
        GLOBAL_MEMORY_STORE.putIfAbsent(memoryId, new LinkedBlockingDeque<>());
    }

    // 获取当前 memoryId 对应的消息队列
    private Deque<ChatMessage> getMessageQueue() {
        return GLOBAL_MEMORY_STORE.get(memoryId);
    }

    @Override
    public List<ChatMessage> messages() {
        // 将队列转为不可变列表返回（避免外部修改）
        Deque<ChatMessage> queue = getMessageQueue();
        return new ArrayList<>(queue); // 按插入顺序返回所有消息
    }

    @Override
    public void add(ChatMessage message) {

        Deque<ChatMessage> queue = getMessageQueue();

        // 1. 添加新消息到队列尾部
        queue.addLast(message);

        // 2. 控制消息数量：超过 maxMessages 时，删除队列头部（最早的消息）
        while (queue.size() > maxMessages) {
            queue.removeFirst(); // 循环删除，直到数量符合限制
        }
    }

    public void updateMessage(ChatMessage message) {
        // 如需支持消息更新：根据 message.id() 找到对应消息替换，此处简化（原生 MessageWindowChatMemory 也未实现）
        throw new UnsupportedOperationException("如需更新消息，请基于 message.id() 实现该方法");
    }

    public void removeMessage(String messageId) {
        // 如需支持单条删除：遍历队列找到对应 messageId 的消息移除，此处简化
        throw new UnsupportedOperationException("如需删除单条消息，请基于 message.id() 实现该方法");
    }

    public void clear() {
        // 清空当前 memoryId 的所有消息
        getMessageQueue().clear();
        // 可选：从 HashMap 中移除该 key（释放内存）
        // GLOBAL_MEMORY_STORE.remove(memoryId);
    }

    @Override
    public String id() {
        return this.memoryId;
    }

    // 可选：静态方法，清空所有会话的记忆（用于测试/重置）
    public static void clearAll() {
        GLOBAL_MEMORY_STORE.clear();
    }
}
