package `fun`.deckz.hulu.starter

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.z4kn4fein.semver.Version
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import platform.posix.opendir
import platform.posix.readdir

private val logger = KotlinLogging.logger {}

fun main() {
    PackagerManager.run()
}


object PackagerManager {

    const val WORK_DIR: String = "/opt/fun.deckz.hulu"
    const val WORK_BIN_DIR: String = "$WORK_DIR/bin"

    fun run() {
        // for starter, we keep it the to latest.

        // for hulu package, we keep the latest three version.
    }

    fun getLocalLatestVersion() {
        val localVersions = getLocalVersions()
        localVersions.forEach {
            logger.info { "found local version: $it" }
        }

    }

    fun getLocalVersions(): List<Version> {
        val packageDir = opendir(WORK_BIN_DIR) ?: throw RuntimeException("can't open WORK_BIN_DIR")
        val versionDirs = mutableListOf<String>()
        do {
            val readDir = readdir(packageDir) ?: break;
            if (readDir.pointed.d_name.toKString() == "." || readDir.pointed.d_name.toKString() == "..") {
                continue
            }
            versionDirs.add(readDir.pointed.d_name.toKString())
        } while (true)
        return versionDirs.map {
            Version.parse(it, strict = true)
        }.sorted().reversed();
    }

    suspend fun getRemoteVersion() {

        val client = HttpClient(CIO) {}

        // 获取一个 URL 的内容。
        val response = CoroutineScope(Dispatchers.Default).async {
            client.get { url("https://127.0.0.1:8181/api/version/latest") }
        }
        response.await().status

        client.close()
    }

    fun installHuluPackage() {

    }
}