package com.deepseek.chat.data.local

import android.content.Context
import com.deepseek.chat.domain.model.Conversation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ConversationStorage(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val storageFile: File
        get() = File(context.filesDir, "conversations.json")
    private val backupFile: File
        get() = File(context.filesDir, "conversations_backup.json")

    suspend fun loadAll(): List<Conversation> = withContext(Dispatchers.IO) {
        try {
            if (storageFile.exists()) {
                val content = storageFile.readText()
                if (content.isBlank()) emptyList()
                else json.decodeFromString<List<Conversation>>(content)
            } else if (backupFile.exists()) {
                // 主文件不存在时尝试从备份恢复
                val content = backupFile.readText()
                if (content.isBlank()) emptyList()
                else json.decodeFromString<List<Conversation>>(content)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // 主文件损坏时尝试从备份恢复
            if (backupFile.exists()) {
                try {
                    val content = backupFile.readText()
                    if (content.isBlank()) emptyList()
                    else json.decodeFromString<List<Conversation>>(content)
                } catch (_: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }

    suspend fun saveAll(conversations: List<Conversation>) = withContext(Dispatchers.IO) {
        try {
            val newContent = json.encodeToString(conversations)
            // 先备份当前有效数据
            if (storageFile.exists()) {
                try {
                    storageFile.copyTo(backupFile, overwrite = true)
                } catch (_: Exception) { }
            }
            storageFile.writeText(newContent)
        } catch (_: Exception) { }
    }

    suspend fun save(conversation: Conversation) = withContext(Dispatchers.IO) {
        val all = loadAll().toMutableList()
        val index = all.indexOfFirst { it.id == conversation.id }
        if (index >= 0) {
            all[index] = conversation
        } else {
            all.add(0, conversation)
        }
        saveAll(all)
    }

    suspend fun delete(conversationId: String) = withContext(Dispatchers.IO) {
        val all = loadAll().toMutableList()
        all.removeAll { it.id == conversationId }
        saveAll(all)
    }
}
