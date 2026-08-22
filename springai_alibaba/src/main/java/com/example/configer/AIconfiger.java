package com.example.configer;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankModel;
import com.alibaba.cloud.ai.memory.redis.JedisRedisChatMemoryRepository;
import com.example.service.ToolService;



@Configuration
public class AIconfiger {
    
    @Autowired
    private DashScopeChatModel dashScopeChatModel ;

    @Bean
    public ChatClient promptChatClient(){
        return ChatClient.builder(dashScopeChatModel)
                .defaultSystem("你是小J，你精通java领域的相关所有知识，用户提问时，你会深度思考并简要回答，不会说不符合事实的说！")         
                .build() ;
    }

    @Bean
    public ChatClient promptTemplateChatClient(){
        String render = PromptTemplate.builder()
                                      .template("你是小{zhiye}，你精通{part}领域的相关所有知识，用户提问时，你会深度思考并简要回答，不会说不符合事实的说！")
                                      .build()
                                      .render(Map.of("zhiye","P","part","python")); 
        return ChatClient.builder(dashScopeChatModel)
                .defaultSystem(render)
                .build() ;
    }

    
    @Bean
    public ChatClient promptTemplateResourceChatClient(@Value("classpath:prompt.yml")Resource resource){
        String render = PromptTemplate.builder()
                                      .resource(resource)
                                      .build()
                                      .render(Map.of("zhiye","C","part","C++")); 
        return ChatClient.builder(dashScopeChatModel)
                .defaultSystem(render)
                .build();
    }

    @Bean
    public ChatMemory redisChatMemory(@Value("${spring.data.redis.host}") String host,
                                 @Value("${spring.data.redis.port}") int port,
                                 @Value("${spring.data.redis.password}") String password){
        JedisRedisChatMemoryRepository redisChatMemoryRepository = JedisRedisChatMemoryRepository.builder()
                                                                                                .host(host) // 添加reids的主机
                                                                                                .port(port) // 添加redis的端口
                                                                                                // .user 配置了用户的需要在这里添加用户名
                                                                                                .password(password)
                                                                                                .build();
        return MessageWindowChatMemory.builder()
                                    .maxMessages(10) // 最大消息数
                                    .chatMemoryRepository(redisChatMemoryRepository)
                                    .build();
    }

    @Bean
    public ChatMemory jdbcChatMemory(@Autowired JdbcChatMemoryRepository jdbcChatMemoryRepository){
        return MessageWindowChatMemory.builder()
                                    .maxMessages(10) // 最大消息数
                                    .chatMemoryRepository(jdbcChatMemoryRepository)
                                    .build();
    }

    @Bean
    public ChatClient advisorChatClient(@Autowired JdbcChatMemoryRepository jdbcChatMemoryRepository){
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                                                        .maxMessages(10) // 最大消息数
                                                        .chatMemoryRepository(jdbcChatMemoryRepository)
                                                        .build();
        return ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(
                    // 使用PromptChatMemoryAdvisor，这个Advisor是专门用于适配ChatMemory对象的对话记忆拦截器
                    PromptChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @Bean
    public ChatClient digestClient(@Autowired JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                                                        .maxMessages(10) // 最大消息数
                                                        .chatMemoryRepository(jdbcChatMemoryRepository)
                                                        .build();
        return ChatClient.builder(dashScopeChatModel)
                        .defaultAdvisors(
                            PromptChatMemoryAdvisor.builder(chatMemory).build()
                        )
                        .defaultSystem("""
                                    你是一位专业的对话摘要专家，任务是阅读完整的对话历史（用户与AI的往复消息），然后生成一段**简洁、连贯、包含关键意图和事实**的摘要。

                                    核心要求：
                                    1. 优先保留**用户的核心目标、问题、需求、情感倾向**和**关键事实/决定**，而非AI的礼貌回复或重复解释。
                                    2. 按时间顺序组织内容，体现对话的进展和演变。
                                    3. 使用**第三人称叙述**，语言客观、中性、精炼。
                                    4. 突出**未解决的问题、待办事项、用户反复强调的点**，以及**最新的用户意图**。
                                    5. 长度控制在 **80–300 字**（中文），尽量压缩但不丢失重要上下文。
                                    6. **不要添加**任何你自己的判断、建议或额外信息，只总结已有对话内容。
                                    7. 输出**仅包含摘要正文**，不要出现“摘要：”、“以下是摘要”等前缀。

                                    示例输出风格：
                                    用户正在开发一个Spring AI聊天机器人，需要实现带摘要的长期记忆机制。他询问了MessageWindowChatMemory的局限性，并希望结合token窗口和自动摘要来控制上下文长度。目前讨论到了自定义SummarizingChatMemory的实现思路，以及使用Advisor在prompt构建前插入摘要的方案。用户倾向于用一个小模型专门做摘要以节省成本，下一步计划实现一个带重要性打分的遗忘策略。
                                    """)
                        .build();
    }

    @Bean
    public ChatClient piiChatClient(@Autowired DashScopeChatModel dashScopeChatModel) {  
        return ChatClient.builder(dashScopeChatModel)
                        .defaultSystem("""
                                    你是PII检测器。只识别并替换文本中的个人敏感信息。
                                    规则：
                                    - 姓名 → [姓名]
                                    - 邮箱 → [邮箱]
                                    - 手机号 → [手机号]
                                    - 身份证号 → [身份证]
                                    - 地址 → [地址]
                                    - 其他明显PII → [敏感信息]
                                    - 输出只返回处理后的完整文本，不要解释。
                                    """)
                        .build();
    }

    @Bean
    public ChatClient piiAdvisorChatClient(@Autowired JdbcChatMemoryRepository jdbcChatMemoryRepository,@Autowired PiiAdvisor piiAdvisor){
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                                                        .maxMessages(10) // 最大消息数
                                                        .chatMemoryRepository(jdbcChatMemoryRepository)
                                                        .build();
        return ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(
                    // 使用PromptChatMemoryAdvisor，这个Advisor是专门用于适配ChatMemory对象的对话记忆拦截器
                    PromptChatMemoryAdvisor.builder(chatMemory).build(),piiAdvisor
                )
                .build();
    }


    @Autowired
    private VectorStore milvusVectorStore;

    @Autowired
    private DashScopeRerankModel dashScopeRerankModel;

    @Bean
    public ChatClient retrievalChatClient() {
        return ChatClient.builder(dashScopeChatModel)
                            .defaultAdvisors(
                                    RetrievalAugmentationAdvisor.builder()
                                                                .queryTransformers(
                                                                    RewriteQueryTransformer.builder()
                                                                            .chatClientBuilder(ChatClient.builder(dashScopeChatModel))
                                                                            .targetSearchSystem("你是一个词汇清理的专家，主要工作是将用户的模糊问题提取出专业的词汇，以提高向量检索的精度，注意不要有任何多余的解释")
                                                                            .build()
                                                                )
                                                                .documentRetriever(
                                                                    VectorStoreDocumentRetriever.builder()
                                                                            .similarityThreshold(.5)  
                                                                            .vectorStore(milvusVectorStore)
                                                                            .build()
                                                                )
                                                                .queryAugmenter(
                                                                    ContextualQueryAugmenter.builder()
                                                                            .allowEmptyContext(false)
                                                                            .emptyContextPromptTemplate(
                                                                                PromptTemplate.builder()
                                                                                        .template("根据您的问题，系统未能找到相关的文档信息。为了更好地帮助您，请提供更多详细信息或尝试重新表述您的问题。")
                                                                                        .build()
                                                                            )
                                                                            .build()
                                                                )
                                                                .build(),
                                                                new RetrievalRerankAdvisor(milvusVectorStore, dashScopeRerankModel,
                                                                    SearchRequest.builder()
                                                                            .topK(200)
                                                                            .similarityThreshold(.4)
                                                                            .build())
                                                                )
                            .build();
    }

    @Bean
    public ChatClient toolChatClient(@Autowired ToolService toolService) {
        return ChatClient.builder(dashScopeChatModel)
                .defaultTools(toolService)    // 注册@Tool注解的工具
                .build();
    }

}
