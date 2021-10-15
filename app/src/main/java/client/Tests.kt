package client

import io.ktor.client.features.*
import kotlinx.coroutines.runBlocking

fun main() {
    val client = ClientWrapper("localhost:25555")

    runBlocking {
        println(client.apiTest { "ERROR!" })

        client.loginInfo = LoginUser("jetbrains", "foobar")

        println(client.apiTest())

        // Logouts BUT doesn't clear the login info.
        client.logout(clearLoginInfo = false)

        // Reconnects again because the login info was not cleared.
        println(client.getSongs("patata"))

        // Now logouts clearing the login info.
        client.logout(clearLoginInfo = true)

        // Fails!
        try {
            println(client.apiTest())
        } catch (ex: ClientRequestException) {
            println(ex.response)
        }
    }
}