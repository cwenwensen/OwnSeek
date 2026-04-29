package com.deepseek.chat.domain.repository

import com.deepseek.chat.domain.model.ChatSettings
import com.deepseek.chat.domain.model.Message
import com.deepseek.chat.domain.model.TokenUsage
import kotlinx.coroutines.flow.Flow

/**
 * 聊天仓库接口
 * 定义聊天操作的核心契约，具体实现在 data 层
 */
interface ChatRepository {

    /**
     * 流式聊天补全
     * @return Flow 发射字符串增量，每次发射追加到 AI 消息末尾
     */
    fun streamCompletion(
        messages: List<Message>,
        settings: ChatSettings
    ): Flow<StreamEvent>

    sealed interface StreamEvent {
        /** 收到了一个新的内容片段 */
        data class Delta(val content: String) : StreamEvent
        /** 收到了一个新的深度思考内容片段 */
        data class ReasoningDelta(val content: String) : StreamEvent
        /** 流正常结束，附带 token 用量 */
        data class Done(val usage: TokenUsage?) : StreamEvent
        /** 发生错误 */
        data class Error(val message: String) : StreamEvent
    }
}
