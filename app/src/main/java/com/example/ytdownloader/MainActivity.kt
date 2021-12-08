package com.example.ytdownloader

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.ytdownloader.compose.ExpandableContent
import com.example.ytdownloader.material.DARK_COLORS
import com.example.ytdownloader.material.DARK_TYPOGRAPHY
import com.example.ytdownloader.material.LIGHT_COLORS
import com.example.ytdownloader.preferences.PreferencesManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferencesManager.init(application)

        setContent {

            MaterialTheme(
                colors = if (isSystemInDarkTheme()) DARK_COLORS else LIGHT_COLORS,
                typography = if (isSystemInDarkTheme()) DARK_TYPOGRAPHY else MaterialTheme.typography,
            ) {
                Login()
            }
        }
    }

    @Composable
    fun Login() {
        var user by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var host by remember { mutableStateOf("locahost") }
        var port by remember { mutableStateOf(2222) }

        var error by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon()

            Spacer(modifier = Modifier.weight(0.5f))

            LoginCard(user, password, { user = it }, { password = it })

            if (error) {
                Text(
                    text = "Invalid user or password!",
                    color = MaterialTheme.colors.error
                )
            }


            Spacer(modifier = Modifier.weight(0.2f))

            ExpandableContent(title = "Hellow") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HostCard(host, port, { host = it }, { port = it })
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))

            LoginButton(user, password, {}, { error = true })

            Spacer(modifier = Modifier.weight(1.0f))
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun Icon() {
        var expanded by remember { mutableStateOf(true) }
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(painterResource(R.drawable.yt_downloader_crack), null)
            AnimatedVisibility(expanded) {
                Text(
                    text = "Magic Music Storage",
                    style = MaterialTheme.typography.h2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}