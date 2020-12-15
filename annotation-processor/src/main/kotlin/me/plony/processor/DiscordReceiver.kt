package me.plony.processor

import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.core.on
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

data class DiscordReceiver(
    val kord: Kord,
    val parentJob: Job = Job(),
    override val coroutineContext: CoroutineContext = kord.coroutineContext + parentJob
) : CoroutineScope

fun Kord.newDiscordReceiver() = DiscordReceiver(this)
inline fun <reified T: Event> DiscordReceiver.on(noinline block: suspend T.() -> Unit) = kord.on<T>(this, block)