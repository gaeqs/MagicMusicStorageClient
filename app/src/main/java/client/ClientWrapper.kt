package client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
private data class LoginUser(val username: String, val password: String)

class ClientWrapper(val url: String) {

    private val fullUrl = "http://$url/"

    val tokenClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    val apiClient = HttpClient(CIO) {
        install(Auth) {
            bearer {
                loadTokens { refreshToken() }
                refreshTokens { refreshToken() }
            }
        }
    }

    private suspend fun refreshToken(): BearerTokens? {
        val response: TokenInfo = tokenClient.post {
            url(fullUrl + "login")
            contentType(ContentType.Application.Json)
            body = LoginUser("jetbrains", "foobaru")
        }
        return BearerTokens(response.token, response.token)
    }

    init {
        runBlocking {
            println(apiClient.get<String>(fullUrl + "api"))
            val raw = apiClient.get<HttpResponse>(fullUrl + "api/get/songs") {
                parameter("section", "patata")
            }
            println(raw.request.url.encodedPath)

            //val result = Json.decodeFromString<List<Song>>(raw.content)
            //println(result)

        }
    }
}


@Serializable
data class TokenInfo(val token: String)

@Serializable
data class Song(val name: String, var artist: String, val album: String)