package me.plony.bot

import createServices
import dev.kord.core.Kord
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import io.ktor.client.*
import io.ktor.client.engine.cio.*

@OptIn(PrivilegedIntent::class)
suspend fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: System.getenv("TOKEN") ?: error("Token is not specified")
    val port = args.getOrNull(1)?.toIntOrNull() ?: 7070
    val kord = Kord(token) {
        intents = Intents {
            +Intent.GuildVoiceStates
            +Intent.GuildMembers
            +Intent.GuildMessages
            +Intent.GuildMessageReactions
        }
        httpClient = HttpClient(CIO) {
            engine {
                threadsCount = 20
            }
        }
        defaultStrategy = EntitySupplyStrategy.rest
    }
    with(kord) {
        val serviceManager = createServices()
        server(port, serviceManager)
        login()
    }
}