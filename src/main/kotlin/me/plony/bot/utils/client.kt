package me.plony.bot.utils

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*

val client = HttpClient {
    install(JsonFeature) {
        serializer = KotlinxSerializer()
    }
}