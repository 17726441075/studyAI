package com.example.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.access.prepost.PreAuthorize;
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


    /**
     * 用户信息查询工具
     * 敏感操作，需要管理员权限
     *
     * @PreAuthorize 注解：
     * - hasRole('ADMIN')：仅允许具有ADMIN角色的用户调用
     * - 在方法执行前进行权限校验
     *
     * 如果当前用户不是管理员，会抛出AccessDeniedException
     */
    @Tool(name = "get_user_info", description = "根据用户名称查询对应的用户信息")
    @PreAuthorize("hasRole('ADMIN')")  // 权限校验注解
    public String getUserInfo(@ToolParam(description = "要查询的用户名称") String username) {
        return String.format(
                "【用户信息】用户名：%s，邮箱：%s@company.com，手机号：138****8901",
                username, username
        );
    }

}