package io.github.gaeqs.ytdownloader.compose

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.gaeqs.ytdownloader.client.ClientInstance
import io.github.gaeqs.ytdownloader.client.ImageCache
import io.github.gaeqs.ytdownloader.client.postAlbum
import io.github.gaeqs.ytdownloader.util.bitmapFromUri
import io.ktor.client.features.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CreateAlbumDialog(
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
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var image by remember { mutableStateOf<ImageBitmap?>(null) }

        val pickPictureLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                imageUri = uri
                image = context.bitmapFromUri(uri)
            }
        }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            dismissButton = {
                Button(onClick = { onDismissRequest() }) {
                    Text(text = "Cancel")
                }
            },
            confirmButton = {
                Button(
                    enabled = valid && image != null && confirmEnabled,
                    onClick = {
                        confirmEnabled = false
                        sendAlbum(
                            album = name.trim(),
                            image = imageUri!!,
                            scope = scope,
                            nav = nav,
                            context = context,
                            onFinished = {
                                confirmEnabled = true
                                if (it) {
                                    ImageCache.forceRefresh(name.trim(), context )
                                    scope.launch { ClientInstance.refreshAlbums() }
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
                        text = "Insert the name of the new album",
                        style = MaterialTheme.typography.h6,
                    )

                    TextField(
                        label = {
                            if (!valid && name.isNotBlank()) {
                                Text(
                                    text = "Album already exists!",
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { pickPictureLauncher.launch("image/*") }) {
                            Text(text = "Select image")
                        }
                        image?.let { Image(bitmap = it, contentDescription = "Album image") }
                    }
                }
            }
        )
    }
}

@Composable
private fun isSectionNameValid(name: String): Boolean {
    return name.isNotBlank() && name.trim() !in ClientInstance.sections
}

private fun sendAlbum(
    album: String,
    image: Uri,
    scope: CoroutineScope,
    nav: NavController,
    context: Context,
    onFinished: (Boolean) -> Unit = {}
) = scope.launch {
    var result = true
    ClientInstance.client!!.postAlbum(context, album, image) {
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