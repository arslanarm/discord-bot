
package me.plony.bot

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.on
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import me.plony.bot.script.startServices

suspend fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: error("Token must be specified")
    val port = args.getOrNull(1)?.toIntOrNull() ?: error("Port must be specified as a number")
    val kord = Kord(token) {
        httpClient = HttpClient(CIO) {
            engine { threadsCount = 20 }
        }
    }
    with (kord) {
        server(port)
        on<ReadyEvent> { startServices() }
        login()
    }
}