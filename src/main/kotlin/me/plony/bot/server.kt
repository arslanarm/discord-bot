package me.plony.bot

import dev.kord.core.Kord
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.websocket.*

private val services = mutableSetOf<Service>()

fun service(name: String, author: String, setup: Kord.() -> Unit) {
    services.add(Service(name, author, setup))
}

fun server(port: Int, wait: Boolean = false) = embeddedServer(CIO, port = port) {
    install(WebSockets)
    install(ContentNegotiation) { json() }
    routing {
        get("services") {
            call.respond(services)
        }
    }
}.start(wait = wait)