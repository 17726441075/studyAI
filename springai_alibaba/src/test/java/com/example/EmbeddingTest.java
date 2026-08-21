package com.example;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.alibaba.cloud.ai.dashscope.embedding.text.DashScopeEmbeddingModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = Main.class)
public class EmbeddingTest {

    @Test
    public void embeddingTest(@Autowired DashScopeEmbeddingModel dashScopeEmbeddingModel){
        // 1. 准备测试文本
        // 选用电商商品描述作为测试数据，贴近企业实际RAG应用场景
        String testText = "【2025新款】智能保温杯 316不锈钢材质 支持温度显示 长效保温24小时 便携车载款 白色 500ml";
        // 2. 调用Embedding模型进行向量化
        // embedForResponse()方法接收List<String>，返回EmbeddingResponse
        // 注意：参数必须是List类型，即使是单条文本也需要包装在List中
        EmbeddingResponse response = dashScopeEmbeddingModel.embedForResponse(List.of(testText));
        // 3. 获取向量结果
        // response.getResult()返回EmbeddingResult对象
        // getOutput()返回float数组，即文本的向量表示
        float[] embedding = response.getResult().getOutput();
        // 输出向量维度（用于验证）
        log.info("向量维度：" + embedding.length);
        // 输出向量内容（前10维，便于查看）
        log.info("向量前10维：" + Arrays.toString(Arrays.copyOf(embedding, 10)));
    }

}   
