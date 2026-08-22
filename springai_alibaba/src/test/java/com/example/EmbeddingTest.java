package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.VectorStore;
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

    // 存储
    @Test
    public void milvusVectorStoreTest(@Autowired VectorStore milvusVectorStore) {
        List<String> batchTexts = new ArrayList<>();
        batchTexts.add("【2025新款】智能保温杯 316不锈钢 温度显示 24小时保温 白色 500ml");
        batchTexts.add("无线蓝牙耳机 半入耳式 降噪高清通话 续航40小时 适配安卓/苹果");
        batchTexts.add("机械键盘 青轴 104键 背光有线 电竞游戏专用 全键无冲 黑色");
        batchTexts.add("家用投影仪 1080P高清 自动对焦 5G双频WiFi 兼容4K 白色 便携款");
        batchTexts.add("电动牙刷 超声波清洁 5种模式 续航90天 软毛刷头 成人款 蓝色");

        List<Document> documents = batchTexts.stream()
                                            .map(Document::new)
                                            .collect(Collectors.toList());
        // 在这里将我们之前的文档直接调用wriete方法写入到miluvs中
        milvusVectorStore.write(documents);
    }

    // 检索
    @Test
    public void milvusVectorStoreTest2(@Autowired VectorStore milvusVectorStore) {
        List<Document> similaritySearch = milvusVectorStore.similaritySearch("机械键盘"); 
        similaritySearch.forEach(doc->{
            log.info(doc.toString());
        });
    }
}   
