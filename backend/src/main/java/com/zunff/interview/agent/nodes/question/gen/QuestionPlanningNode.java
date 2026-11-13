package com.zunff.interview.agent.nodes.question.gen;

import com.zunff.interview.agent.state.BatchQuestionGenState;
import com.zunff.interview.config.PromptConfig;
import com.zunff.interview.model.bo.LevelMatchResult;
import com.zunff.interview.model.dto.llm.resp.QuestionPlanResponseDto;
import com.zunff.interview.service.extend.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 题目规划节点
 * 在 4 个并行生成节点之前运行，负责协调各类题目的主题分配和难度分配
 * 确保技术基础/项目经验/业务理解/软技能四类题目覆盖不同维度，避免重叠
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionPlanningNode {

    private final ChatClient textChatClient;
    private final PromptTemplateService promptTemplateService;
    private final PromptConfig promptConfig;

    /**
     * 执行题目规划
     */
    public CompletableFuture<Map<String, Object>> execute(BatchQuestionGenState state) {
        log.info("开始题目规划");

        try {
            String candidateProfile = state.candidateProfile();
            String jobContext = state.getJobContext();
            LevelMatchResult levelMatch = state.levelMatchResult();

            String positionLevel = levelMatch != null
                    ? levelMatch.positionLevel().getDescription()
                    : "Mid-Level";
            String candidateLevel = levelMatch != null
                    ? levelMatch.candidateLevel().getDescription()
                    : "Mid-Level";
            String difficultyRangeMin = levelMatch != null
                    ? levelMatch.difficultyRangeMin().getCode()
                    : "easy";
            String difficultyRangeMax = levelMatch != null
                    ? levelMatch.difficultyRangeMax().getCode()
                    : "hard";
            String difficultyPreference = levelMatch != null
                    ? levelMatch.difficultyPreference().getCode()
                    : "standard";

            Map<String, Object> promptVars = new HashMap<>();

            // 把这些信息通过Prompt交给大模型
            promptVars.put("responseLanguage", promptConfig.getResponseLanguage());
            promptVars.put("candidateProfile", candidateProfile != null ? candidateProfile : "");  // 候选人画像
            promptVars.put("jobInfo", jobContext != null ? jobContext : "");  // 岗位信息
            promptVars.put("technicalBasicCount", String.valueOf(state.getTechnicalBasicCount()));  // 四类题目数量
            promptVars.put("projectCount", String.valueOf(state.getProjectCount()));
            promptVars.put("businessCount", String.valueOf(state.getBusinessCount()));
            promptVars.put("softSkillCount", String.valueOf(state.getSoftSkillCount()));
            promptVars.put("positionLevel", positionLevel);  // 岗位级别
            promptVars.put("candidateLevel", candidateLevel);  // 候选人级别
            promptVars.put("difficultyRangeMin", difficultyRangeMin);  // 难度范围
            promptVars.put("difficultyRangeMax", difficultyRangeMax);
            promptVars.put("difficultyPreference", difficultyPreference);  // 难度偏好
            // 调用 question-planning Prompt
            String systemPrompt = promptTemplateService.getPrompt("question-planning", promptVars);
            String userPrompt = promptTemplateService.getPrompt("question-planning-user", promptVars);
            // 生成题目规划
            QuestionPlanResponseDto plan = textChatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(QuestionPlanResponseDto.class);

            log.info("题目规划完成: technicalTopics={}, projectTopics={}, businessTopics={}, softSkillTopics={}",
                    plan.technicalPlan() != null ? plan.technicalPlan().assignedTopics() : null,
                    plan.projectPlan() != null ? plan.projectPlan().assignedTopics() : null,
                    plan.businessPlan() != null ? plan.businessPlan().assignedTopics() : null,
                    plan.softSkillPlan() != null ? plan.softSkillPlan().assignedTopics() : null);

            Map<String, Object> updates = new HashMap<>();
            // 将规划放到共享状态当中 BatchQuestionGenState.QUESTION_PLAN
            // 后续四个生题节点在生题时，会从共享状态中读取对应类型的规划结果，拼接到各自的 Prompt 中
            updates.put(BatchQuestionGenState.QUESTION_PLAN, plan);
            return CompletableFuture.completedFuture(updates);

        } catch (Exception e) {
            log.error("题目规划失败，降级为无规划", e);
            // 降级：返回空规划，各生成节点按原有逻辑运行
            Map<String, Object> updates = new HashMap<>();
            updates.put(BatchQuestionGenState.QUESTION_PLAN, null);
            return CompletableFuture.completedFuture(updates);
        }
    }
}
