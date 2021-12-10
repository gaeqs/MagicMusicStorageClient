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
import kotlinx.coroutines.launch

object ClientInstance {

    var client: ClientWrapper? = null

    var sections by mutableStateOf(listOf<String>())
        private set

    var albums by mutableStateOf(listOf<String>())

    var songs by mutableStateOf(mapOf<String, List<Song>>())

    var images by mutableStateOf(mapOf<String, MutableState<ImageBitmap?>>())

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
        refreshSectionsAndSongs()
        refreshAlbums()
        return Pair(true, "Ok")
    }

    fun disconnect() {
        client?.let {
            it.apiClient.close()
            it.close()
            sections = emptyList()
        }
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
        return client != null
    }

    fun getOrLoadImage(album: String, loadScope: CoroutineScope): MutableState<ImageBitmap?> {
        val state: MutableState<ImageBitmap?>
        images[album]?.let { return it }
        state = mutableStateOf(null)
        images = images + Pair(album, state)

        loadScope.launch {
            val array = client!!.getAlbumImage(album) ?: return@launch
            val bitmap = BitmapFactory.decodeByteArray(array, 0, array.size)
                ?: return@launch
            val image = bitmap.asImageBitmap()
            state.value = image
        }

        return state
    }

}