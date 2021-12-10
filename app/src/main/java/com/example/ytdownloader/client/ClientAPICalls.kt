package com.example.ytdownloader.client

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class SectionWrapper(val name: String)

@Serializable
data class DownloadRequest(
    val url: String,
    val name: String,
    val artist: String,
    val album: String,
    val section: String
)

enum class SongDownloadStatus {

    QUEUED,
    FETCHING,
    DOWNLOADING,
    CONVERTING,
    NORMALIZING,
    ENHANCING,
    FINISHED,
    ERROR,
    CANCELLED

}

suspend inline fun <reified T : String?> ClientWrapper.apiTest(
    onResponseException: (Exception) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.get(host = host, port = port, path = "/api")
    } catch (ex: Exception) {
        onResponseException(ex)
    }
}

suspend inline fun <reified T : List<Song>?> ClientWrapper.getSongs(
    section: String,
    onResponseException: (Exception) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.get(host = host, port = port, path = "/api/get/songs") {
            parameter("section", section)
        }
    } catch (ex: Exception) {
        onResponseException(ex)
    }
}

suspend inline fun <reified T : Map<String, List<Song>>?> ClientWrapper.getSectionsAndSongs(
    onResponseException: (Exception) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.get(host = host, port = port, path = "/api/get/sectionsAndSongs")
    } catch (ex: Exception) {
        onResponseException(ex)
    }
}


suspend inline fun <reified T : List<String>?> ClientWrapper.getSections(
    onResponseException: (Exception) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.get(host = host, port = port, path = "/api/get/sections")
    } catch (ex: Exception) {
        onResponseException(ex)
    }
}

suspend inline fun <reified T : List<String>?> ClientWrapper.getAlbums(
    onResponseException: (Exception) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.get(host = host, port = port, path = "/api/get/albums")
    } catch (ex: Exception) {
        onResponseException(ex)
    }
}

suspend inline fun ClientWrapper.getAlbumImage(
    album: String,
    onResponseException: (Exception) -> ByteArray? = { ex -> throw ex }
): ByteArray? {
    return try {
        apiClient.get(host = host, port = port, path = "/api/get/albumCover") {
            parameter("album", album)
        }
    } catch (ex: Exception) {
        onResponseException(ex)
    }
}


suspend inline fun <reified T> ClientWrapper.postAlbum(
    album: String,
    image: File,
    onResponseException: (Exception) -> T = { ex -> throw ex }
): T {
    val albumData = formData {
        append("header", """{"name":"$album"}""")
        append("image", "a", ContentType.Image.PNG) {
            writeFully(image.inputStream().readBytes())
        }
    }

    return try {
        apiClient.submitFormWithBinaryData(formData = albumData) {
            path("/api/post/album")
        }
    } catch (ex: Exception) {
        onResponseException(ex)
    }
}

suspend inline fun <reified T> ClientWrapper.postSection(
    section: String,
    onResponseException: (Exception) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.post(host = host, port = port, path = "/api/post/section") {
            contentType(ContentType.Application.Json)
            body = SectionWrapper(section)
        }
    } catch (ex: Exception) {
        onResponseException(ex)
    }
}

suspend inline fun <reified T> ClientWrapper.postRequest(
    request: DownloadRequest,
    onResponseException: (Exception) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.post(host = host, port = port, path = "/api/post/request") {
            contentType(ContentType.Application.Json)
            body = request
        }
    } catch (ex: Exception) {
        onResponseException(ex)
    }
}