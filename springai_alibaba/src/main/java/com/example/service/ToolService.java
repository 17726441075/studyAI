package com.example.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class ToolService {

    /**
     * @Tool 注解说明：
     * - name: 工具名称，AI模型通过此名称识别和调用工具
     * - description: 工具描述，帮助大模型理解工具的用途和使用场景
     *
     * @ToolParam 注解说明：
     * - description: 参数描述，帮助模型理解参数的含义
     * - required: 参数是否为必填项（默认为true）
     */
    @Tool(name = "get_weather_by_annotation", description = "根据城市名称查询当前天气，支持国内外城市查询")
    public String getWeather(@ToolParam(description = "要查询的城市名称（如北京、上海、东京、纽约）") String city) {
        return String.format("【注解方式】%s 当前天气：晴，温度26℃，湿度60%%", city);
    }
}