package io.github.gaeqs.ytdownloader.compose

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import io.github.gaeqs.ytdownloader.client.ClientInstance

@Composable
fun LogoutAction(nav: NavController) {
    TextButton(onClick = {
        ClientInstance.disconnect()
        nav.navigate("login") {
            popUpTo(0) {
                inclusive = true
            }
        }
    }) {
        Text(text = "Logout")
    }
}
