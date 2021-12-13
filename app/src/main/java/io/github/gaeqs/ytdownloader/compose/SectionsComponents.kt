package io.github.gaeqs.ytdownloader.compose

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.gaeqs.ytdownloader.client.ClientInstance
import io.github.gaeqs.ytdownloader.client.Song
import io.github.gaeqs.ytdownloader.client.deleteSection
import io.github.gaeqs.ytdownloader.client.deleteSong
import io.github.gaeqs.ytdownloader.work.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private var selectedSection: String = ""

@Composable
fun SectionsScaffold(nav: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Sections") }) },
        bottomBar = { MainNav(nav) }
    ) {
        SectionsList(nav)
    }
}

@Composable
fun SectionsList(nav: NavController) {
    val state = rememberSwipeRefreshState(false)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("folder" to uri.toString(), "section" to selectedSection))
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    SwipeRefresh(
        state = state,
        onRefresh = {
            scope.launch {
                state.isRefreshing = true
                try {
                    ClientInstance.refreshSectionsAndSongs()
                } catch (ex: Exception) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                    nav.navigate("login")
                }
                state.isRefreshing = false
            }
        }) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp, 8.dp, 8.dp, 64.dp)
        ) {
            items(ClientInstance.sections.sortedBy { it.lowercase() }) { name ->
                Section(name = name, folderLauncher = folderLauncher, scope = scope)
            }
        }
    }
}

@Composable
fun Section(
    name: String,
    folderLauncher: ManagedActivityResultLauncher<Uri?, Uri?>,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val image by remember {
        val song = ClientInstance.songs[name]?.firstOrNull()
        if (song == null) {
            mutableStateOf<ImageBitmap?>(null)
        } else {
            ClientInstance.getOrLoadImage(song.album, context)
        }
    }
    var deleting by remember { mutableStateOf(false) }

    Card {
        ExpandableContent(
            title = name,
            modifier = Modifier.fillMaxWidth(),
            rowModifier = Modifier.height(128.dp),
            width = 1.0f,
            rowScope = {
                if (image != null) {
                    Image(
                        modifier = Modifier.padding(top = 4.dp, end = 16.dp),
                        bitmap = image!!,
                        contentDescription = name
                    )
                }
                Text(text = name, style = MaterialTheme.typography.h4)


                Spacer(modifier = Modifier.weight(1.0f))

                Column(
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        selectedSection = name
                        folderLauncher.launch(null)
                    }) {
                        Icon(imageVector = Icons.Filled.Sync, contentDescription = "Sync")
                    }
                    IconButton(onClick = {
                        deleting = true
                    }) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }

            }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ClientInstance.songs[name]?.forEach {
                    Song(name, it, scope)
                }
            }
        }
    }

    if (deleting) {
        AlertDialog(
            onDismissRequest = { deleting = false },
            title = { Text("Delete section $name?") },
            confirmButton = {
                Button(onClick = {
                    deleting = false
                    scope.launch {
                        ClientInstance.client!!.deleteSection<Any?>(name)
                        ClientInstance.refreshSectionsAndSongs()
                    }
                }) {
                    Text(text = "Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { deleting = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
fun Song(section: String, song: Song, scope: CoroutineScope) {
    val context = LocalContext.current
    val image by remember { ClientInstance.getOrLoadImage(song.album, context) }
    var deleting by remember { mutableStateOf(false) }

    Surface {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth()
            ) {
                if (image != null) {
                    Image(
                        modifier = Modifier.padding(end = 16.dp),
                        bitmap = image!!,
                        contentDescription = song.name
                    )
                }
                Text(text = "${song.album} - ${song.name}", style = MaterialTheme.typography.h5)

                Spacer(modifier = Modifier.weight(1.0f))

                IconButton(onClick = {
                    deleting = true
                }) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }
    if (deleting) {
        AlertDialog(
            onDismissRequest = { deleting = false },
            title = { Text("Delete song ${song.album} - ${song.name}?") },
            confirmButton = {
                Button(onClick = {
                    deleting = false
                    scope.launch {
                        ClientInstance.client!!.deleteSong<Any?>(
                            song.name,
                            section,
                            song.album
                        )
                        ClientInstance.refreshSectionsAndSongs()
                    }
                }) {
                    Text(text = "Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { deleting = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}