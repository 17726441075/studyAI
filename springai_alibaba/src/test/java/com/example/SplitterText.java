package com.example;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = Main.class)
public class SplitterText {

    @Test
    public void TokenTextSplitterTest(@Value("classpath:doc/Spring+Mybatis复习答案.pdf") Resource resource) {
        List<Document> documents = new PagePdfDocumentReader(resource).read();

        TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder().build();
        List<Document> apply = tokenTextSplitter.apply(documents);

        log.info("原始读取的文档数："+documents.size()+"===="+"拆分后的文档数"+apply.size());
        for (Document document : apply) 
            log.info(document.toString());
    }
    
    @Test
    public void SentenceSplitterTest(@Value("classpath:doc/Spring+Mybatis复习答案.pdf") Resource resource) {
        List<Document> documents = new PagePdfDocumentReader(resource).read();
        
        SentenceSplitter sentenceSplitter = new SentenceSplitter(300);
        List<Document> apply = sentenceSplitter.apply(documents);
        
        log.info("原始读取的文档数："+documents.size()+"===="+"拆分后的文档数"+apply.size());
        for (Document document : apply) 
            log.info(document.toString());
    }

    @Test
    public void RecursiveCharacterTextSplitterTest(@Value("classpath:doc/Spring+Mybatis复习答案.pdf") Resource resource) {
        List<Document> documents = new PagePdfDocumentReader(resource).read();
        
        RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter();
        List<Document> apply = splitter.apply(documents);
        
        log.info("原始读取的文档数："+documents.size()+"===="+"拆分后的文档数"+apply.size());
        for (Document document : apply) 
            log.info(document.toString());
    }

}
