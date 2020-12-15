package me.plony.processor


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

data class Service(
    val receiver: DiscordReceiver,
    val name: String,
    val execution: suspend DiscordReceiver.() -> Unit
) {
    fun start() = receiver.launch { receiver.execution() }
    fun cancel() = receiver.cancel()
    fun restart() {
        cancel()
        start()
    }
}