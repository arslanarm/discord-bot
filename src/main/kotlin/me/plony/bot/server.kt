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
import me.plony.bot.script.startServices

internal val services = mutableSetOf<Service>()

fun service(
    name: String,
    job: Job,
) = Service(name, job).also { services.add(it) }

fun Kord.server(port: Int, wait: Boolean = false) = embeddedServer(CIO, port = port) {
    install(ContentNegotiation) { json() }
    routing {
        route("services") {
            get {
                call.respond(services)
            }
            get("reload") {
                services.forEach { it.cancel() }
                startServices("./services")
            }
        }
    }
}.start(wait = wait)