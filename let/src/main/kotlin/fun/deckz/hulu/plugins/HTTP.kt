package `fun`.deckz.hulu.plugins

import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.application.*

fun Application.configureHTTP() {
    install(ConditionalHeaders)
    routing {
        openAPI(path = "openapi")
    }
    routing {
        swaggerUI(path = "openapi")
    }
}
