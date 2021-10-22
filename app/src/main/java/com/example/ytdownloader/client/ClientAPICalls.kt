package com.example.ytdownloader.client

import io.ktor.client.features.*
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

suspend inline fun <reified T : String?> ClientWrapper.apiTest(
    onResponseException: (ClientRequestException) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.get(fullUrl + "api")
    } catch (ex: ClientRequestException) {
        onResponseException(ex)
    }
}

suspend inline fun <reified T : List<Song>?> ClientWrapper.getSongs(
    section: String,
    onResponseException: (ClientRequestException) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.get(fullUrl + "api/get/songs") {
            parameter("section", section)
        }
    } catch (ex: ClientRequestException) {
        onResponseException(ex)
    }
}

suspend inline fun <reified T> ClientWrapper.postAlbum(
    album: String,
    image: File,
    onResponseException: (ClientRequestException) -> T = { ex -> throw ex }
): T {
    val albumData = formData {
        append("header", """{"name":"$album"}""")
        append("image", "a", ContentType.Image.PNG) {
            writeFully(image.inputStream().readBytes())
        }
    }

    return try {
        apiClient.submitFormWithBinaryData(formData = albumData) {
            url(fullUrl + "api/post/album")
        }
    } catch (ex: ClientRequestException) {
        onResponseException(ex)
    }
}

suspend inline fun <reified T> ClientWrapper.postSection(
    section: String,
    onResponseException: (ClientRequestException) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.post(fullUrl + "api/post/section") {
            contentType(ContentType.Application.Json)
            body = SectionWrapper(section)
        }
    } catch (ex: ClientRequestException) {
        onResponseException(ex)
    }
}

suspend inline fun <reified T> ClientWrapper.postRequest(
    request: DownloadRequest,
    onResponseException: (ClientRequestException) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.post(fullUrl + "api/post/request") {
            contentType(ContentType.Application.Json)
            body = request
        }
    } catch (ex: ClientRequestException) {
        onResponseException(ex)
    }
}