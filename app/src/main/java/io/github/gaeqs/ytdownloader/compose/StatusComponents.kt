package io.github.gaeqs.ytdownloader.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.gaeqs.ytdownloader.client.*
import io.ktor.client.features.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun StatusScaffold(nav: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Status") }) },
        bottomBar = { MainNav(nav) }
    ) {
        ClientInstance.checkStatusInformer()
        StatusList(nav)
    }
}

@Composable
fun StatusList(nav: NavController) {
    val scope = rememberCoroutineScope()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp, 8.dp)
    ) {
        items(ClientInstance.requests.values.toList()) { request ->
            Status(status = request, scope = scope, nav = nav)
        }
    }
}

@Composable
fun Status(status: TaskStatus, scope: CoroutineScope, nav: NavController) {
    val context = LocalContext.current;
    val image by remember { ClientInstance.getOrLoadImage(status.request.album, context) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
    ) {
        Row(
        )
        {
            if (image != null) {
                Image(
                    modifier = Modifier.padding(end = 16.dp),
                    bitmap = image!!,
                    contentDescription = status.request.name
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = status.request.name,
                        style = MaterialTheme.typography.h4
                    )

                    if (status.status.type != SongDownloadStatusType.END) {
                        IconButton(onClick = {
                            scope.launch { cancelRequest(status, nav, context) }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = "Cancel"
                            )
                        }
                    } else if (
                        status.status == SongDownloadStatus.CANCELLED
                        || status.status == SongDownloadStatus.ERROR
                    ) {
                        IconButton(onClick = {
                            scope.launch { restartRequest(status, nav, context) }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Autorenew,
                                contentDescription = "Restart"
                            )
                        }
                    }
                }

                Text(
                    text = status.status.name,
                    style = MaterialTheme.typography.h5
                )

                when (status.status.type) {
                    SongDownloadStatusType.INDEFINITE ->
                        LinearProgressIndicator()
                    SongDownloadStatusType.PERCENTAGE ->
                        LinearProgressIndicator(status.percentage.toFloat())
                    SongDownloadStatusType.END -> {}
                }

            }
        }
    }
}

private suspend fun cancelRequest(status: TaskStatus, nav: NavController, context: Context) {
    ClientInstance.client!!.cancelRequest(status.request.name, status.request.section) {
        if (it is ClientRequestException) {
            when (it.response.status) {
                HttpStatusCode.Unauthorized -> nav.navigate("login")
                else -> Toast.makeText(context, it.response.status.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            it.printStackTrace()
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            nav.navigate("login")
        }
    }
}

private suspend fun restartRequest(status: TaskStatus, nav: NavController, context: Context) {
    ClientInstance.client!!.postRequest(status.request) {
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
    }
}