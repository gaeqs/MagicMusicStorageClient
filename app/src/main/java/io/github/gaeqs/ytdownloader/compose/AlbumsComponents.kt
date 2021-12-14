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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.gaeqs.ytdownloader.client.ClientInstance
import io.github.gaeqs.ytdownloader.client.ImageCache
import io.github.gaeqs.ytdownloader.client.deleteAlbum
import io.github.gaeqs.ytdownloader.work.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

private var selectedAlbum: String = ""

@Composable
fun AlbumsScaffold(nav: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Albums") }, actions = { LogoutAction(nav) }) },
        bottomBar = { MainNav(nav) }
    ) {
        AlbumsList(nav)
    }
}

@Composable
fun AlbumsList(nav: NavController) {
    val state = rememberSwipeRefreshState(false)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("folder" to uri.toString(), "album" to selectedAlbum))
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    SwipeRefresh(
        state = state,
        onRefresh = {
            scope.launch {
                state.isRefreshing = true
                try {
                    ClientInstance.refreshAlbums()
                    ClientInstance.refreshSectionsAndSongs()
                } catch (ex: Exception) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                    nav.navigate("login")
                }
                state.isRefreshing = false
            }
        }) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp, 8.dp, 8.dp, 64.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            var search by rememberSaveable { mutableStateOf("") }
            val searchLower = search.lowercase().trim()

            val items = mutableListOf<Any>()
            var expandedElements by remember { mutableStateOf(setOf<String>()) }

            val songs = TreeSet<SectionSong> { o1, o2 ->
                val i = o1.song.album.compareTo(o2.song.album)
                if (i == 0) {
                    return@TreeSet o1.song.name.compareTo(o2.song.name)
                }
                return@TreeSet i
            }

            ClientInstance.albums.sortedBy { it.lowercase() }.forEach { album ->

                songs.clear()
                ClientInstance.songs.entries.forEach { (section, list) ->
                    list.filter {
                        it.album == album &&
                                (it.album.lowercase().startsWith(searchLower)
                                        || it.name.lowercase().startsWith(searchLower))
                    }.forEach {
                        songs += SectionSong(section, it)
                    }
                }

                if (songs.isEmpty() && searchLower.isNotEmpty()) return@forEach

                items += album
                if (album in expandedElements) {
                    items.addAll(songs)
                }
            }


            TextField(
                modifier = Modifier.fillMaxWidth(1.0f),
                label = { Text(text = "Filter") },
                value = search,
                onValueChange = { search = it }
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(items) { element ->
                    if (element is String) {
                        Album(visible = element in expandedElements, name = element,
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
}

@Composable
fun Album(
    visible: Boolean,
    name: String,
    folderLauncher: ManagedActivityResultLauncher<Uri?, Uri?>,
    scope: CoroutineScope,
    onVisible: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val image by ImageCache.getOrLoadImage(name, context)
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
                            .weight(0.3f)
                            .padding(top = 4.dp, end = 16.dp),
                        bitmap = image!!,
                        contentDescription = name
                    )
                } else {
                    Spacer(modifier = Modifier.weight(0.3f))
                }

                Text(
                    modifier = Modifier.weight(0.525f),
                    text = name,
                    style = MaterialTheme.typography.h5
                )


                Column(
                    modifier = Modifier.weight(0.175f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        selectedAlbum = name
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
            title = { Text("Delete album $name?") },
            confirmButton = {
                Button(onClick = {
                    deleting = false
                    scope.launch {
                        ClientInstance.client!!.deleteAlbum<Any?>(name)
                        ClientInstance.refreshAlbums()
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