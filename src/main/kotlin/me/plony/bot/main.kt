
package me.plony.bot

import com.gitlab.kordlib.core.Kord
import createServices
import io.ktor.client.*
import io.ktor.client.engine.cio.*

suspend fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: error("Token must be specified")
    val port = args.getOrNull(1)?.toIntOrNull() ?: error("Port must be specified as a number")
    val kord = Kord(token) {
        httpClient = HttpClient(CIO) {
            engine { threadsCount = 20 }
        }
    }
    with (kord) {
        val serviceManager = createServices()
        server(port, serviceManager)
        login()
    }
}