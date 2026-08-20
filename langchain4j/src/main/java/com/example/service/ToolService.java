package com.example.service;

import org.springframework.stereotype.Service;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

@Service
public class ToolService {
    

    @Tool("根据用户名查询人数数量")
    public Integer getCountByName(@P("name") String name){
        return name.length();
    }

    @Tool("根据用户名获取名字长度")
    public Integer getLengthByName(@P("name") String name){
        return name.length()*2;
    }

}   
