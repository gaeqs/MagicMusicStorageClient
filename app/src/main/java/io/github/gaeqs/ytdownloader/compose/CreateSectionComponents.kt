package io.github.gaeqs.ytdownloader.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.gaeqs.ytdownloader.client.ClientInstance
import io.github.gaeqs.ytdownloader.client.postSection
import io.ktor.client.features.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CreateSectionDialog(
    nav: NavController,
    scope: CoroutineScope,
    show: Boolean,
    onDismissRequest: () -> Unit
) {
    if (show) {

        var name by remember { mutableStateOf("") }
        val valid = isSectionNameValid(name = name)
        val context = LocalContext.current

        var confirmEnabled by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            dismissButton = {
                Button(onClick = { onDismissRequest() }) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                Button(
                    enabled = valid && confirmEnabled,
                    onClick = {
                        confirmEnabled = false
                        sendSection(
                            section = name.trim(),
                            scope = scope,
                            nav = nav,
                            context = context,
                            onFinished = {
                                confirmEnabled = true
                                if (it) {
                                    scope.launch { ClientInstance.refreshSections() }
                                    onDismissRequest()
                                }
                            }
                        )
                    }
                ) {
                    Text(text = "Confirm")
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Insert the name of the new section",
                        style = MaterialTheme.typography.h6,
                    )

                    TextField(
                        label = {
                            if (!valid && name.isNotBlank()) {
                                Text(
                                    text = "Section already exists!",
                                    color = MaterialTheme.colors.error,
                                )
                            } else {
                                Text(text = "Name")
                            }
                        },
                        value = name,
                        isError = !valid,
                        onValueChange = { name = it }
                    )
                }
            }
        )
    }
}

@Composable
private fun isSectionNameValid(name: String): Boolean {
    return name.isNotBlank() && name.trim() !in ClientInstance.sections
}

private fun sendSection(
    section: String,
    scope: CoroutineScope,
    nav: NavController,
    context: Context,
    onFinished: (Boolean) -> Unit = {}
) = scope.launch {
    var result = true
    ClientInstance.client!!.postSection(section) {
        if (it is ClientRequestException) {
            when (it.response.status) {
                HttpStatusCode.Unauthorized -> nav.navigate("login")
                else -> Toast.makeText(context, it.response.readText(), Toast.LENGTH_SHORT).show()
            }
        } else {
            it.printStackTrace()
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            nav.navigate("login")
        }
        result = false
    }
    onFinished(result)
}