package io.github.gaeqs.ytdownloader.client

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.github.gaeqs.ytdownloader.util.bitmapFromUri
import io.github.gaeqs.ytdownloader.util.createAlbumImageFile
import io.github.gaeqs.ytdownloader.util.getAlbumImageFile
import io.ktor.network.sockets.*
import kotlinx.coroutines.launch

object ImageCache {

    var images by mutableStateOf(mapOf<String, MutableState<ImageBitmap?>>())

    private var refreshingStates = mutableListOf<String>()

    fun getOrLoadImage(album: String, context: Context): MutableState<ImageBitmap?> {
        images[album]?.let {
            if (it.value != null) {
                refreshSong(album, it, context)
            }
            return it
        }

        val state = mutableStateOf<ImageBitmap?>(null)
        images = images + (album to state)
        refreshSong(album, state, context)
        return state
    }

    fun forceRefresh(album: String, context: Context) {
        images[album]?.let {
            it.value = null
            refreshSong(album, it, context)
        }
    }

    fun refreshSong(album: String, state: MutableState<ImageBitmap?>, context: Context) {
        if (album in refreshingStates) return
        refreshingStates += album

        ClientInstance.ioScope.launch {
            val imageFromFile = context.getAlbumImageFile(album)
            if (imageFromFile != null) {
                val date: Long = ClientInstance.client!!.getAlbumCoverModificationDate(album) {
                    ClientInstance.uiScope.launch { refreshingStates -= album }
                    it.printStackTrace()
                    return@launch
                }
                if (date < imageFromFile.lastModified()) {
                    val imageUri = Uri.fromFile(imageFromFile)
                    val bitmap = context.bitmapFromUri(imageUri)
                    ClientInstance.uiScope.launch {
                        refreshingStates -= album
                        state.value = bitmap
                    }
                    return@launch
                }
            }

            val array = ClientInstance.client!!.getAlbumImage(album) ?: return@launch
            val bitmap = BitmapFactory.decodeByteArray(array, 0, array.size)
                ?: return@launch
            val image = bitmap.asImageBitmap()
            context.createAlbumImageFile(album, image)
            ClientInstance.uiScope.launch {
                refreshingStates -= album
                state.value = image
            }
        }
    }

}