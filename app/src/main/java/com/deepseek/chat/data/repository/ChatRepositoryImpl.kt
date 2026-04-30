package com.deepseek.chat.data.repository

import com.deepseek.chat.data.api.DeepSeekApiService
import com.deepseek.chat.data.api.dto.ChatCompletionRequest
import com.deepseek.chat.data.api.dto.ChatRequestMessage
import com.deepseek.chat.data.api.dto.ResponseFormat
import com.deepseek.chat.data.api.dto.Usage
import com.deepseek.chat.data.local.SettingsDataStore
import com.deepseek.chat.data.streaming.LineFlowParser
import com.deepseek.chat.domain.model.ChatSettings
import com.deepseek.chat.domain.model.Message
import com.deepseek.chat.domain.model.MessageRole
import com.deepseek.chat.domain.model.ResponseFormatType
import com.deepseek.chat.domain.model.TokenUsage
import com.deepseek.chat.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.ConnectException
import java.net.SocketTimeoutException

class ChatRepositoryImpl(
    private val apiService: DeepSeekApiService,
    private val settingsDataStore: SettingsDataStore
) : ChatRepository {

    override fun streamCompletion(
        messages: List<Message>,
        settings: ChatSettings
    ): Flow<ChatRepository.StreamEvent> = flow {

        val apiKey = settingsDataStore.apiKey.first()
        if (apiKey.isBlank()) {
            emit(ChatRepository.StreamEvent.Error("请先在设置中配置 API 密钥"))
            return@flow
        }

        val request = buildRequest(messages, settings)

        try {
            if (settings.stream) {
                val response = apiService.streamChatCompletion(request)
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string() ?: "未知错误"
                    emit(ChatRepository.StreamEvent.Error("服务器错误 (${response.code()}): $errorBody"))
                    return@flow
                }

                val body = response.body()
                    ?: run {
                        emit(ChatRepository.StreamEvent.Error("服务器返回空响应"))
                        return@flow
                    }

                var lastUsage: Usage? = null

                LineFlowParser.parse(body.source()).collect { chunk ->
                    val delta = chunk.choices.firstOrNull()?.delta

                    // 提取深度思考内容
                    val reasoning = delta?.reasoningContent ?: ""
                    if (reasoning.isNotEmpty()) {
                        emit(ChatRepository.StreamEvent.ReasoningDelta(reasoning))
                    }

                    // 提取普通内容
                    val content = delta?.content ?: ""
                    if (content.isNotEmpty()) {
                        emit(ChatRepository.StreamEvent.Delta(content))
                    }

                    if (chunk.usage != null) {
                        lastUsage = chunk.usage
                    }
                }

                emit(
                    ChatRepository.StreamEvent.Done(
                        usage = lastUsage?.let {
                            TokenUsage(
                                promptTokens = it.promptTokens,
                                completionTokens = it.completionTokens,
                                totalTokens = it.totalTokens
                            )
                        }
                    )
                )
            } else {
                val response = apiService.sendChatCompletion(request)
                val choice = response.choices.firstOrNull()
                    ?: run {
                        emit(ChatRepository.StreamEvent.Error("服务器返回了空的回复"))
                        return@flow
                    }
                val message = choice.message
                val reasoning = message?.reasoningContent ?: ""
                if (reasoning.isNotEmpty()) {
                    emit(ChatRepository.StreamEvent.ReasoningDelta(reasoning))
                }
                val content = message?.content ?: ""
                if (content.isNotEmpty()) {
                    emit(ChatRepository.StreamEvent.Delta(content))
                }
                emit(
                    ChatRepository.StreamEvent.Done(
                        usage = response.usage?.let {
                            TokenUsage(
                                promptTokens = it.promptTokens,
                                completionTokens = it.completionTokens,
                                totalTokens = it.totalTokens
                            )
                        }
                    )
                )
            }
        } catch (e: SocketTimeoutException) {
            emit(ChatRepository.StreamEvent.Error("请求超时，请稍后重试"))
        } catch (e: ConnectException) {
            emit(ChatRepository.StreamEvent.Error("网络连接失败，请检查网络"))
        } catch (e: java.io.IOException) {
            if (e.message?.contains("Canceled") == true) return@flow
            emit(ChatRepository.StreamEvent.Error("网络异常: ${e.message}"))
        } catch (e: Exception) {
            emit(ChatRepository.StreamEvent.Error("发生未知错误: ${e.message ?: "请稍后重试"}"))
        }
    }.flowOn(Dispatchers.IO)

    private fun buildRequest(
        messages: List<Message>,
        settings: ChatSettings
    ): ChatCompletionRequest {
        val requestMessages = messages.map { msg ->
            ChatRequestMessage(
                role = when (msg.role) {
                    MessageRole.USER -> "user"
                    MessageRole.ASSISTANT -> "assistant"
                },
                content = msg.content
            )
        }

        return ChatCompletionRequest(
            model = settings.model,
            messages = requestMessages,
            stream = settings.stream,
            temperature = settings.temperature.toDouble(),
            topP = settings.topP.toDouble(),
            maxTokens = settings.maxTokens,
            presencePenalty = settings.presencePenalty.toDouble(),
            frequencyPenalty = settings.frequencyPenalty.toDouble(),
            seed = settings.seed,
            thinkingMode = settings.thinkingMode.apiValue,
            responseFormat = if (settings.responseFormat == ResponseFormatType.JSON_OBJECT) {
                ResponseFormat(type = "json_object")
            } else {
                null
            },
            optOut = if (settings.forbidTraining) "training" else null
        )
    }
}
