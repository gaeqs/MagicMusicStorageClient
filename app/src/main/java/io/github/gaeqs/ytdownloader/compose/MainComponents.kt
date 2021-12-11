package io.github.gaeqs.ytdownloader.compose

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.gaeqs.ytdownloader.client.ClientInstance
import io.github.gaeqs.ytdownloader.client.DownloadRequest
import io.github.gaeqs.ytdownloader.client.postRequest
import io.ktor.client.features.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch

@Composable
fun MainScaffold(nav: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Download") }) },
        bottomBar = { MainNav(nav) }
    ) {
        Main(nav)
    }
}

@Composable
fun Main(nav: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(ScrollState(0)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            modifier = Modifier.padding(16.dp),
            text = "Download a new song",
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center
        )

        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        var showNewSectionDialog by remember { mutableStateOf(false) }
        var showNewAlbumDialog by remember { mutableStateOf(false) }
        var sectionDropdownExpanded by remember { mutableStateOf(false) }
        var albumDropdownExpanded by remember { mutableStateOf(false) }

        var youtubeLink by rememberSaveable { mutableStateOf("") }
        var name by rememberSaveable { mutableStateOf("") }
        var artist by rememberSaveable { mutableStateOf("") }
        var errorMessage by rememberSaveable { mutableStateOf("") }

        var section by rememberSaveable {
            mutableStateOf(ClientInstance.sections.firstOrNull() ?: "")
        }

        var album by rememberSaveable {
            mutableStateOf(ClientInstance.albums.firstOrNull() ?: "")
        }

        if (section.isNotBlank() && section !in ClientInstance.sections) {
            section = ClientInstance.sections.firstOrNull() ?: ""
        }

        if (album.isNotBlank() && album !in ClientInstance.albums) {
            album = ClientInstance.albums.firstOrNull() ?: ""
        }

        TextField(
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text(text = "URL") },
            value = youtubeLink,
            onValueChange = { youtubeLink = it }
        )

        TextField(
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text(text = "Name") },
            value = name,
            onValueChange = { name = it }
        )

        TextField(
            modifier = Modifier.fillMaxWidth(0.8f),
            label = { Text(text = "Artist") },
            value = artist,
            onValueChange = { artist = it }
        )

        DropDownMenu(
            expanded = sectionDropdownExpanded,
            title = "Section",
            modifier = Modifier.fillMaxWidth(0.8f),
            elements = ClientInstance.sections,
            selectedElement = section,
            onExpand = { sectionDropdownExpanded = it },
            onSelectElement = { section = it }
        ) {
            DropdownMenuItem(onClick = {
                showNewSectionDialog = true
                sectionDropdownExpanded = false
            }) {
                Text(text = "New...")
            }
        }

        DropDownMenu(
            expanded = albumDropdownExpanded,
            title = "Album",
            modifier = Modifier.fillMaxWidth(0.8f),
            elements = ClientInstance.albums,
            selectedElement = album,
            onExpand = { albumDropdownExpanded = it },
            onSelectElement = { album = it },
            itemBuilder = {
                val image by remember { ClientInstance.getOrLoadImage(it, context) }

                Text(text = it)

                Spacer(Modifier.weight(1.0f))

                image?.let { i ->
                    Image(
                        modifier = Modifier.height(40.dp),
                        bitmap = i,
                        contentDescription = it
                    )
                }
            }
        ) {
            DropdownMenuItem(onClick = {
                showNewAlbumDialog = true
                albumDropdownExpanded = false
            }) {
                Text(text = "New...")
            }
        }

        CreateSectionDialog(
            show = showNewSectionDialog,
            scope = scope,
            nav = nav
        ) { showNewSectionDialog = false }

        CreateAlbumDialog(
            show = showNewAlbumDialog,
            scope = scope,
            nav = nav
        ) { showNewAlbumDialog = false }

        Button(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(32.dp),
            enabled = youtubeLink.isNotBlank()
                    && name.isNotBlank()
                    && section in ClientInstance.sections
                    && album in ClientInstance.albums,
            onClick = {
                val request =
                    DownloadRequest(youtubeLink.trim(), name.trim(), artist.trim(), album, section)
                youtubeLink = ""
                errorMessage = ""

                scope.launch {
                    ClientInstance.client!!.postRequest(request) {
                        if (it is ClientRequestException) {
                            when (it.response.status) {
                                HttpStatusCode.Unauthorized -> nav.navigate("login")
                                else -> errorMessage = it.response.readText()
                            }
                        } else {
                            it.printStackTrace()
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                            nav.navigate("login")
                        }
                    }
                }
            }
        ) {
            Text(text = "Download")
        }

        Text(
            text = errorMessage,
            color = MaterialTheme.colors.error
        )

    }
}