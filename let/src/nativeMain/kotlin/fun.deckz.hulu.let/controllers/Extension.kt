package `fun`.deckz.hulu.let.controllers

import `fun`.deckz.hulu.api.common.HuluResponse
import `fun`.deckz.hulu.api.extension.InstallExtensionRequest
import `fun`.deckz.hulu.api.extension.ListExtensionRequest
import `fun`.deckz.hulu.api.extension.ListExtensionResponse
import `fun`.deckz.hulu.let.repositoy.ExtensionRepository
import `fun`.deckz.hulu.process.Process
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.configureExtension() {
    val logger = KotlinLogging.logger {}

    routing {
        route("extension") {
            post("list") {
                val request: ListExtensionRequest = call.receive()
                logger.info { Json.encodeToString(request) }
                call.respond(
                    HuluResponse.of(
                        ListExtensionResponse(
                            extensions = ExtensionRepository.extensions.values.map { it.toExtension() }.toList()
                        )
                    )
                )
            }

            post("install") {
                val request: InstallExtensionRequest = call.receive()
                logger.info { Json.encodeToString(request) }
                val extension = ExtensionRepository.extensions[request.name]
                if (extension == null) {
                    call.respond(HuluResponse.failure(1500, "can't find extension")) // TODO: can't find extension
                    return@post
                }
                CoroutineScope(Dispatchers.Default).launch {
                    Process.start(extension.deploy.install.command, extension.deploy.install.argv)
                }
                call.respond(HuluResponse.success())
            }
        }
    }
}


