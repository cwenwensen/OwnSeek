package com.deepseek.chat

import android.app.Application
import com.deepseek.chat.data.local.ConversationStorage
import com.deepseek.chat.data.local.SettingsDataStore

class DeepSeekApp : Application() {

    lateinit var settingsDataStore: SettingsDataStore
        private set
    lateinit var conversationStorage: ConversationStorage
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        settingsDataStore = SettingsDataStore(this)
        conversationStorage = ConversationStorage(this)
    }

    companion object {
        lateinit var instance: DeepSeekApp
            private set
    }
}
