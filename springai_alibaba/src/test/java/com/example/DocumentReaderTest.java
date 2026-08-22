package com.example;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = Main.class)
public class DocumentReaderTest {

    // PDF读取演示
    @Test
    public void test01(@Value("classpath:doc/Spring+Mybatis复习答案.pdf") Resource resource) {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
        // 返回List<Document>，每个Document代表PDF的一页内容,每个Document包含提取出的文本内容和元数据
        for (Document document : reader.read()) 
            log.info(document.toString());
    }

    // Word文档读取演示取多种格式的文档
    @Test
    public void test02(@Value("classpath:doc/《大数据分析技术》课程课后练习题.docx") Resource resource){
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        for (Document document : reader.read()) 
            log.info(document.toString());
    }


}
