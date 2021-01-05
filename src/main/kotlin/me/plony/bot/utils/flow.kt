package me.plony.bot.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

suspend inline fun <reified T, reified R> Flow<T>.asyncMap(coroutineScope: CoroutineScope, crossinline block: suspend (T) -> R) =
    map { coroutineScope.async { block(it) } }
        .map { it.await() }

suspend inline fun <reified T> Flow<T>.onEachLaunch(coroutineScope: CoroutineScope, crossinline block: suspend (T) -> Unit) =
    onEach { coroutineScope.launch { block(it) } }