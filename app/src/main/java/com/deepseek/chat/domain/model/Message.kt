package com.deepseek.chat.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 一条聊天消息
 * @param role USER = 用户消息, ASSISTANT = AI 回复
 * @param content 消息文本内容
 * @param tokens 该消息消耗的 token 数（仅 ASSISTANT 消息有值）
 */
@Serializable
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val reasoningContent: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val tokens: Int? = null
)

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT
}
