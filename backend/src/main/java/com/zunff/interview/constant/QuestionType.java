package com.zunff.interview.constant;

import lombok.Getter;

/**
 * 问题类型枚举
 */
@Getter
public enum QuestionType {

    /**
     * 枚举值、值当中的字段写为 private final 属性、全参构造、getter 方法
     * 可以有别的方法，比如通过自定义方法，根据某个字段查找对应的枚举值【通过 values() 遍历查找】
     */

    // ========== 技术轮问题类型 ==========
    TECHNICAL_BASIC(1, "technical_basic", "技术基础", "technical"),
    PROJECT_EXPERIENCE(2, "project_experience", "项目经验", "project"),
    TECHNICAL_CHALLENGE(3, "technical_challenge", "技术难点", "technical"),
    SYSTEM_DESIGN(4, "system_design", "系统设计", "technical"),

    // ========== 业务轮问题类型 ==========
    BUSINESS_UNDERSTANDING(5, "business_understanding", "业务理解", "business"),
    COMMUNICATION(6, "communication", "沟通协作", "soft"),
    PROBLEM_SOLVING(7, "problem_solving", "场景分析", "business"),
    PROFESSIONALISM(8, "professionalism", "职业素养", "soft"),

    // ========== 追问类型 ==========
    FOLLOW_UP(9, "follow_up", "追问", "followup-basic"),
    CHALLENGE_QUESTION(10, "challenge_question", "挑战题", "followup-challenge"),
    DEEP_DIVE(11, "deep_dive", "深入追问", "followup-deep-dive"),
    ;

    private final int code;
    private final String apiName;
    private final String displayName;
    private final String evaluationPrompt;

    QuestionType(int code, String apiName, String displayName, String evaluationPrompt) {
        this.code = code;
        this.apiName = apiName;
        this.displayName = displayName;
        this.evaluationPrompt = evaluationPrompt;
    }




    /**
     * 根据显示名称获取问题类型
     */
    public static QuestionType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return TECHNICAL_BASIC;
        }
        String normalized = displayName.trim().toLowerCase().replace('-', '_');
        for (QuestionType type : values()) {
            if (type.displayName.equals(displayName)
                    || type.apiName.equalsIgnoreCase(normalized)
                    || type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return TECHNICAL_BASIC;
    }

    public static QuestionType fromCode(Integer code) {
        if (code == null) {
            return TECHNICAL_BASIC;
        }
        // values() 方法返回枚举类的所有枚举值，遍历这些值，找到与给定 code 匹配的枚举类型
        // values() 是在编译时，Java 为每一个枚举类自动生成的一个静态方法
        for (QuestionType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return TECHNICAL_BASIC;
    }

    /**
     * 判断是否为技术轮问题类型
     */
    public boolean isTechnicalType() {
        return this == TECHNICAL_BASIC
                || this == PROJECT_EXPERIENCE
                || this == TECHNICAL_CHALLENGE
                || this == SYSTEM_DESIGN;
    }

    /**
     * 判断是否为业务轮问题类型
     */
    public boolean isBusinessType() {
        return this == BUSINESS_UNDERSTANDING
                || this == COMMUNICATION
                || this == PROBLEM_SOLVING
                || this == PROFESSIONALISM;
    }

    /**
     * 判断是否为追问类型
     */
    public boolean isFollowUpType() {
        return this == FOLLOW_UP
                || this == CHALLENGE_QUESTION
                || this == DEEP_DIVE;
    }
}