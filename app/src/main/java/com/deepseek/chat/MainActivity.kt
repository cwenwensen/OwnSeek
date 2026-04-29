package com.deepseek.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.deepseek.chat.ui.navigation.AppNavHost
import com.deepseek.chat.ui.theme.DeepSeekChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeepSeekChatTheme {
                AppNavHost()
            }
        }
    }
}
