package io.github.gaeqs.ytdownloader

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.gaeqs.ytdownloader.client.ClientInstance
import io.github.gaeqs.ytdownloader.compose.AppIcon
import io.github.gaeqs.ytdownloader.compose.ExpandableContent
import io.github.gaeqs.ytdownloader.preferences.PreferencesManager
import kotlinx.coroutines.launch


@Composable
fun Login(nav: NavController) {
    var user by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var host by rememberSaveable { mutableStateOf(PreferencesManager.host ?: "localhost") }
    var port by rememberSaveable { mutableStateOf(PreferencesManager.port ?: 22222) }
    var error by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(ScrollState(0)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIcon(modifier = Modifier.padding(0.dp, 8.dp))

        LoginCard(user, password, { user = it }, { password = it })

        Text(
            text = error,
            color = MaterialTheme.colors.error
        )

        ExpandableContent(title = "Host", modifier = Modifier.padding(0.dp, 8.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HostCard(host, port, { host = it }, { port = it })
            }
        }

        LoginButton(
            user = user,
            password = password,
            host = host,
            port = port,
            modifier = Modifier.padding(0.dp, 8.dp),
            onError = { error = it }
        ) {
            PreferencesManager.setLoginData(user, password, host, port)
            nav.navigate("main")
        }
    }
}

@Composable
fun LoginCard(
    user: String = "",
    password: String = "",
    onUserChange: (String) -> Unit = {},
    onPassWordChange: (String) -> Unit = {},
) {
    var passwordVisibility by remember { mutableStateOf(false) }
    Column {
        TextField(
            label = { Text("Username") },
            value = user,
            onValueChange = onUserChange
        )
        TextField(
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            value = password,
            onValueChange = onPassWordChange,
            visualTransformation = if (passwordVisibility) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                    Icon(
                        imageVector = if (passwordVisibility) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff,
                        contentDescription = "Show password"
                    )
                }
            }
        )
    }
}

@Composable
fun HostCard(
    host: String = "localhost",
    port: Int = 2222,
    onHostChange: (String) -> Unit = {},
    onPortChange: (Int) -> Unit = {},
) {
    Column {
        TextField(
            label = { Text("Host") },
            value = host,
            onValueChange = onHostChange
        )
        TextField(
            label = { Text("Port") },
            value = port.toString(),
            onValueChange = {
                onPortChange(it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0)
            }
        )
    }
}


@Composable
fun LoginButton(
    user: String,
    password: String,
    host: String,
    port: Int,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit,
    onLogin: () -> Unit
) {
    var enabled by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    Button(
        enabled = enabled && user.isNotBlank() && password.isNotBlank()
                && host.isNotBlank() && port >= 0,
        modifier = modifier.fillMaxWidth(0.8f),
        onClick = {
            scope.launch {
                enabled = false

                val (success, message) = ClientInstance.tryConnect(user, password, host, port)

                if (success) {
                    onLogin()
                } else {
                    onError(message)
                }

                enabled = true
            }
        }) {
        Text(text = "Login")
    }
}