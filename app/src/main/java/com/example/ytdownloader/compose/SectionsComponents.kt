package com.example.ytdownloader.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ytdownloader.client.ClientInstance
import com.example.ytdownloader.client.Song
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SectionsScaffold(nav: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Sections") }) },
        bottomBar = { MainNav(nav) }
    ) {
        SectionsList()
    }
}

@Composable
fun SectionsList() {
    val state = rememberSwipeRefreshState(false)
    val scope = rememberCoroutineScope()
    SwipeRefresh(
        state = state,
        onRefresh = {
            scope.launch {
                state.isRefreshing = true
                ClientInstance.refreshSectionsAndSongs()
                state.isRefreshing = false
            }
        }) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp, 8.dp)
        ) {
            items(ClientInstance.sections) { name ->
                Section(name = name)
            }
        }
    }
}

@Composable
fun Section(name: String) {
    val scope = rememberCoroutineScope()
    val image by remember {
        val song = ClientInstance.songs[name]?.firstOrNull()
        if (song == null) {
            mutableStateOf<ImageBitmap?>(null)
        } else {
            ClientInstance.getOrLoadImage(song.album, scope)
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
                    Image(bitmap = image!!, contentDescription = name)
                }
                Text(text = name, style = MaterialTheme.typography.h4)
            }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ClientInstance.songs[name]?.forEach {
                    Song(it, scope)
                }
            }
        }
    }
}

@Composable
fun Song(song: Song, scope: CoroutineScope) {
    val image by remember { ClientInstance.getOrLoadImage(song.album, scope) }

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
                    Image(bitmap = image!!, contentDescription = song.name)
                }
                Text(text = song.name, style = MaterialTheme.typography.h5)
            }
        }
    }
}