package me.plony.bot

import dev.kord.core.Kord
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
data class Service(val name: String, val author: String, @Transient val setup: Kord.() -> Unit = {})