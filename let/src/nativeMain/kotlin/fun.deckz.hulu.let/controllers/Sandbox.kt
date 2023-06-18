package `fun`.deckz.hulu.let.controllers

import `fun`.deckz.hulu.api.sandbox.CreateSandboxRequest
import `fun`.deckz.hulu.api.sandbox.CreateSandboxResponse
import `fun`.deckz.hulu.process.ProcessManager
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

fun Application.configureSandbox() {
    val logger = KotlinLogging.logger {}

    routing {
        route("/sandbox") {
            post("/create") {
                val request: CreateSandboxRequest = call.receive()
                logger.info { Json.encodeToString(request) }
                CoroutineScope(Dispatchers.Default).launch {
                    ProcessManager.startCmd(request.command, request.argv)
                }
                call.respond(CreateSandboxResponse(sandboxID = "abc"))
            }
        }
    }
}
