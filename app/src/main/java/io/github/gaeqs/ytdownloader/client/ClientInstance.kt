package io.github.gaeqs.ytdownloader.client

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.github.gaeqs.ytdownloader.util.createAlbumImageFile
import io.github.gaeqs.ytdownloader.util.getImageFile
import io.ktor.client.features.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*

object ClientInstance {

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)

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

        return Pair(true, "Ok")
    }

    fun disconnect() {
        client?.let {
            status?.stop()
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

    fun getOrLoadImage(
        album: String,
        context: Context
    ): MutableState<ImageBitmap?> {
        images[album]?.let { return it }
        val state = mutableStateOf<ImageBitmap?>(null)
        images = images + (album to state)

        ioScope.launch {
            val imageFromFile = context.getImageFile(album)
            if (imageFromFile != null) {
                uiScope.launch { state.value = imageFromFile }
                return@launch
            }

            val array = client!!.getAlbumImage(album) ?: return@launch
            val bitmap = BitmapFactory.decodeByteArray(array, 0, array.size)
                ?: return@launch
            val image = bitmap.asImageBitmap()

            context.createAlbumImageFile(album, image)

            uiScope.launch { state.value = imageFromFile }
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