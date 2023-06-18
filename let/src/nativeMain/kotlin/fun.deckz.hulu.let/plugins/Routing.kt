package `fun`.deckz.hulu.let.plugins

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureException() {
    val logger = KotlinLogging.logger {}

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error { "found exception: $cause" }
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
}

