package `fun`.deckz.hulu.let.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.conditionalheaders.*

fun Application.configureHTTP() {
    install(ConditionalHeaders)
}
