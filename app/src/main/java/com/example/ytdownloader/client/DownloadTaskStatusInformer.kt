package com.example.ytdownloader.client

import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class TaskStatus(
    val request: DownloadRequest = DownloadRequest("", "", "", "", ""),
    val status: SongDownloadStatus = SongDownloadStatus.ERROR,
    val percentage: Double
)

class DownloadTaskStatusInformer(private val client: ClientWrapper) {

    private var session: DefaultClientWebSocketSession? = null

    private val initMutex = Mutex()
    private var requestOnInit = false

    val listeners = ConcurrentList<suspend (TaskStatus) -> Unit>()

    val running get() = session != null

    private val job: Job = CoroutineScope(Dispatchers.Main).launch {
        client.apiClient.webSocket(
            host = client.host,
            port = client.port,
            path = "/api/socket/status"
        ) {
            initMutex.withLock {
                if (requestOnInit) {
                    send("all")
                }
                session = this
            }
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        manageText(frame.readText())
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
            } catch (e: CancellationException) {
                close(CloseReason(CloseReason.Codes.NORMAL, "Client stop"))
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                session = null
            }
        }
    }

    suspend fun requestAll() {
        initMutex.withLock {
            val ses = session
            if (ses != null) {
                ses.send("all")
            } else {
                requestOnInit = true
            }
        }
    }

    fun stop() {
        job.cancel()
    }

    suspend fun join() {
        job.join()
    }

    private suspend fun manageText(text: String) {
        try {
            val status = Json.decodeFromString<TaskStatus>(text)
            listeners.forEach { it(status) }
        } catch (ex: Throwable) {
            System.err.println("Couldn't decode TaskStatus $text!")
            ex.printStackTrace()
        }
    }

}