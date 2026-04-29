package com.deepseek.chat.ui.chat

import com.deepseek.chat.domain.model.Conversation
import com.deepseek.chat.domain.model.Message
import com.deepseek.chat.domain.model.StreamState
import com.deepseek.chat.domain.model.ThinkingMode
import com.deepseek.chat.domain.model.TokenUsage

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val streamState: StreamState = StreamState.Idle,
    val tokenUsage: TokenUsage? = null,
    val apiKeyConfigured: Boolean = false,
    val sendEnabled: Boolean = false,
    val showClearDialog: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val currentConversationId: String = "",
    val showDeleteDialog: String? = null,
    val thinkingMode: ThinkingMode = ThinkingMode.NON_THINKING
) {
    val isStreaming: Boolean get() = streamState is StreamState.Streaming

    val errorMessage: String? get() =
        (streamState as? StreamState.Error)?.message

    val currentConversation: Conversation?
        get() = conversations.find { it.id == currentConversationId }

    val historyConversations: List<Conversation>
        get() = conversations.sortedByDescending { it.updatedAt }

    val displayTitle: String
        get() = currentConversation?.title ?: "DeepSeek Chat"
}
