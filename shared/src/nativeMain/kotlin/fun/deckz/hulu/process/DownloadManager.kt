package `fun`.deckz.hulu.process

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.cinterop.toCValues
import kotlinx.serialization.json.Json
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite
import platform.posix.rename

object DownloadManager {
    private const val DOWNLOADING_SUFFIX: String = ".downloading"

    private val client = HttpClient(CIO) {
        defaultRequest {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json(Json {
                encodeDefaults = true
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
    }

    suspend fun download(src: String, dest: String, md5: String?) {
        val downloading = dest + DOWNLOADING_SUFFIX
        downloadFile(src, downloading, md5)
        rename(downloading, dest)
    }

    private suspend fun downloadFile(fileUrl: String, destPath: String, md5: String?) {
        val fileBytes: ByteArray = client.get { url(fileUrl) }.body()
        val downloadFile = fopen(destPath, "w") ?: throw RuntimeException("can't open download file path")
        fwrite(fileBytes.toCValues(), fileBytes.size.toULong(), 1, downloadFile)
        fclose(downloadFile)
        // TODO: check md5, not now, because of kotlin native has no openssl library to make md5. crypt()
        if (md5 != null) {
            checkMd5() // TODO
        }
    }

    private fun checkMd5() {
        TODO("Not yet implemented")
    }

}