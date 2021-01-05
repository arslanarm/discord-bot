package me.plony.bot

import createServices
import dev.kord.core.Kord
import io.ktor.client.*
import io.ktor.client.engine.cio.*

suspend fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: System.getenv("TOKEN") ?: error("Token is not specified")
    val port = args.getOrNull(1)?.toIntOrNull() ?: 7070
    val kord = Kord(token) {
        httpClient = HttpClient(CIO) {
            engine {
                threadsCount = 20
            }
        }
    }
    with(kord) {
        val serviceManager = createServices()
        server(port, serviceManager)
        login()
    }
}