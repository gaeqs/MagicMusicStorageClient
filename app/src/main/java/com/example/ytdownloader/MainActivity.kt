package com.example.ytdownloader

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ytdownloader.client.ClientInstance
import com.example.ytdownloader.compose.MainScaffold
import com.example.ytdownloader.compose.SectionsScaffold
import com.example.ytdownloader.material.DARK_COLORS
import com.example.ytdownloader.material.DARK_TYPOGRAPHY
import com.example.ytdownloader.material.LIGHT_COLORS
import com.example.ytdownloader.preferences.PreferencesManager
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferencesManager.init(application)

        val result = runBlocking { tryConnect() }

        setContent {
            val nav = rememberNavController()
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) DARK_COLORS else LIGHT_COLORS,
                typography = if (isSystemInDarkTheme()) DARK_TYPOGRAPHY else MaterialTheme.typography,
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                ) {
                    NavHost(
                        navController = nav,
                        startDestination = if (result) "main" else "login"
                    ) {
                        composable("login") { Login(nav) }
                        composable("main") { MainScaffold(nav) }
                        composable("sections") { SectionsScaffold(nav) }
                    }
                }
            }
        }
    }

    private suspend fun tryConnect(): Boolean {
        val user = PreferencesManager.user ?: return false
        val password = PreferencesManager.password ?: return false
        val host = PreferencesManager.host ?: return false
        val port = PreferencesManager.port ?: return false
        return ClientInstance.tryConnect(user, password, host, port).first
    }
}