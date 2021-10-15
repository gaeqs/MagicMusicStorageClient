package client

import io.ktor.client.features.*
import io.ktor.client.request.*

suspend inline fun <reified T : String?> ClientWrapper.apiTest(
    onResponseException: (ClientRequestException) -> T = { ex -> throw ex }
): T {
    return try {
        apiClient.get(fullUrl + "api")
    } catch (ex: ClientRequestException) {
        onResponseException(ex)
    }
}

suspend inline fun <reified T : String?> ClientWrapper.getSongs(
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