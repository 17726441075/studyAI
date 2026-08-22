package baize.code.java.service.impl;

import baize.code.java.code.ResultCode;
import baize.code.java.common.Result;
import baize.code.java.entity.Goods;
import baize.code.java.service.GoodsDocumentService;
import baize.code.java.utils.FileToDocuments;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import baize.code.java.entity.GoodsDocument;
import baize.code.java.mapper.GoodsDocumentMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static baize.code.java.code.DocumentCode.FILE_ID;
import static baize.code.java.code.DocumentCode.GOODS_ID;

@Service
@RequiredArgsConstructor
public class GoodsDocumentServiceImpl extends ServiceImpl<GoodsDocumentMapper, GoodsDocument> implements GoodsDocumentService {

    @Resource
    private FileToDocuments fileToDocuments;

    @Resource
    private final MilvusVectorStore vectorStore;

    @Override
    public List<GoodsDocument> getListByGoodsId(Integer id) {
        return lambdaQuery().eq(GoodsDocument::getGoodsId, id).list();
    }

    @Override
    public Result<?> upload(MultipartFile file, Integer goodsId) {
        //（1）根据不同文件类型选择合适文件读取分析器 Spring-AI官方提供了多种文件读取
        //将拆分好的文件进行向量化[对文档进行存储时自动调用向量化模型进行处理]
        //保存到向量数据库中
        List<Document> documents = fileToDocuments.handle(file);
        System.out.println(documents);
        //先将文档信息存储本地数据库 标识哪个商品对应有哪些文档
        GoodsDocument goodsDocument = GoodsDocument.builder().name(file.getOriginalFilename())
                .goodsId(goodsId)
                .build();
        if(!save(goodsDocument)) {
            return Result.error(ResultCode.UPLOAD_DOCUMENT_ERROR);
        }
        //将文档设置的元数据
        documents.forEach(d -> {
            //设置元数据 当前文档设定属性file_id 只文档ID
            //设置元数据 当前文档属于哪一件商品
            d.getMetadata().put(FILE_ID,goodsDocument.getId().toString());
            d.getMetadata().put(GOODS_ID,goodsId.toString());
        });
        //将文档存储到向量数据库中vectorStore 存储 每次只能处理10条
        for(int i = 0;i<documents.size(); i+=10){
            int endIndex = Math.min(i+10,documents.size());
            List<Document> batch = documents.subList(i, endIndex);
            vectorStore.add(batch);
        }

        return Result.success(ResultCode.ADD_SUCCESS,goodsDocument);
    }

    @Override
    public Result<?> delete(Integer id) {
       //查询删除的文档是否存在
        GoodsDocument fileInfo = getById(id);
        if(fileInfo == null) {
            return Result.error(ResultCode.DELETE_DOCUMENT_ERROR);
        }
        //根据文档ID删除数据库中记录
        if(!removeById(id)) {
            return Result.error(ResultCode.DELETE_DOCUMENT_ERROR);
        }
        //根据文件ID删除向量数据库中的数据
        //需要构建元数据过滤表达式
        vectorStore.delete(new Filter.Expression(Filter.ExpressionType.EQ,new Filter.Key(FILE_ID),new Filter.Value(id.toString())));
        return Result.success(ResultCode.DELETE_SUCCESS);
    }
}
