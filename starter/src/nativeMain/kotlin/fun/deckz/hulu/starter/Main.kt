package `fun`.deckz.hulu.starter

import `fun`.deckz.hulu.api.common.HuluResponse
import `fun`.deckz.hulu.api.version.*
import `fun`.deckz.hulu.process.Process
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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import platform.posix.*
import kotlin.native.concurrent.AtomicReference
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


private val myVersion = Version(
    major = 0, minor = 0, patch = 1
)

private val logger = KotlinLogging.logger("Main")
private val UPDATE_DURATION: Duration = 10.minutes
private val LET_SERVICE_DURATION: Duration = 10.seconds

private val CATCH_SIGNALS = listOf(SIGABRT, SIGFPE, SIGILL, SIGINT, SIGSEGV, SIGTERM)

fun main(argv: Array<String>) = runBlocking {
    if (argv.isNotEmpty()) {
        if (argv[0] == "version") {
            print(myVersion)
            exit(0)
        }
    }

    logger.info { "starter version is $myVersion" }
    ModuleManager.run()
}

object ModuleManager {

    private val logger = KotlinLogging.logger("ModuleManager")

    private const val WORK_DIR =  Environment.WORK_DIR

    private const val WORK_STARTER_DIR: String = "$WORK_DIR/starter"
    private const val WORK_LET_DIR: String = "$WORK_DIR/let"
    private const val WORK_PAD_DIR: String = "$WORK_DIR/pad"

    private const val WORK_DATA_DIR: String = "$WORK_DIR/data"
    private const val WORK_VAR_DIR: String = "$WORK_DIR/var"
    private const val WORK_ETC_DIR: String = "$WORK_DIR/etc"

    private const val huluHost: String = "150.158.135.143"
    private const val huluPort: Int = 8181

    private val guardCheckChannel: Channel<Unit> = Channel()
    private val letProcess: AtomicReference<Process?> = AtomicReference(null)

    private val client = HttpClient(CIO) {
        defaultRequest {
            host = huluHost
            port = huluPort
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

    suspend fun run() {
        startDurationDaemon(LET_SERVICE_DURATION) {
            letServiceGuardTicker()
        }
        startDaemon {
            letServiceGuard()
        }
        startDurationDaemon(UPDATE_DURATION) {
            updateVersionGuard()
        }
        awaitCancellation()
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
        val localHuluVersions = getModuleLocalVersion()
        logger.info { "found local version: ${localHuluVersions.starter.version} ${localHuluVersions.let.version} ${localHuluVersions.pad.version}" }
        val localStarterVersion = Version.parse(localHuluVersions.starter.version)
        val localLetVersion = Version.parse(localHuluVersions.let.version)
        val localPadVersion = Version.parse(localHuluVersions.pad.version)

        // get remote version for starter and hulu
        val remoteHuluLocations = getRemoteVersion(localHuluVersions)
        logger.info { "found remote version: ${remoteHuluLocations.starter.version} ${remoteHuluLocations.let.version} ${remoteHuluLocations.pad.version}" }
        val remoteStarterVersion = Version.parse(remoteHuluLocations.starter.version)
        val remoteLetVersion = Version.parse(remoteHuluLocations.let.version)
        val remotePadVersion = Version.parse(remoteHuluLocations.pad.version)

        if (remoteStarterVersion > localStarterVersion) {
            updateModule(remoteHuluLocations.starter)
//            restartStarter()
        }

        if (remoteLetVersion > localLetVersion) {
            updateModule(remoteHuluLocations.let)
            restartLet()
        }

        if (remotePadVersion > localPadVersion) {
            updateModule(remoteHuluLocations.pad)
        }
    }

    private suspend fun letServiceGuard() {
        for (unit in guardCheckChannel) {
            // check whether the let service alive
            // if let don't start or has been exit, we restart it.
            logger.info { "check let service ... " }
            if (letProcess.value == null || !letProcess.value!!.alive()) {
                logger.info { "found let process not exist, because={value=${letProcess.value}, alive={${letProcess.value?.alive()}}}, restart" }
                letProcess.value?.clear()
                // restart let service
                letProcess.value = Process.start("$WORK_LET_DIR/let.kexe", null, pipe = false) // TODO:
            }
        }
    }

    private suspend fun letServiceGuardTicker() {
        guardCheckChannel.send(Unit)
    }

    private suspend fun restartLet() {
        // we just kill the let service, and then send signal to guardCheckChannel
        letProcess.value?.kill()
        guardCheckChannel.send(Unit)
    }

    private fun restartStarter() {
        Process.systemCmd("systemctl daemon-restart")
        Process.systemCmd("systemctl restart ") // TODO:
    }

    private fun updateModule(starterModuleLocation: ModuleLocation) {
        Process.systemCmd(updateCmd(starterModuleLocation))
    }

    private fun getModuleLocalVersion(): HuluModuleVersion {
        val starter = Process.start("$WORK_STARTER_DIR/starter.kexe", arrayOf("version"), pipe = true)
        val let = Process.start("$WORK_LET_DIR/let.kexe", arrayOf("version"), pipe = true)
        val pad = Process.start("$WORK_PAD_DIR/bin/pad", arrayOf("version"), pipe = true)
        starter.waitExited()
        let.waitExited()
        pad.waitExited()

        return HuluModuleVersion(
            starter = ModuleVersion(name = ModuleName.STARTER, starter.readStdout()),
            let = ModuleVersion(name = ModuleName.LET, let.readStdout()),
            pad = ModuleVersion(name = ModuleName.PAD, pad.readStdout())
        )
    }

    private suspend fun getRemoteVersion(localHuluVersions: HuluModuleVersion): VersionLatestResponse {
        // get remote latest version
        val versionLatest = client.post {
            url("/version/latest")
            setBody(
                localHuluVersions
            )
        }.body<HuluResponse<VersionLatestResponse>>()
        if ((versionLatest.status.code) != 0) {
            throw RuntimeException("status code not zero")
        }
        return versionLatest.data!!
    }

    private fun updateCmd(location: ModuleLocation): String {
        return "cd $WORK_DIR/${location.name} && wget ${location.downloadUrl} " +
                "&& tar -zxf ${location.name}.tar.gz && rm -rf ${location.name}.tar.gz"
    }
}

