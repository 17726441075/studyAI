package baize.code.java.utils;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 将文件转换为文档
 */
@Component
public class FileToDocuments {

    public List<Document> handle(MultipartFile file) {
        //调用方法将不同文件调用合理的读取器解析为Document对象
        FileHandler classifier = classifier(file);
        //调用读取器的run方法将文件转换为文档
        return classifier.run(file);
    }

    private FileHandler classifier(MultipartFile file){
        //根据文件名的后缀选择合适的读取器进行文件的读取
        String originalFilename = file.getOriginalFilename();
        if(originalFilename==null){
            throw new RuntimeException("文件名不能为空");
        }
        //获取后缀
        //xxx.txt
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        //根据后缀选择合适的读取器
        switch (extension) {
            case "md":
                return new MarkdownFileHandler();
            case "pdf":
                return new PdfFileHandler();
            case "txt":
                return new TextFileHandler();
            default:
                throw new RuntimeException("不支持的文件类型"+extension);
        }
    }

    public interface FileHandler {
        List<Document> run(MultipartFile multipartFile);
    }

    //针对不同的文件读取器【MD PDF TXT...】 都是实现这个方法 将功能都编写在run方法中
    private static class MarkdownFileHandler implements FileHandler {
        @Override
        public List<Document> run(MultipartFile multipartFile) {
            //使用Spring-AI官方提供的Markdown文件读取器
            //MarkdownDocumentReader
            MarkdownDocumentReaderConfig build = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(false) //不按照分各项创建新的document对象
                    .withIncludeCodeBlock(false) //不按照代码块创建新的document对象
                    .build();
            Resource byteArrayResource = null;
            try {
                //将文件上传对象转为Resource
                byteArrayResource = new ByteArrayResource(multipartFile.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //通过配置创建MD文档读取器
            MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(byteArrayResource, build);//构建MD的文档读取器 需要传入对应的文件
            List<Document> read = markdownDocumentReader.read();
            // 对文档进行切分
            read = ChineseTokenTextSplitter.quicklyBuilder().split(read);
            return read;
        }
    }

    private static class PdfFileHandler implements FileHandler {
        @Override
        public List<Document> run(MultipartFile file) {
            // 使用Spring AI的PdfDocumentReader --- 这里使用的是一页一个document
            // 将MultipartFile包装为Resource
            Resource resource;
            try {
                resource = new ByteArrayResource(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("读取文件失败", e);
            }
            PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
            List<Document> documents = reader.read();
            // 对文档进行切分
            documents = ChineseTokenTextSplitter.quicklyBuilder().split(documents);
            return documents;
        }
    }

    private static class TextFileHandler implements FileHandler {
        @Override
        public List<Document> run(MultipartFile file) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                List<Document> documents = new ArrayList<>(1);
                documents.add(new Document(content.toString()));
                // 对文档进行切分
                documents = ChineseTokenTextSplitter.quicklyBuilder().split(documents);
                return documents;
            } catch (IOException e) {
                throw new RuntimeException("读取文本文件失败", e);
            }
        }
    }
}
