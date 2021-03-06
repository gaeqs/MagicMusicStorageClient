package io.github.gaeqs.ytdownloader.client

import io.ktor.client.features.*
import kotlinx.coroutines.runBlocking

fun main() {
    val client = ClientWrapper("localhost", 25555)

    runBlocking {
        println(client.apiTest { "ERROR!" })

        client.loginInfo = LoginUser("jetbrains", "foobar")

        println(client.apiTest())
        println(client.getSections())
        println(client.getAlbums())
        println(client.getAlbumImage("test album")?.size)

        val informer = DownloadTaskStatusInformer(client)
        informer.listeners += { status -> println(status) }

        println(client.postSection<Any>("test section") { it })

        println(client.postRequest<Any>(
            DownloadRequest(
                "https://www.youtube.com/watch?v=Dr1S8DfNiKw&ab_channel=Turbo",
                "Dragostea Din Tei Eurobeat Remix",
                "Turbo",
                "test album",
                "test section"
            )
        ) { it })

        // Logouts BUT doesn't clear the login info.
        client.logout(clearLoginInfo = false)

        // Reconnects again because the login info was not cleared.
        println(client.getSongs("test section"))

        // Now logouts clearing the login info.
        client.logout(clearLoginInfo = true)

        // Fails!
        try {
            println(client.apiTest())
        } catch (ex: ClientRequestException) {
            println(ex.response)
        }

        informer.join()
    }
}