package `fun`.deckz.hulu.let

import `fun`.deckz.hulu.let.plugins.configureHTTP
import `fun`.deckz.hulu.let.plugins.configureMonitoring
import `fun`.deckz.hulu.let.plugins.configureRouting
import `fun`.deckz.hulu.let.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO, port = 8282, host = "127.0.0.1", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
