package com.deepseek.chat.di

import com.deepseek.chat.DeepSeekApp
import com.deepseek.chat.data.api.AuthInterceptor
import com.deepseek.chat.data.api.DeepSeekApiService
import com.deepseek.chat.data.local.SettingsDataStore
import com.deepseek.chat.data.repository.ChatRepositoryImpl
import com.deepseek.chat.domain.repository.ChatRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

/**
 * 手动依赖注入容器
 *
 * 不使用 Hilt/Dagger 等 DI 框架。因为项目只有 2 个 ViewModel + 1 个 Repository，
 * 手动创建实例比引入框架更简单直接。
 *
 * 所有单例在首次访问时惰性初始化。
 */
object AppDependencies {

    private val app: DeepSeekApp get() = DeepSeekApp.instance

    /** DataStore（由 Application 初始化） */
    private val settingsDataStore: SettingsDataStore get() = app.settingsDataStore

    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /** AuthInterceptor（缓存 API 密钥，避免读 DataStore 阻塞 IO 线程） */
    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(settingsDataStore)
    }

    /** OkHttpClient（配置 5 分钟读写超时以支持长 SSE 连接） */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.deepseek.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /** API 服务（Retrofit 接口的实现） */
    val apiService: DeepSeekApiService by lazy {
        retrofit.create(DeepSeekApiService::class.java)
    }

    /** 聊天仓库（单例） */
    val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(apiService, settingsDataStore)
    }
}
