package me.plony.bot.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File

fun <T> config(serializer: KSerializer<T>, path: String): T = Json.decodeFromString(serializer, File("data/$path/config.json").readText())