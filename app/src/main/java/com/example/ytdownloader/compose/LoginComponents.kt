package com.example.ytdownloader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun StandaloneLoginCard() {
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    LoginCard(user, password, { user = it }, { password = it })
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
fun LoginButton(user: String, password: String, onLogin: () -> Unit, onError: () -> Unit) {
    Button(
        enabled = user.isNotBlank() && password.isNotBlank(),
        modifier = Modifier.fillMaxWidth(0.8f),
        onClick = {
            onError()
        }) {
        Text(text = "Login")
    }
}