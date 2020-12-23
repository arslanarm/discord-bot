package me.plony.bot.utils.globals

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.websocket.*

val client = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }
    install(WebSockets)
}