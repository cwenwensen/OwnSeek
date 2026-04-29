package com.deepseek.chat.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "新对话",
    val messages: List<Message> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** 该对话中所有消息消耗的 token 总数 */
    val totalTokens: Int
        get() = messages.sumOf { it.tokens ?: 0 }
}
