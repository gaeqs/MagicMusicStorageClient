package com.example.ytdownloader.client

import android.graphics.BitmapFactory
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.features.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object ClientInstance {

    var client: ClientWrapper? = null
    var status: DownloadTaskStatusInformer? = null

    var sections by mutableStateOf(listOf<String>())
        private set

    var albums by mutableStateOf(listOf<String>())
    var songs by mutableStateOf(mapOf<String, List<Song>>())
    var images by mutableStateOf(mapOf<String, MutableState<ImageBitmap?>>())
    var requests by mutableStateOf(mapOf<DownloadRequest, TaskStatus>())

    suspend fun tryConnect(
        user: String,
        password: String,
        host: String,
        port: Int
    ): Pair<Boolean, String> {
        disconnect()
        val client = ClientWrapper(host, port)
        client.loginInfo = LoginUser(user, password)
        client.apiTest {
            if (it is ClientRequestException) {
                return Pair(false, it.response.readText())
            }
            return Pair(false, it.localizedMessage ?: "Unknown error")
        }
        this.client = client
        this.status = DownloadTaskStatusInformer(client).apply {
            listeners.add(this@ClientInstance::statusListener)
        }
        refreshSectionsAndSongs()
        refreshAlbums()

        val request =
            DownloadRequest("www.url.com", "Test name", "Test artist", "test album", "Test section")

        val status = TaskStatus(request, SongDownloadStatus.DOWNLOADING, 0.5)

        requests = mapOf(request to status)

        return Pair(true, "Ok")
    }

    fun disconnect() {
        client?.let {
            status?.stop()
            it.apiClient.close()
            it.close()

            sections = emptyList()
            albums = emptyList()
            songs = emptyMap()
            //images = emptyMap() we don't clear the images to save mobile data!
            requests = emptyMap()
        }
        client = null
    }

    suspend fun refreshSections() {
        sections = client!!.getSections()
    }

    suspend fun refreshAlbums() {
        albums = client!!.getAlbums()
    }

    suspend fun refreshSectionsAndSongs() {
        songs = client!!.getSectionsAndSongs()
        sections = songs.keys.toList()
    }

    suspend fun refreshSongs(section: String) {
        val songs = client!!.getSongs<List<Song>>(section)
    }

    fun isConnected(): Boolean {
        val c = client ?: return false
        if (!c.apiClient.isActive) {
            client = null
            return false
        }
        return true
    }

    fun getOrLoadImage(album: String, loadScope: CoroutineScope): MutableState<ImageBitmap?> {
        images[album]?.let { return it }
        val state = mutableStateOf<ImageBitmap?>(null)
        images = images + (album to state)

        loadScope.launch {
            val array = client!!.getAlbumImage(album) ?: return@launch
            val bitmap = BitmapFactory.decodeByteArray(array, 0, array.size)
                ?: return@launch
            val image = bitmap.asImageBitmap()
            state.value = image
        }

        return state
    }

    fun checkStatusInformer() {
        val s = status
        val c = client
        if (c != null && (s == null || !s.running)) {
            status = DownloadTaskStatusInformer(c)
        }
    }

    private fun statusListener(status: TaskStatus) {
        requests = requests + (status.request to status)
    }

}