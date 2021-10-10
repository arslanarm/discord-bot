package me.plony.bot.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ImplConfig(
    override val messages: Messages,
    override val privateChannels: List<PrivateChannelConfig>
    ) : Config

fun parseConfig(): Config =
    Json.decodeFromString<ImplConfig>(File("config.json").readText())

interface Config {
    val messages: Messages
    val privateChannels: List<PrivateChannelConfig>
    companion object : Config by parseConfig()
}