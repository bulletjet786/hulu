package `fun`.deckz.hulu.let

import `fun`.deckz.hulu.let.controllers.configureExtension
import `fun`.deckz.hulu.let.extensions.configureExtensionDaemon
import `fun`.deckz.hulu.let.plugins.configureException
import `fun`.deckz.hulu.let.plugins.configureHTTP
import `fun`.deckz.hulu.let.plugins.configureMonitoring
import `fun`.deckz.hulu.let.plugins.configureSerialization
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.z4kn4fein.semver.Version
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import platform.posix.exit

private val logger = KotlinLogging.logger {}

private val myVersion = Version(
    major = 0, minor = 0, patch = 2
)

fun main(argv: Array<String>) {
    if (argv.isNotEmpty()) {
        if (argv[0] == "version") {
            print(myVersion)
            exit(0)
        }
    }

    embeddedServer(CIO, port = 8282, host = "127.0.0.1", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureException()
    configureExtension()
    configureExtensionDaemon()
}
