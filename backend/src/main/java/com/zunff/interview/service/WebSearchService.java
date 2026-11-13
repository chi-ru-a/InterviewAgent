package com.zunff.interview.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


/**
 * RestClient：新一代的同步 HTTP 客户端【spring 6.1 引入】，用来发起 RESTful API 调用
 * 之前：RestTemplate 或 WebClient（WebClient 是响应式的，适合异步场景，写同步别扭）
 * 现在：RestClient 是同步的，适合传统的阻塞式调用场景，简化了配置和使用
 * 适合在 Spring Boot 3.x 中
 * 1. RestClient 是 Spring 6.1 引入的同步 HTTP 客户端，简化了 RESTful API 调用的配置和使用。
 * 2. RestClient 提供了更现代的 API，支持链式调用和更灵活的请求构建方式。
 * 3. RestClient 内部使用了 HttpClient 或 OkHttp 等底层 HTTP 客户端库，提供了更好的性能和可扩展性。
 */
@Slf4j
@Service
public class WebSearchService {

    private static final String TAVILY_API_URL = "https://api.tavily.com/search";

    private final String apiKey;
    private final boolean enabled;
    private final RestClient restClient;

    public WebSearchService(@Value("${search.tavily.api-key:}") String apiKey,
                            @Value("${search.tavily.enabled:false}") boolean enabled) {
        this.apiKey = apiKey;
        this.enabled = enabled;
        this.restClient = RestClient.builder().baseUrl(TAVILY_API_URL).build();
    }

    public String search(String query) {
        log.info("联网搜索: {}", query);

        if (!enabled || apiKey == null || apiKey.isBlank()) {
            log.warn("联网搜索功能未启用（缺少 TAVILY_API_KEY）");
            return null;
        }

        try {
            // 使用 JSONUtil 组装请求体
            String body = JSONUtil.createObj()
                    .set("api_key", apiKey)
                    .set("query", query)  // 查询词、搜索内容
                    /**
                     * basic：轻量搜索，返回内容摘要，与查询的相关性一般；快速搜索、便宜、返回内容摘要
                     * advanced：深度搜索，返回搜索结果中最相关的片段，而不是摘要；高精度搜索、贵、返回更详细的内容
                     */
                    .set("search_depth", "basic")  // 搜索深度，basic / advanced
                    .set("max_results", 5)  // 最大结果数 5条
                    .set("include_answer", true)  // 返回摘要
                    .toString();

            // 使用 RestClient 向 TAVILY API 发送 POST 请求，并拿到结果
            String response = restClient.post()
            // MediaType 在发起 HTTP 请求或处理响应时，用来明确“我发送的数据是什么格式”（如 JSON、XML、表单）或 “我能接收什么格式”
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);  // 把第三方 API 返回的响应体，直接作为字符串拿到

            // 解析结果
            String summary = extractSummary(response);
            log.info("搜索完成, 结果长度: {}", summary.length());
            return summary;
        } catch (Exception e) {
            log.error("联网搜索失败: {}", e.getMessage(), e);
            return null;
        }
    }
·
    /**
     * 把解析后的这段纯文本，作为背景知识（Context）拼接到给大语言模型的 Prompt（提示词）里
     * 大模型非常擅长阅读这种干净的 Markdown 格式文本，从而基于最新的联网信息来生成问题
     */
    private String extractSummary(String response) {
        try {
            JSONObject json = JSONUtil.parseObj(response);

            StringBuilder sb = new StringBuilder();


            // 如果 Tavily 返回了 AI 生成的 answer（针对搜索词的直接回答），就把它放在最前面，并加两个换行
            String answer = json.getStr("answer");
            if (answer != null && !answer.isBlank()) {
                sb.append(answer).append("\n\n");
            }

            // 遍历 results 数组，提取每一个搜索结果的网页信息
            JSONArray results = json.getJSONArray("results");
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    JSONObject item = results.getJSONObject(i);
                    // 标题，前面加个短横线
                    sb.append("- ").append(item.getStr("title", "")).append("\n");
                    // 正文内容，截取前 200 个字符，超过的加省略号
                    String content = item.getStr("content", "");
                    if (content.length() > 200) {
                        content = content.substring(0, 200) + "...";
                    }
                    sb.append("  ").append(content).append("\n");
                    // URL，前面加两个空格
                    sb.append("  ").append(item.getStr("url", "")).append("\n\n");
                }
            }

            return sb.toString();
        } catch (Exception e) {
            log.warn("解析搜索结果失败，返回原始响应", e);
            return response;
        }
    }
}