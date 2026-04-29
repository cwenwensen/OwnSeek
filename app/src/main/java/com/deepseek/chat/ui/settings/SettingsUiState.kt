package com.deepseek.chat.ui.settings

import com.deepseek.chat.domain.model.ChatSettings
import com.deepseek.chat.domain.model.ResponseFormatType
import com.deepseek.chat.domain.model.ThinkingMode

/**
 * 设置页面的 UI 状态
 *
 * 除了 ChatSettings 的所有字段外，额外包含：
 * - apiKey: API 密钥明文
 * - seedText: 种子文本（String，需要解析为 Int?）
 * - seedError: 种子解析错误
 */
data class SettingsUiState(
    val apiKey: String = "",
    val model: String = "deepseek-v4-pro",
    val thinkingMode: ThinkingMode = ThinkingMode.NON_THINKING,
    val temperature: Float = 1.0f,
    val topP: Float = 1.0f,
    val maxTokens: Float = 4096f,      // 滑块用 Float，提交时转 Int
    val presencePenalty: Float = 0.0f,
    val frequencyPenalty: Float = 0.0f,
    val seedText: String = "",
    val seedError: String? = null,
    val stream: Boolean = true,
    val responseFormat: ResponseFormatType = ResponseFormatType.TEXT
)

/** 从领域模型构建初始 UI 状态 */
fun ChatSettings.toUiState(apiKey: String): SettingsUiState = SettingsUiState(
    apiKey = apiKey,
    model = model,
    thinkingMode = thinkingMode,
    temperature = temperature,
    topP = topP,
    maxTokens = maxTokens.toFloat(),
    presencePenalty = presencePenalty,
    frequencyPenalty = frequencyPenalty,
    seedText = seed?.toString() ?: "",
    stream = stream,
    responseFormat = responseFormat
)
