package me.plony.bot.config

import kotlinx.serialization.Serializable

@Serializable
data class PrivateChannelConfig(
    val createChannel: Long,
    val createCategory: Long
)