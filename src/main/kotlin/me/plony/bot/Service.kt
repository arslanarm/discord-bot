package me.plony.bot

import dev.kord.core.Kord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class Service(
    val name: String,
    @Transient val coroutineScope: Job = Job()
)

suspend fun Service.cancel() {
    coroutineScope.cancelAndJoin()
    services.remove(this)
}