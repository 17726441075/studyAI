package baize.code.java.ai.memory;

import baize.code.java.entity.SessionLog;
import baize.code.java.executor.GlobalTreadPool;
import baize.code.java.service.SessionLogService;
import baize.code.java.service.SessionService;
import baize.code.java.utils.KeyUtils;
import baize.code.java.utils.TypeConversion;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomizationMemory implements ChatMemory {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SessionLogService sessionLogService;

    @Value("${memory.key}")
    private String memoryKey;
    @Value("${memory.expiration-duration}")
    private Integer timeout;
    @Value("${memory.redis-length}")
    private Integer memoryLength;

    @Override
    public void add(String conversationId, List<Message> messages) {
        GlobalTreadPool.executor.execute(()->sessionLogService.add(conversationId,messages));
    }

    @Override
    public List<Message> get(String conversationId) {
       //return List.of();
        List<String> range = stringRedisTemplate.opsForList().range(KeyUtils.redisKeyUtils(memoryKey, conversationId), -memoryLength, -1);
        List<Message> messages = null;
        if(range==null || range.isEmpty()) {
            //redis中没有记忆
            //去数据库中查询
            List<SessionLog> sessionLogs = sessionLogService.tryGetSessionLog(conversationId);
            //将查询结果添加到redis中[单开线程]
            GlobalTreadPool.executor.execute(() -> sessionLogService.addToRedis(conversationId, sessionLogs));
            //将sessionLog转为Message返回
            messages = new ArrayList<>(sessionLogs.size());
            for (SessionLog sessionLog : sessionLogs) {
                Message message = TypeConversion.sessionToMessage(sessionLog.getType(), sessionLog.getContent());
                messages.add(message);
            }
        }else{
          messages = new ArrayList<>(range.size());
            for (String json : range) {
                SessionLog sessionLog = JSONUtil.toBean(json, SessionLog.class);
                Message message = TypeConversion.sessionToMessage(sessionLog.getType(), sessionLog.getContent());
                messages.add(message);
            }
        }
        return messages;
    }
    @Override
    public void clear(String conversationId) {
    }
}
