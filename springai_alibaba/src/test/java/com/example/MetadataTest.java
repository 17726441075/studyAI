package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = Main.class)
public class MetadataTest {
    
    @Test
    public void test01(@Autowired VectorStore milvusVectorStore) {
        List<String> batchTexts = new ArrayList<>();
        batchTexts.add("【2025新款】智能保温杯 316不锈钢 温度显示 24小时保温 白色 500ml");
        batchTexts.add("无线蓝牙耳机 半入耳式 降噪高清通话 续航40小时 适配安卓/苹果");

        List<String> batchTexts2 = new ArrayList<>();
        batchTexts2.add("机械键盘 青轴 104键 背光有线 电竞游戏专用 全键无冲 黑色");
        batchTexts2.add("家用投影仪 1080P高清 自动对焦 5G双频WiFi 兼容4K 白色 便携款");
        batchTexts2.add("电动牙刷 超声波清洁 5种模式 续航90天 软毛刷头 成人款 蓝色");

        List<Document> documents = batchTexts.stream()
                .map(document -> Document.builder()
                        .text(document)                   
                        .metadata("source", "商品")      
                        .build())
                .collect(Collectors.toList());

        List<Document> documents2 = batchTexts2.stream()
                .map(document -> Document.builder()
                        .text(document)
                        .metadata("source", "说明")      
                        .build())
                .collect(Collectors.toList());
        
        milvusVectorStore.write(documents);
        milvusVectorStore.write(documents2);
    }

    @Test
    public void test01WithMap(@Autowired VectorStore milvusVectorStore) {
        String text = "【2025新款】智能保温杯 316不锈钢";
        
        // 创建包含多个元数据的Map
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "商品");
        metadata.put("category", "杯具");
        metadata.put("department", "家居用品");
        metadata.put("date", "2025-01-01");
        
        Document document = Document.builder()
                .text(text)
                .metadata(metadata)  // 一次性添加多个元数据
                .build();
        
        milvusVectorStore.write(List.of(document));
    }

    @Test
    public void test02(@Autowired VectorStore milvusVectorStore) {
        String query = "无线蓝牙耳机";
        
        List<Document> documents = milvusVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)                              // 查询文本
                        .filterExpression("source=='商品'")       // 元数据过滤条件
                        .topK(10)                                 // 返回前10条结果
                        .build()
        );
        
        for (Document document : documents) 
            log.info(document.toString());
    }

}
