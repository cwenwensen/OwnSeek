package com.deepseek.chat.data.api

import com.deepseek.chat.data.api.dto.ChatCompletionRequest
import com.deepseek.chat.data.api.dto.ChatCompletionResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * DeepSeek API 的 Retrofit 接口定义
 *
 * 两个端点：
 * - streamChatCompletion: 流式请求，返回 ResponseBody 供手动 SSE 解析
 * - sendChatCompletion:   非流式请求，直接返回完整 JSON
 */
interface DeepSeekApiService {

    @Streaming
    @POST("v1/chat/completions")
    suspend fun streamChatCompletion(
        @Body request: ChatCompletionRequest
    ): Response<ResponseBody>

    @POST("v1/chat/completions")
    suspend fun sendChatCompletion(
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}
