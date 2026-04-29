package com.deepseek.chat.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepseek.chat.DeepSeekApp
import com.deepseek.chat.data.api.dto.ChatCompletionRequest
import com.deepseek.chat.data.api.dto.ChatRequestMessage
import com.deepseek.chat.di.AppDependencies
import com.deepseek.chat.domain.model.Conversation
import com.deepseek.chat.domain.model.Message
import com.deepseek.chat.domain.model.MessageRole
import com.deepseek.chat.domain.model.StreamState
import com.deepseek.chat.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel : ViewModel() {

    private val dataStore = DeepSeekApp.instance.settingsDataStore
    private val conversationStorage = DeepSeekApp.instance.conversationStorage
    private val repository: ChatRepository = AppDependencies.chatRepository
    private val apiService = AppDependencies.apiService

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null
    private var titleGenerated = false

    init {
        viewModelScope.launch {
            dataStore.apiKey.collect { key ->
                _uiState.update { state ->
                    val hasKey = key.isNotBlank()
                    state.copy(
                        apiKeyConfigured = hasKey,
                        sendEnabled = hasKey && state.streamState is StreamState.Idle
                    )
                }
            }
        }
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            val all = conversationStorage.loadAll()
            _uiState.update { state ->
                if (all.isNotEmpty()) {
                    val current = all.first()
                    state.copy(
                        conversations = all,
                        currentConversationId = current.id,
                        messages = current.messages,
                        sendEnabled = state.apiKeyConfigured
                    )
                } else {
                    val newId = ""
                    state.copy(
                        conversations = all,
                        currentConversationId = newId,
                        messages = emptyList()
                    )
                }
            }
        }
    }

    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return

        val currentState = _uiState.value
        if (currentState.isStreaming) return

        val userMessage = Message(role = MessageRole.USER, content = trimmed)
        val updatedMessages = currentState.messages + userMessage

        _uiState.update {
            it.copy(
                messages = updatedMessages,
                streamState = StreamState.Streaming(""),
                sendEnabled = false,
                tokenUsage = null
            )
        }

        // 第一次发送消息时重置标题生成标志
        if (currentState.messages.isEmpty()) {
            titleGenerated = false
        }

        streamingJob = viewModelScope.launch {
            val settings = dataStore.chatSettings.first()

            // 更新当前生效的思考模式到 UI 状态
            _uiState.update { it.copy(thinkingMode = settings.thinkingMode) }

            repository.streamCompletion(updatedMessages, settings).collect { event ->
                when (event) {
                    is ChatRepository.StreamEvent.Delta -> {
                        _uiState.update { state ->
                            val messages = state.messages.toMutableList()
                            val lastMsg = messages.lastOrNull()
                            if (lastMsg?.role == MessageRole.ASSISTANT) {
                                messages[messages.lastIndex] =
                                    lastMsg.copy(content = lastMsg.content + event.content)
                            } else {
                                messages.add(
                                    Message(role = MessageRole.ASSISTANT, content = event.content)
                                )
                            }
                            state.copy(
                                messages = messages,
                                streamState = StreamState.Streaming(
                                    messages.lastOrNull { it.role == MessageRole.ASSISTANT }?.content ?: ""
                                )
                            )
                        }
                    }
                    is ChatRepository.StreamEvent.ReasoningDelta -> {
                        // 非思考模式时忽略推理内容
                        if (_uiState.value.thinkingMode == com.deepseek.chat.domain.model.ThinkingMode.NON_THINKING) {
                            // 跳过推理内容
                        } else {
                            _uiState.update { state ->
                                val messages = state.messages.toMutableList()
                                val lastMsg = messages.lastOrNull()
                                if (lastMsg?.role == MessageRole.ASSISTANT) {
                                    messages[messages.lastIndex] =
                                        lastMsg.copy(reasoningContent = lastMsg.reasoningContent + event.content)
                                } else {
                                    messages.add(
                                        Message(
                                            role = MessageRole.ASSISTANT,
                                            content = "",
                                            reasoningContent = event.content
                                        )
                                    )
                                }
                                state.copy(messages = messages)
                            }
                        }
                    }
                    is ChatRepository.StreamEvent.Done -> {
                        _uiState.update { state ->
                            val messages = state.messages.toMutableList()
                            val lastIdx = messages.indexOfLast { it.role == MessageRole.ASSISTANT }
                            if (lastIdx >= 0 && event.usage != null) {
                                messages[lastIdx] = messages[lastIdx].copy(
                                    tokens = event.usage.totalTokens
                                )
                            }
                            state.copy(
                                messages = messages,
                                streamState = StreamState.Idle,
                                tokenUsage = event.usage,
                                sendEnabled = state.apiKeyConfigured
                            )
                        }
                        // 生成对话标题
                        generateTitleIfNeeded()
                        // 保存对话
                        saveCurrentConversation()
                    }
                    is ChatRepository.StreamEvent.Error -> {
                        _uiState.update { state ->
                            state.copy(
                                streamState = StreamState.Error(event.message),
                                sendEnabled = state.apiKeyConfigured
                            )
                        }
                        // 发生错误也尝试保存
                        saveCurrentConversation()
                    }
                }
            }
        }
    }

    private fun generateTitleIfNeeded() {
        if (titleGenerated) return
        val messages = _uiState.value.messages
        if (messages.size < 2) return

        viewModelScope.launch {
            try {
                titleGenerated = true
                val title = withContext(Dispatchers.IO) {
                    val titleRequest = ChatCompletionRequest(
                        model = "deepseek-chat",
                        messages = listOf(
                            ChatRequestMessage("user", "根据以下对话内容，生成一个简短的对话标题（不超过15个字）。只输出标题文本，不要输出任何其他内容。\n\n用户: ${messages.first { it.role == MessageRole.USER }.content.take(200)}\n助手: ${messages.first { it.role == MessageRole.ASSISTANT }.content.take(200)}")
                        ),
                        stream = false,
                        maxTokens = 50,
                        temperature = 0.3
                    )
                    val response = apiService.sendChatCompletion(titleRequest)
                    response.choices.firstOrNull()?.message?.content?.trim()?.replace("\"", "")?.take(20) ?: "新对话"
                }
                _uiState.update { state ->
                    val conversations = state.conversations.toMutableList()
                    val index = conversations.indexOfFirst { it.id == state.currentConversationId }
                    if (index >= 0) {
                        conversations[index] = conversations[index].copy(title = title)
                    }
                    state.copy(conversations = conversations)
                }
                saveCurrentConversation()
            } catch (_: Exception) {
                titleGenerated = false
            }
        }
    }

    private fun saveCurrentConversation() {
        val state = _uiState.value
        val messages = state.messages
        if (messages.isEmpty()) return

        viewModelScope.launch {
            val convId = state.currentConversationId.ifEmpty { null }
            val conversations = state.conversations.toMutableList()

            val conv = Conversation(
                id = convId ?: java.util.UUID.randomUUID().toString(),
                title = conversations.find { it.id == convId }?.title ?: "新对话",
                messages = messages,
                createdAt = conversations.find { it.id == convId }?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (convId != null) {
                val index = conversations.indexOfFirst { it.id == convId }
                if (index >= 0) {
                    conversations[index] = conv
                } else {
                    conversations.add(0, conv)
                }
            } else {
                conversations.add(0, conv)
            }

            conversationStorage.saveAll(conversations)

            _uiState.update {
                it.copy(
                    conversations = conversations,
                    currentConversationId = conv.id
                )
            }
        }
    }

    fun newConversation() {
        saveCurrentConversationSync()
        _uiState.update { state ->
            state.copy(
                messages = emptyList(),
                streamState = StreamState.Idle,
                tokenUsage = null,
                currentConversationId = "",
                sendEnabled = state.apiKeyConfigured
            )
        }
        titleGenerated = false
    }

    private fun saveCurrentConversationSync() {
        val state = _uiState.value
        val messages = state.messages
        if (messages.isEmpty()) return

        viewModelScope.launch {
            val convId = state.currentConversationId.ifEmpty { null }
            val conversations = state.conversations.toMutableList()

            val conv = Conversation(
                id = convId ?: java.util.UUID.randomUUID().toString(),
                title = conversations.find { it.id == convId }?.title ?: "新对话",
                messages = messages,
                createdAt = conversations.find { it.id == convId }?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (convId != null) {
                val index = conversations.indexOfFirst { it.id == convId }
                if (index >= 0) {
                    conversations[index] = conv
                } else {
                    conversations.add(0, conv)
                }
            } else {
                conversations.add(0, conv)
            }

            conversationStorage.saveAll(conversations)
            _uiState.update { it.copy(conversations = conversations) }
        }
    }

    fun switchConversation(conversationId: String) {
        if (conversationId == _uiState.value.currentConversationId) return
        saveCurrentConversationSync()
        val conv = _uiState.value.conversations.find { it.id == conversationId } ?: return
        _uiState.update { state ->
            state.copy(
                messages = conv.messages,
                streamState = StreamState.Idle,
                tokenUsage = null,
                currentConversationId = conversationId,
                sendEnabled = state.apiKeyConfigured
            )
        }
        titleGenerated = conv.title != "新对话"
    }

    fun showDeleteDialog(conversationId: String) {
        _uiState.update { it.copy(showDeleteDialog = conversationId) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = null) }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            conversationStorage.delete(conversationId)
            val conversations = conversationStorage.loadAll()
            if (conversationId == _uiState.value.currentConversationId) {
                _uiState.update { state ->
                    val next = conversations.firstOrNull()
                    state.copy(
                        conversations = conversations,
                        currentConversationId = next?.id ?: "",
                        messages = next?.messages ?: emptyList(),
                        streamState = StreamState.Idle,
                        tokenUsage = null,
                        showDeleteDialog = null,
                        sendEnabled = state.apiKeyConfigured
                    )
                }
            } else {
                _uiState.update { it.copy(conversations = conversations, showDeleteDialog = null) }
            }
        }
    }

    fun cancelStream() {
        streamingJob?.cancel()
        streamingJob = null
        _uiState.update { state ->
            state.copy(
                streamState = StreamState.Idle,
                sendEnabled = state.apiKeyConfigured
            )
        }
    }

    fun toggleClearDialog(show: Boolean) {
        _uiState.update { it.copy(showClearDialog = show) }
    }

    fun clearChat() {
        cancelStream()
        _uiState.update {
            ChatUiState(
                apiKeyConfigured = it.apiKeyConfigured,
                sendEnabled = it.apiKeyConfigured,
                conversations = it.conversations,
                currentConversationId = it.currentConversationId
            )
        }
        titleGenerated = false
    }

    fun dismissError() {
        if (_uiState.value.streamState is StreamState.Error) {
            _uiState.update { state ->
                state.copy(
                    streamState = StreamState.Idle,
                    sendEnabled = state.apiKeyConfigured
                )
            }
        }
    }
}
