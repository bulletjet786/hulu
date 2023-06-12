package `fun`.deckz.hulu.starter

import `fun`.deckz.hulu.api.common.HuluResponse
import `fun`.deckz.hulu.api.version.VersionLatestRequest
import `fun`.deckz.hulu.api.version.VersionLatestResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.z4kn4fein.semver.Version
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import platform.posix.*
import kotlin.native.concurrent.AtomicInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val myVersion = Version(
    major = 0,
    minor = 0,
    patch = 1
)
private val logger = KotlinLogging.logger("Main")
private val UPDATE_DURATION: Duration = 10.minutes
private val LET_SERVICE_DURATION: Duration = 10.seconds

private val CATCH_SIGNALS = listOf(SIGABRT, SIGFPE, SIGILL, SIGINT, SIGSEGV, SIGTERM)

fun main() = runBlocking {
    logger.info { "start, version is $myVersion" }

    PackagerManager.run()
}

fun exitSigHandler(sig: Int) {
    ProcessManager.removePidFile(PackagerManager.MY_PID_FILE)
}

object PackagerManager {
    private val logger = KotlinLogging.logger {}

    //    private const val WORK_DIR: String = "/opt/fun.deckz/hulu"
    private const val WORK_DIR: String = "/home/deck/tmp/fun.deckz/hulu"
    private const val WORK_BIN_DIR: String = "$WORK_DIR/bin"
    private const val WORK_VAR_DIR: String = "$WORK_DIR/var"
    const val MY_PID_FILE: String = "$WORK_VAR_DIR/starter.pid"
    private const val DOWNLOADING_SUFFIX: String = ".downloading"

    private const val huluHost: String = "localhost"
    private const val huluPort: Int = 8181

    private val guardCheckChannel: Channel<Unit> = Channel()
    private val huluLetPid: AtomicInt = AtomicInt(0) // 0 for no let service process

    private val client = HttpClient(CIO) {
        defaultRequest {
            host = huluHost
            port = huluPort
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json(
                Json {
                    encodeDefaults = true
                    prettyPrint = true
                    ignoreUnknownKeys = true
                }
            )
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
    }

    fun run() {
        startDurationDaemon(LET_SERVICE_DURATION) {
            letServiceGuardTicker()
        }
        startDaemon {
            letServiceGuard()
        }
        startDurationDaemon(UPDATE_DURATION) {
            updateVersionGuard()
        }

    }

    private fun startDurationDaemon(duration: Duration, daemonFunc: suspend () -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                try {
                    daemonFunc()
                } catch (e: Exception) {
                    logger.error { "found exception on startDurationDaemon: ${e.message}" }
                }
                delay(duration)
            }
        }
    }

    private fun startDaemon(daemonFunc: suspend () -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                try {
                    daemonFunc()
                } catch (e: Exception) {
                    logger.error { "found exception on startDaemon: ${e.message}" }
                }
            }
        }
    }

    private suspend fun updateVersionGuard() {
        // get local version for starter and hulu
        logger.info { "start update version daemon" }
        val localHuluVersions = getHuluLocalVersions()
        val localHuluLatestVersion = getHuluLocalLatestVersion(localHuluVersions)
        val localStarterVersion = myVersion
        logger.info { "found local version: $localHuluLatestVersion, $localStarterVersion" }

        // get remote version for starter and hulu
        val remoteVersionResponse = getRemoteVersion(localStarterVersion, localHuluLatestVersion)
        val remoteHuluVersion = Version.parse(remoteVersionResponse.huluVersion)
        val remoteStarterVersion = Version.parse(remoteVersionResponse.starterVersion)
        logger.info { "found remote version: $remoteHuluVersion, $remoteStarterVersion" }

        // for hulu package, we keep the latest three version.
        if (remoteHuluVersion > localHuluLatestVersion) {
            updateHulu(remoteVersionResponse)
            if (localHuluVersions.size == 3) {
                removeOldVersion(localHuluVersions.subList(2, localHuluVersions.size - 1))
                restartHulu()
            }
        }

        // for starter, we keep it the to latest.
        if (remoteStarterVersion > localStarterVersion) {
            updateStarter(remoteVersionResponse)
//            restartStarter()  // TODO:
        }
    }

    private suspend fun letServiceGuard() {
        for (unit in guardCheckChannel) {
            // check whether the let service alive
            memScoped {
                val status: IntVar = alloc<IntVar>()
                // hulu let don't start or has been exit, we restart it.
                if (huluLetPid.value == 0 || waitpid(huluLetPid.value, status.ptr, WNOHANG) != 0) {
                    if (huluLetPid.value != 0) {
                        logger.error { "hulu let has been exist, because of exitStatus=" + ((status.value shl 8) and 0xFF) }
                    }
                    // restart let service
                    huluLetPid.value = ProcessManager.startCmd("$WORK_BIN_DIR/let.kexe", null) ?: throw RuntimeException("start let service failed")
                }
            }
        }
    }

    private suspend fun letServiceGuardTicker() {
        guardCheckChannel.send(Unit)
    }

    private suspend fun restartHulu() {
        // we just stop the let service, and then send signal to guardCheckChannel
        kill(huluLetPid.value, SIGILL)
        guardCheckChannel.send(Unit)
    }

    private fun restartStarter() {
        execl("systemctl", "daemon-restart")
        execl("systemctl", "restart")   // TODO:
    }

    private fun removeOldVersion(removeVersions: List<Version>) {
        removeVersions.forEach {
            unlink("$WORK_BIN_DIR/$it")
        }
    }

    private suspend fun updateStarter(remoteVersionResponse: VersionLatestResponse) {
        val destFilePath = "$WORK_DIR/starter.kexe"
        val downloadingFilePath = destFilePath + DOWNLOADING_SUFFIX
        downloadFile(remoteVersionResponse.starterDownloadUrl, downloadingFilePath)
        rename(downloadingFilePath, destFilePath)
    }

    private suspend fun updateHulu(remoteVersionResponse: VersionLatestResponse) {
        // download hulu package
        val letDownloadUrl = remoteVersionResponse.huluDownloadUrl + "/let.kexe"
        val padDownloadUrl = remoteVersionResponse.huluDownloadUrl + "/pad.kexe"
        val destDirPath = WORK_BIN_DIR + "/" + remoteVersionResponse.huluVersion
        val downloadingDirPath = destDirPath + DOWNLOADING_SUFFIX
        mkdir(
            downloadingDirPath,
            (S_IRWXU or S_IRGRP or S_IWGRP or S_IROTH or S_IWOTH).toUInt()
        )
        downloadFile(letDownloadUrl, "$downloadingDirPath/let.kexe")
        downloadFile(padDownloadUrl, "$downloadingDirPath/pad.kexe")
        rename(downloadingDirPath, destDirPath)
    }

    private fun getPids() {
        ProcessInfo(
            starterPid = getpid(),
            letPid = ProcessManager.getPidFromPidFile("$WORK_VAR_DIR/let.pid"),
            padPid = ProcessManager.getPidFromPidFile("$WORK_VAR_DIR/pad.pid"),
        )
    }

    private fun getHuluLocalVersions(): List<Version> {
        val packageDir = opendir(WORK_BIN_DIR) ?: throw RuntimeException("can't open WORK_BIN_DIR")
        val versionDirs = mutableListOf<String>()
        do {
            val readDir = readdir(packageDir) ?: break;
            if (readDir.pointed.d_name.toKString() == "." || readDir.pointed.d_name.toKString() == ".."
                || readDir.pointed.d_name.toKString().endsWith(DOWNLOADING_SUFFIX)
            ) {
                continue
            }
            versionDirs.add(readDir.pointed.d_name.toKString())
        } while (true)
        return versionDirs.map {
            Version.parse(it, strict = true)
        }.sorted().reversed();
    }

    private fun getHuluLocalLatestVersion(localVersions: List<Version>): Version {
        if (localVersions.isEmpty()) {
            throw RuntimeException("can't found local versions")
        }
        return localVersions[0]
    }

    private suspend fun getRemoteVersion(starterVersion: Version, huluVersion: Version): VersionLatestResponse {
        // get remote latest version
        val versionLatest: HuluResponse<VersionLatestResponse> = client.post {
            url("/version/latest")
            setBody(
                VersionLatestRequest(
                    myStarterVersion = starterVersion.toString(),
                    myHuluVersion = huluVersion.toString()
                )
            )
        }.body()
        if ((versionLatest.status.code) != 0) {
            throw RuntimeException("status code not zero")
        }
        return versionLatest.data!!;
    }

    private suspend fun downloadFile(fileUrl: String, destPath: String) {
        val fileBytes: ByteArray = client.get { url(fileUrl) }.body()
        val downloadFile = fopen(destPath, "w") ?: throw RuntimeException("can't open download file path")
        fwrite(fileBytes.toCValues(), fileBytes.size.toULong(), 1, downloadFile)
        fclose(downloadFile)
        // TODO: check md5, not now, because of kotlin native has no openssl library to make md5. crypt()
    }
}

object NameGenerator {
    private val alphaPool = ('a'..'z')

    fun randomAlphaName(count: Int): String {
        val result = ""
        repeat(count) {
            result + alphaPool.random()
        }
        return result
    }
}

class ProcessInfo(
    starterPid: Int,
    letPid: Int?,
    padPid: Int?
)

object ProcessManager {

    private val logger = KotlinLogging.logger {}

    fun getCurrentPid(): Int {
        return getpid()
    }

    fun getPidFromPidFile(filePath: String): Int? {
        val pidFile = fopen(filePath, "r")
        return if (pidFile == null) {
            null
        } else {
            val pid = memScoped {
                val pidBuffer: CArrayPointer<ByteVar> = nativeHeap.allocArray<ByteVar>(32)
                fread(pidBuffer, 32, 1, pidFile)
                pidBuffer.toKString()
            }
            pid.toInt()
        }
    }

    fun writePidToPidFile(pid: Int, filePath: String) {
        val pidFile = fopen(filePath, "w")
        if (pidFile == null) {
            throw RuntimeException("can't open filePath=$filePath, because of $errno")
        } else {
            fwrite(pid.toString().cstr, pid.toString().length.toULong(), 1, pidFile)
            logger.info { "write pid=$pid to $filePath" }
            fclose(pidFile)
        }
    }

    fun removePidFile(filePath: String) {
        unlink(filePath)
    }

    fun startCmd(cmd: String, args: Array<String>?): Int? {
        val ret = fork()
        if (ret < 0) {
            logger.error { "start let service failed" }
            return null
        }
        return if (ret == 0) {   // for child process
//            memScoped {
//                val cArgs = allocArray<ByteVar>(args.size + 1)
//                args.forEachIndexed(
//                    cArgs[i]
//                )
//            }
            if (args == null) {
                execv(cmd, null)
            }
            0
        } else {    // for parent process
            ret
        }
    }
}