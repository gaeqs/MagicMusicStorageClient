package io.github.gaeqs.ytdownloader.work

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.github.gaeqs.ytdownloader.client.*
import kotlinx.coroutines.runBlocking

class SyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun doWork(): Result {
        println("STARTING")
        val section = inputData.getString("section") ?: return Result.failure()
        val folderPath = inputData.getString("folder") ?: return Result.failure()
        val folderUri = Uri.parse(folderPath)
        val cr = applicationContext.contentResolver

        val appClient = ClientInstance.client!!

        val client = ClientWrapper(appClient.host, appClient.port)
        client.loginInfo = appClient.loginInfo

        val document =
            DocumentFile.fromTreeUri(applicationContext, folderUri) ?: return Result.failure()

        runBlocking {
            try {
                val songs: List<Song> = client.getSongs(section)
                songs.forEach {
                    val data = client.getSong(section, it.name, it.album)
                    val name = "${it.album} - ${it.name}.mp3"
                    println("Downloading song $name")

                    var file = document.findFile(name)
                    if (file == null) {
                        file = document.createFile("audio/mp3", name)
                        if (file == null) {
                            println("CANNOT CREATE FILE $name")
                            return@forEach
                        }
                    }

                    val os = cr.openOutputStream(file.uri)!!
                    os.write(data)
                    os.close()
                }
            } finally {
                client.close()
            }
        }
        return Result.success()
    }
}