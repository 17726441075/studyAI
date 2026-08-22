package baize.code.java.websocket.endpoint;

import baize.code.java.ai.service.AIService;
import baize.code.java.config.ChatMessageCoder;
import baize.code.java.mapper.SessionLogMapper;
import baize.code.java.service.SessionService;
import baize.code.java.utils.SessionFind;
import baize.code.java.websocket.message.ChatMessage;
import baize.code.java.entity.SessionLog;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/user/chat/{userId}", decoders = ChatMessageCoder.class, encoders = ChatMessageCoder.class)
public class UserServiceEndpoint implements WebSocketEndpoint {
    private static SessionService sessionService;
    private static SessionFind sessionFind;
    private static AIService aiService;
    private static final ConcurrentHashMap<Integer, UserServiceEndpoint> userEndpointPool = new ConcurrentHashMap<>();

    private Session session;
    private Integer userId;
    private static SessionLogMapper sessionLogMapper;

    @Autowired
    public void setDependencies( AIService aiService,SessionLogMapper sessionLogMapper,SessionFind sessionFind,SessionService sessionService){
        UserServiceEndpoint.aiService = aiService;
        UserServiceEndpoint.sessionService = sessionService;
        UserServiceEndpoint.sessionFind = sessionFind;
        UserServiceEndpoint.sessionLogMapper = sessionLogMapper;
    }

    @OnOpen //建立连接时触发
    public void onOpen(Session session, @PathParam("userId") Integer userId) {
        this.session = session;
        this.userId = userId;
        userEndpointPool.put(userId, this);
    }



    @OnClose//关闭连接时触发
    public void onClose() {
        if (userId != null) {
            userEndpointPool.remove(userId);
        }
    }

    // TODO:005
    @OnMessage//发送消息时触发
    public void onMessage(ChatMessage message, Session session) throws EncodeException, IOException, IllegalAccessException {
        message.setType(getEndpointType());
        //向商户发送消息
        //开启会话 【针对哪一个商家 哪一个用户 哪一个商品的一个会话】
        baize.code.java.entity.Session chatSession = sessionService.find(message, userId, this);
        switch (chatSession.getConversationStatus()) {
            case AI->{
                //判断当前用户发送的消息是否有转人工意愿
                aiService.turnToManualJudgement(chatSession,message);
                //根据会话状态
                if(chatSession.getConversationStatus()== baize.code.java.entity.Session.ConversationStatus.HUMAN){
                    //被AI识别需要强行转人工
                    //找到商户的端点
                    CommercialTenantEndpoint tenantEndpointById = sessionFind.getTenantEndpointById(message.getSessionId());
                    if(tenantEndpointById!=null){
                        tenantEndpointById.sendMessage(message); //将消息发送给商户
                    }
                }else{
                    //正式走AI对话
                    aiService.chat(chatSession,message,this);
                }
            }
            case HUMAN->{
                //将消息存入数据库中 只有人工的需要手动存储，这里不需要经过redis
                 sessionLogMapper.insert(SessionLog.builder()
                        .type(message.getType())
                        .sessionId(message.getSessionId())
                        .content(message.getMessage())
                        .build());

                //找到商户的端点
                CommercialTenantEndpoint tenantEndpointById = sessionFind.getTenantEndpointById(message.getSessionId());
                if(tenantEndpointById!=null){
                    tenantEndpointById.sendMessage(message); //将消息发送给商户
                }
            }
        }
    }

    @OnError//消息发送错误时触发
    public void onError(Session session, Throwable error) {
        try {
            error.printStackTrace();
            session.getBasicRemote().sendObject(ChatMessage.builder()
                    .state(ChatMessage.State.ERROR)
                    .message(error.getMessage())
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 封装发送消息的方法
     */
    public void sendMessage(ChatMessage chatMessage) throws EncodeException, IOException {
        this.session.getBasicRemote().sendObject(chatMessage);
    }

    @Override
    public SessionLog.Type getEndpointType() {
        return SessionLog.Type.USER;
    }

    public static UserServiceEndpoint findEndPoint(Integer userId) {
        return userEndpointPool.get(userId);
    }
}
