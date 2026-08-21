package com.example.configer;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.ai.dashscope.embedding.text.DashScopeEmbeddingModel;

import io.milvus.client.MilvusServiceClient;

@Configuration
public class VectorStoreConfig {
    @Autowired
    private DashScopeEmbeddingModel dashScopeEmbeddingModel;
    
    @Autowired
    private MilvusServiceClient milvusServiceClient;
    
    @Bean
    public VectorStore milvusVectorStore() {
        // 将miluvs的客户端和想要使用的向量模型组合成为一个VectorStore注入到Spring容器中
        return MilvusVectorStore.builder(milvusServiceClient, dashScopeEmbeddingModel).build();
    }
}
