package com.zunff.interview.controller;

import com.zunff.interview.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统健康检查和状态控制器
 * 返回的是死数据
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor  // lombok生成构造器
@Tag(name = "系统管理", description = "系统健康检查和状态接口")  // swagger 给整个控制器分组，主要影响 Swagger UI 页面的展示
public class AgentController {

    @Operation(summary = "健康检查", description = "检查服务是否正常运行")  // swagger 描述单个 API 接口方法的注解  细化到控制器中的每一个具体的接口
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {  // ApiResponse<Map<String, Object>> 自定义的响应封装
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "interview-agent"
        ));
    }

    @Operation(summary = "获取服务信息", description = "获取服务的基本信息和功能列表")
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> info() {
        return ApiResponse.success(Map.of(
                "name", "Interview Agent Service",
                "version", "1.0.0",
                "description", "基于 Spring AI + LangGraph4j 的实时视频面试系统",
                "features", new String[]{
                        "实时视频面试",
                        "多模态分析（视频/音频/文本）",
                        "智能问题生成与追问",
                        "综合评估报告"
                }
        ));
    }
}