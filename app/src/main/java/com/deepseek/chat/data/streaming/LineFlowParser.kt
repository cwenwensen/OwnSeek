package com.deepseek.chat.data.streaming

import com.deepseek.chat.data.api.dto.ChatStreamChunk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okio.BufferedSource

/**
 * SSE (Server-Sent Events) 流解析器
 *
 * 将 OkHttp 的 BufferedSource（字节流）逐行读取，
 * 解析 "data: {...}" 格式的 SSE 行，发射为 ChatStreamChunk 的 Flow。
 *
 * 原理：
 * - DeepSeek 以 SSE 协议发送数据，每行格式为 "data: <json>"
 * - "data: [DONE]" 表示流结束
 * - 忽略空行和注释行
 */
object LineFlowParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * 将 BufferedSource 的字节流解析为 Flow<ChatStreamChunk>
     * @param source OkHttp 响应体提供的 BufferedSource
     * @return Flow 发射每一个解析成功的 chunk；流结束时自动关闭。
     */
    fun parse(source: BufferedSource): Flow<ChatStreamChunk> = flow {
        try {
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue

                // SSE 标准格式：每行以 "data: " 开头
                if (!line.startsWith("data: ")) continue

                val data = line.removePrefix("data: ").trim()

                // "[DONE]" 表示流正常结束
                if (data == "[DONE]") break

                // 跳过空数据行
                if (data.isEmpty()) continue

                try {
                    val chunk = json.decodeFromString<ChatStreamChunk>(data)
                    emit(chunk)
                } catch (e: Exception) {
                    // 某一行解析失败不影响后续行（容错处理）
                    continue
                }
            }
        } finally {
            source.close()
        }
    }
}
