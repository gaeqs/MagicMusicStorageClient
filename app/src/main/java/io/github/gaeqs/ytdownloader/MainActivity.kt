package io.github.gaeqs.ytdownloader

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.gaeqs.ytdownloader.client.ClientInstance
import io.github.gaeqs.ytdownloader.compose.MainScaffold
import io.github.gaeqs.ytdownloader.compose.SectionsScaffold
import io.github.gaeqs.ytdownloader.compose.StatusScaffold
import io.github.gaeqs.ytdownloader.material.DARK_COLORS
import io.github.gaeqs.ytdownloader.material.DARK_TYPOGRAPHY
import io.github.gaeqs.ytdownloader.material.LIGHT_COLORS
import io.github.gaeqs.ytdownloader.preferences.PreferencesManager
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var nav: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferencesManager.init(application)

        setContent {
            nav = rememberNavController()
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
                        startDestination = if (ClientInstance.isConnected()) "main" else "login"
                    ) {
                        composable("login") { Login(nav) }
                        composable("main") { MainScaffold(nav) }
                        composable("sections") { SectionsScaffold(nav) }
                        composable("status") { StatusScaffold(nav) }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        ClientInstance.disconnect()
    }

    override fun onResume() {
        super.onResume()
        if (ClientInstance.client == null) {
            runBlocking { tryConnect() }
        }

        super.onResume()
    }

    private suspend fun tryConnect(): Boolean {
        val user = PreferencesManager.user ?: return false
        val password = PreferencesManager.password ?: return false
        val host = PreferencesManager.host ?: return false
        val port = PreferencesManager.port ?: return false
        return ClientInstance.tryConnect(user, password, host, port).first
    }
}