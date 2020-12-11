
package me.plony.bot

import dev.kord.core.Kord
import io.ktor.client.*

suspend fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: error("Token must be specified")
    val port = args.getOrNull(1)?.toIntOrNull() ?: error("Port must be specified as a number")
    val kord = Kord(token) {
        httpClient = HttpClient {
            engine { threadsCount = 20 }
        }
    }
    server(port)
    kord.login()
}
