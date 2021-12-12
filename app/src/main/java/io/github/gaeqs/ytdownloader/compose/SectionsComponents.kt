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
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import io.github.gaeqs.ytdownloader.client.ClientInstance
import io.github.gaeqs.ytdownloader.client.Song
import io.github.gaeqs.ytdownloader.work.SyncWorker
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
        println("DOWN")
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
        val dataBuilder = Data.Builder()
        dataBuilder.putString("folder", uri.toString())
        dataBuilder.putString("section", selectedSection)
        request.setInputData(dataBuilder.build())
        WorkManager.getInstance(context).enqueue(request.build())
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
                Section(name = name, folderLauncher = folderLauncher)
            }
        }
    }
}

@Composable
fun Section(name: String, folderLauncher: ManagedActivityResultLauncher<Uri?, Uri?>) {
    val context = LocalContext.current
    val image by remember {
        val song = ClientInstance.songs[name]?.firstOrNull()
        if (song == null) {
            mutableStateOf<ImageBitmap?>(null)
        } else {
            ClientInstance.getOrLoadImage(song.album, context)
        }
    }

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

                IconButton(onClick = {
                    selectedSection = name
                    folderLauncher.launch(null)
                }) {
                    Icon(imageVector = Icons.Filled.Sync, contentDescription = "Sync")
                }

            }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ClientInstance.songs[name]?.forEach {
                    Song(it)
                }
            }
        }
    }
}

@Composable
fun Song(song: Song) {
    val context = LocalContext.current
    val image by remember { ClientInstance.getOrLoadImage(song.album, context) }

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
                Text(text = song.name, style = MaterialTheme.typography.h5)
            }
        }
    }
}