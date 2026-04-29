package com.deepseek.chat.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DeepSeek API 请求体（OpenAI 兼容格式）
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatRequestMessage>,
    val stream: Boolean = true,
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    @SerialName("presence_penalty")
    val presencePenalty: Double? = null,
    @SerialName("frequency_penalty")
    val frequencyPenalty: Double? = null,
    val seed: Int? = null,
    @SerialName("thinking_mode")
    val thinkingMode: String? = null,
    @SerialName("response_format")
    val responseFormat: ResponseFormat? = null
)

@Serializable
data class ChatRequestMessage(
    val role: String,
    val content: String
)

@Serializable
data class ResponseFormat(
    val type: String
)
