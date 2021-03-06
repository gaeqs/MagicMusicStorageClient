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
import androidx.compose.ui.Alignment
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
        topBar = { TopAppBar(title = { Text(text = "Status") }, actions = { LogoutAction(nav) }) },
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
    val image by remember { ImageCache.getOrLoadImage(status.request.album, context) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        )
        {
            if (image != null) {
                Image(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .weight(0.3f),
                    bitmap = image!!,
                    contentDescription = status.request.name
                )
            }
            Column(
                modifier = Modifier.weight(0.5f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                var name = "${status.request.album} - ${status.request.name}"
                if (name.length > 35) {
                    name = name.substring(0, 32) + "..."
                }

                Text(
                    text = name,
                    style = MaterialTheme.typography.h6
                )

                Text(
                    text = status.status.name,
                    style = MaterialTheme.typography.h6
                )

                when (status.status.type) {
                    SongDownloadStatusType.INDEFINITE ->
                        LinearProgressIndicator()
                    SongDownloadStatusType.PERCENTAGE ->
                        LinearProgressIndicator(status.percentage.toFloat())
                    SongDownloadStatusType.END -> {}
                }
            }


            if (status.status.type != SongDownloadStatusType.END) {
                IconButton(
                    modifier = Modifier.weight(0.2f),
                    onClick = {
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
                IconButton(
                    modifier = Modifier.weight(0.2f),
                    onClick = {
                        scope.launch { restartRequest(status, nav, context) }
                    }) {
                    Icon(
                        imageVector = Icons.Filled.Autorenew,
                        contentDescription = "Restart"
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(0.2f))
            }

        }
    }
}

private suspend fun cancelRequest(status: TaskStatus, nav: NavController, context: Context) {
    ClientInstance.client!!.cancelRequest(
        status.request.name,
        status.request.section,
        status.request.album
    ) {
        if (it is ClientRequestException) {
            when (it.response.status) {
                HttpStatusCode.Unauthorized -> nav.navigate("login") {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
                else -> Toast.makeText(context, it.response.status.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            it.printStackTrace()
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            nav.navigate("login") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
    }
}

private suspend fun restartRequest(status: TaskStatus, nav: NavController, context: Context) {
    ClientInstance.client!!.postRequest(status.request) {
        if (it is ClientRequestException) {
            when (it.response.status) {
                HttpStatusCode.Unauthorized -> nav.navigate("login") {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
                else -> Toast.makeText(context, it.response.readText(), Toast.LENGTH_SHORT).show()
            }
        } else {
            it.printStackTrace()
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            nav.navigate("login") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
    }
}