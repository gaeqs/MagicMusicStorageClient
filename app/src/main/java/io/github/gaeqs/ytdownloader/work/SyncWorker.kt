package io.github.gaeqs.ytdownloader.work

import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.github.gaeqs.ytdownloader.R
import io.github.gaeqs.ytdownloader.client.*
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

private val atomic = AtomicInteger()

private data class SongToDownload(val section: String, val name: String, val album: String)

class SyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun doWork(): Result {
        val section = inputData.getString("section")
        val album = inputData.getString("album")
        if (section == null && album == null) return Result.failure()

        val folderPath = inputData.getString("folder") ?: return Result.failure()
        val folderUri = Uri.parse(folderPath)
        val cr = applicationContext.contentResolver

        val appClient = ClientInstance.client!!

        val client = ClientWrapper(appClient.host, appClient.port)
        client.loginInfo = appClient.loginInfo

        val document =
            DocumentFile.fromTreeUri(applicationContext, folderUri) ?: return Result.failure()

        val notificationManager =
            applicationContext.getSystemService(NotificationManager::class.java)
        val builder = NotificationCompat.Builder(applicationContext, "yt_dl_sync")
            .setSmallIcon(R.drawable.ic_song)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)

        if(section != null) {
            builder.setContentTitle("Synchronizing section $section")
        } else {
            builder.setContentTitle("Synchronizing album $album")
        }


        val id = atomic.incrementAndGet()

        runBlocking {
            try {
                val songs = getSongsToDownload(client, section, album)
                songs.forEachIndexed { index, it ->
                    val data = client.getSong(it.section, it.name, it.album)
                    val name = "${it.album} - ${it.name}.mp3"

                    builder.setContentText("$index/${songs.size}\n$name")
                    builder.setSilent(index > 0)
                    notificationManager.notify(id, builder.build())

                    println("Downloading song $name")

                    var file = document.findFile(name)
                    if (file == null) {
                        file = document.createFile("audio/mp3", name)
                        if (file == null) {
                            println("CANNOT CREATE FILE $name")
                            return@forEachIndexed
                        }
                    }

                    val os = cr.openOutputStream(file.uri)!!
                    os.write(data)
                    os.close()

                    setProgressAsync(
                        workDataOf(
                            "section" to section,
                            "last" to name,
                            "progress" to index / songs.size.toFloat()
                        )
                    )
                }
            } finally {
                client.close()
            }
        }

        builder.setContentText("Synchronization completed")
        builder.setSilent(false).setOngoing(false)
        notificationManager.notify(id, builder.build())

        return Result.success()
    }

    private suspend fun getSongsToDownload(
        client: ClientWrapper,
        section: String?,
        album: String?
    ): List<SongToDownload> {
        return if (section != null) {
            val data: List<Song> = client.getSongs(section)
            data.map { SongToDownload(section, it.name, it.album) }
        } else {
            val data: Map<String, List<Song>> = client.getSectionsAndSongs()
            data.entries
                .flatMap { e -> e.value.map { SongToDownload(e.key, it.name, it.album) } }
                .filter { it.album == album }
        }

    }
}