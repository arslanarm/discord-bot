package me.plony.bot


import dev.kord.core.Kord
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.Job
import me.plony.processor.ServiceManager


fun Kord.server(port: Int, serviceManager: ServiceManager, wait: Boolean = false) = embeddedServer(CIO, port = port) {
    install(ContentNegotiation) { json() }
    routing {
        route("services") {
            get {
                call.respond(serviceManager.serviceList.map { it.name })
            }
            get("reload") {
                serviceManager.restartAll()
            }
        }
    }
}.start(wait = wait)