package com.deepseek.chat.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * SSE 流中的一个数据块
 * DeepSeek 以 "data: {...}" 行格式发送，每个 chunk 包含 choices[0].delta.content
 */
@Serializable
data class ChatStreamChunk(
    val id: String = "",
    @SerialName("object")
    val `object`: String = "",
    val created: Long = 0,
    val model: String = "",
    val choices: List<StreamChoice> = emptyList(),
    val usage: Usage? = null
)

@Serializable
data class StreamChoice(
    val index: Int = 0,
    val delta: StreamDelta? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class StreamDelta(
    val role: String? = null,
    val content: String? = null,
    @SerialName("reasoning_content")
    val reasoningContent: String? = null
)
