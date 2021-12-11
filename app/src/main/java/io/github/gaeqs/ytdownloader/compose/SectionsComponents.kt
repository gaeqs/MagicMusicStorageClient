package io.github.gaeqs.ytdownloader.compose

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.gaeqs.ytdownloader.client.ClientInstance
import io.github.gaeqs.ytdownloader.client.Song
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch

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
                Section(name = name)
            }
        }
    }
}

@Composable
fun Section(name: String) {
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