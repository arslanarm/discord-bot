package me.plony.bot.config

import kotlinx.serialization.Serializable

@Serializable
data class Messages(
    val joinedAt: String = "Вы присоединились %s"
)
