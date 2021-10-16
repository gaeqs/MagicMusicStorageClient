package com.example.ytdownloader.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class LoginUser(val username: String, val password: String)

@Serializable
data class TokenInfo(val token: String)

@Serializable
data class Song(val name: String, var artist: String, val album: String)

class ClientWrapper(url: String) {

    var loginInfo: LoginUser? = null

    val tokenClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    val apiClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(Auth) {
            bearer {
                loadTokens { refreshToken() }
                refreshTokens { refreshToken() }
            }
        }
    }

    val fullUrl = "http://$url/"

    private suspend fun refreshToken(): BearerTokens? {
        val login = loginInfo ?: return null
        val response: TokenInfo = tokenClient.post {
            url(fullUrl + "login")
            contentType(ContentType.Application.Json)
            body = login
        }
        return BearerTokens(response.token, response.token)
    }

    suspend fun logout(clearLoginInfo: Boolean) {
        if (clearLoginInfo) loginInfo = null
        val auth = apiClient.feature(Auth) ?: return
        auth.providers.forEach { if (it is BearerAuthProvider) it.clearToken() }
    }
}