package com.deepseek.chat.domain.model

/**
 * 所有可调节的 API 设置参数
 * 每个参数都有默认值，对应 DeepSeek V4 Pro 的推荐值
 */
data class ChatSettings(
    val model: String = "deepseek-v4-pro",
    val thinkingMode: ThinkingMode = ThinkingMode.NON_THINKING,
    val temperature: Float = 1.0f,
    val topP: Float = 1.0f,
    val maxTokens: Int = 4096,
    val presencePenalty: Float = 0.0f,
    val frequencyPenalty: Float = 0.0f,
    val seed: Int? = null,
    val stream: Boolean = true,
    val responseFormat: ResponseFormatType = ResponseFormatType.TEXT,
    val forbidTraining: Boolean = true
)
