package io.github.gaeqs.ytdownloader.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File


fun Context.getImageFile(album: String): ImageBitmap? {
    val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val albumsDirectory = File(directory, "albums")
    if (!albumsDirectory.exists()) return null

    val albumFile = File(albumsDirectory, "$album.png")
    if (!albumFile.exists()) return null

    val imageUri = Uri.fromFile(albumFile)
    return bitmapFromUri(imageUri)
}

fun Context.bitmapFromUri(uri: Uri): ImageBitmap {
    return if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(contentResolver, uri)
    } else {
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(contentResolver, uri)
        )
    }.asImageBitmap()
}

fun Context.createAlbumImageFile(album: String, bitmap: ImageBitmap) {
    val directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val albumsDirectory = File(directory, "albums")
    if (!albumsDirectory.exists()) {
        albumsDirectory.mkdirs()
    }

    val albumFile = File(albumsDirectory, "$album.png")
    if (albumFile.exists()) {
        error("Image already exist!")
    }

    if (!albumFile.createNewFile()) {
        error("Cannot create file!")
    }

    val os = albumFile.outputStream()
    bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 0, os)
    os.close()
}