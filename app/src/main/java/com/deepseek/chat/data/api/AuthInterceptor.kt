package com.deepseek.chat.data.api

import com.deepseek.chat.data.local.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp 拦截器：自动在每个请求头注入 API 密钥
 *
 * 通过收集 DataStore 的 apiKey Flow，将密钥缓存到 @Volatile 变量中，
 * 避免每次拦截都做阻塞的 DataStore 读取。
 */
class AuthInterceptor(
    private val settingsDataStore: SettingsDataStore
) : Interceptor {

    @Volatile
    private var cachedApiKey: String = ""

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            settingsDataStore.apiKey.collectLatest { key ->
                cachedApiKey = key
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val request = if (cachedApiKey.isNotBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $cachedApiKey")
                .addHeader("Content-Type", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .build()
        }
        return chain.proceed(request)
    }
}
