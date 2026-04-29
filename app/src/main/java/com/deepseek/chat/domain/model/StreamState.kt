package com.deepseek.chat.domain.model

/**
 * 流的四种状态
 *
 * Idle       - 空闲，没有请求在进行中
 * Streaming  - 正在接收流式回复，partialContent 是已收到的部分内容
 * Error      - 请求失败，message 是错误信息
 */
sealed interface StreamState {
    data object Idle : StreamState
    data class Streaming(val partialContent: String) : StreamState
    data class Error(val message: String) : StreamState
}

/**
 * Token 用量统计（一次 API 调用的消耗）
 */
data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)
