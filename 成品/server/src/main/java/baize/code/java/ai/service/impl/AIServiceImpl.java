package baize.code.java.ai.service.impl;

import baize.code.java.ai.adviser.CustomiZationMemeoryAdviser;
import baize.code.java.ai.service.AIService;
import baize.code.java.entity.Role;
import baize.code.java.entity.Session;
import baize.code.java.entity.SessionLog;
import baize.code.java.mapper.SessionMapper;
import baize.code.java.service.RoleService;
import baize.code.java.utils.KeyUtils;
import baize.code.java.websocket.endpoint.UserServiceEndpoint;
import baize.code.java.websocket.message.ChatMessage;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import jakarta.websocket.EncodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static baize.code.java.code.DocumentCode.GOODS_ID;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    @Autowired
    private ChatClient chatClient;

    @Autowired
    private DashScopeChatModel dashScopeChatModel;

    @Autowired
    private SessionMapper sessionMapper;
    @Autowired
    private CustomiZationMemeoryAdviser customiZationMemeoryAdviser;

    @Autowired
    private RoleService roleService;

    @Value("${session.key}")
    private String sessionKey;
    @Value("${session.expiration-duration}")
    private long sessionExpireTime;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private VectorStore vectorStore;

    @Value("classpath:template/convert-to-manual-judgment-prompts.st")
    private Resource contextResource;//转人工的提示词资源
    @Value("classpath:template/customer-service-role.st")
    private Resource customerServiceRoleResource;
    @Value("${retrieval.threshold}")
    private Double retrievalThreshold;
    @Value("classpath:template/relevant-information-cannot-be-retrieved.st")
    private Resource relevantInformationCannotBeRetrievedResource;

    @Value("${retrieval.number}")
    private Integer retrievalNum;

    @Override
    public void turnToManualJudgement(Session chatSession, ChatMessage message) {
        //添加转人工的判断逻辑
        String result = chatClient.prompt()
                .system(contextResource)
                .user(message.getMessage())
                .call()
                .content();
        //需要ai返回true或者false
        //将AI的结果转为Boolean类型
        boolean needTransfer = false;
        if(result!=null){
            needTransfer = Boolean.parseBoolean(result);
        }
        //如果需要转人工 改变会话状态中对话状态为HUMAN
      if(needTransfer){
          chatSession.setConversationStatus(Session.ConversationStatus.HUMAN);
          //将转人工的对话状态跟新在数据库中
          sessionMapper.updateById(chatSession);
          //将状态更新到redis中
          stringRedisTemplate.opsForValue().set(
                KeyUtils.redisKeyUtils(sessionKey,chatSession.getId()),
                JSONUtil.toJsonStr(chatSession),
                sessionExpireTime,
                TimeUnit.MINUTES
        );
      }
    }

    @Override
    public void chat(Session chatSession, ChatMessage message, UserServiceEndpoint userServiceEndpoint) throws IllegalAccessException, EncodeException, IOException {
        //查询商户设定的AI客服角色信息
        Role role = roleService.getById(chatSession.getCtId());
        //对提示词模版进行关键字的插入
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build()).resource(customerServiceRoleResource)
                .build();
        HashMap<String, Object> pos = new HashMap<>();
        //通过反射将role将需要填入的字段进行填入
        Class<? extends Role> aClass = role.getClass();
        for (Field declaredField : aClass.getDeclaredFields()) {
            if(declaredField.getType()==String.class){
                //如果是字符串则进行取值然后给提示词模板站位
                declaredField.setAccessible(true);
                pos.put(declaredField.getName(),declaredField.get(role));
            }
        }

        Flux<ChatResponse> chatResponse = chatClient.prompt().system(
                promptTemplate.render(pos))
                .user(message.getMessage())
                //传递会话ID作为记忆的唯一ID， 作用在于后去这一次会话获取以前会话记录
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,chatSession.getId()))
                .advisors(customiZationMemeoryAdviser,
                        RetrievalAugmentationAdvisor.builder()
                                .order(1)//确保在记忆存储之后执行
                                .documentRetriever(
                                        VectorStoreDocumentRetriever.builder()
                                                .similarityThreshold(retrievalThreshold) //设置相关度阈值 只有相似度大于阈值才会被获取
                                                .topK(retrievalNum)
                                                .filterExpression(new Filter.Expression(
                                                        Filter.ExpressionType.EQ,
                                                        new Filter.Key(GOODS_ID.toString()),
                                                        new Filter.Value(chatSession.getGoodsId().toString())
                                                )).vectorStore(vectorStore)
                                                .build()
                                ).queryAugmenter( //配置查询增强器
                                        ContextualQueryAugmenter.builder()
                                                .allowEmptyContext(false) //如果向量数据库为空 则运行将控制传递给大模型
                                                .emptyContextPromptTemplate(
                                                        PromptTemplate.builder()
                                                                .resource(relevantInformationCannotBeRetrievedResource)
                                                                .build()
                                                )
                                                .build()
                                ).queryTransformers(
                                        TranslationQueryTransformer.builder()
                                                .chatClientBuilder(ChatClient.builder(dashScopeChatModel))
                                        .targetLanguage("chinese") //目标语言
                                                .build())

                                .build()
                )
                .stream().chatResponse();// stream()流式调用 call阻塞式调用

        chatResponse.toIterable().forEach(chatResponse1 -> {
            //System.out.println(chatResponse1.getResult().getOutput().getText());
            //将AI回复的消息发送给客户
            try {
                userServiceEndpoint.sendMessage(ChatMessage.builder()
                        .sessionId(chatSession.getId())
                        .type(SessionLog.Type.ASSISTANT)
                        .message(chatResponse1.getResult().getOutput().getText())
                        .build());
            } catch (EncodeException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        userServiceEndpoint.sendMessage(ChatMessage.builder().state(ChatMessage.State.END).build());
    }
}
