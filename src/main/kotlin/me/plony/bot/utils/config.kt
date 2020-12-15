package me.plony.bot.utils

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import java.io.File

inline fun <reified T> readConfig(deserializer: DeserializationStrategy<T>, directory: String, filename: String): T {
    var fail = false
    val dir = File(directory)

    if (!dir.exists()) {
        dir.mkdirs()
        fail = true
    }

    val file = File(directory, filename)
    if (!file.exists()) {
        file.writeText("")
        fail = true
    }

    if (fail) error("You must create config file $directory/$filename")
    return Json.decodeFromString(deserializer, file.readText())
}