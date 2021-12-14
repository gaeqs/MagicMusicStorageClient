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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.gaeqs.ytdownloader.client.*
import io.github.gaeqs.ytdownloader.work.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SectionSong(val section: String, val song: Song)

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
                    ClientInstance.refreshAlbums()
                } catch (ex: Exception) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                    nav.navigate("login")
                }
                state.isRefreshing = false
            }
        }) {


        val items = mutableListOf<Any>()
        var expandedElements by remember { mutableStateOf(setOf<String>()) }
        ClientInstance.sections.sortedBy { it.lowercase() }.forEach {
            items += it
            if (it in expandedElements) {
                ClientInstance.songs[it]
                    ?.sortedBy { s -> s.album + " - " + s.name }
                    ?.forEach { s -> items += SectionSong(it, s) }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp, 8.dp, 8.dp, 64.dp)
        ) {
            items(items) { element ->
                if (element is String) {
                    Section(visible = element in expandedElements, name = element,
                        folderLauncher = folderLauncher, scope = scope,
                        onVisible = {
                            expandedElements = if (it) {
                                expandedElements + element
                            } else {
                                expandedElements - element
                            }
                        })
                } else if (element is SectionSong) {
                    Song(element.section, element.song, scope)
                }
            }
        }
    }
}

@Composable
fun Section(
    visible: Boolean,
    name: String,
    folderLauncher: ManagedActivityResultLauncher<Uri?, Uri?>,
    scope: CoroutineScope,
    onVisible: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val song = ClientInstance.songs[name]?.firstOrNull()
    val image = if (song == null) {
        null
    } else {
        ImageCache.getOrLoadImage(song.album, context).value
    }
    var deleting by remember { mutableStateOf(false) }

    Card {
        SimpleExpandableContent(
            visible = visible,
            title = name,
            modifier = Modifier.fillMaxWidth(),
            rowModifier = Modifier.height(128.dp),
            width = 1.0f,
            onVisible = onVisible,
            rowScope = {
                if (image != null) {
                    Image(
                        modifier = Modifier
                            .padding(top = 4.dp, end = 16.dp)
                            .weight(0.3f),
                        bitmap = image,
                        contentDescription = name
                    )
                } else {
                    Spacer(modifier = Modifier.weight(0.3f))
                }
                Text(
                    modifier = Modifier.weight(0.525f),
                    text = name,
                    style = MaterialTheme.typography.h4
                )


                Column(
                    modifier = Modifier
                        .weight(0.175f)
                        .padding(end = 8.dp),
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

            })
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
    val image by remember { ImageCache.getOrLoadImage(song.album, context) }
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
                        modifier = Modifier
                            .padding(top = 4.dp, end = 16.dp, bottom = 4.dp)
                            .weight(0.3f),
                        bitmap = image!!,
                        contentDescription = song.name
                    )
                } else {
                    Spacer(modifier = Modifier.weight(0.3f))
                }
                Text(
                    modifier = Modifier.weight(0.5f),
                    text = "${song.album} - ${song.name}",
                    style = MaterialTheme.typography.h5
                )

                IconButton(
                    modifier = Modifier.weight(0.2f),
                    onClick = {
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