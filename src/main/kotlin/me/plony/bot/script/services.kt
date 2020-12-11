package me.plony.bot.script

import dev.kord.core.Kord
import dev.kord.core.kordLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.onFailure
import kotlin.script.experimental.api.onSuccess

fun Kord.startServices() {
    kordLogger.info("Starting services")
    val files = File("services").listFiles { file -> file.name.endsWith("discord.kts")  }
        ?: error("Directory 'services' not found")
    files.forEach {
        val job = Job()
        launch(job) {
            kordLogger.info("Evaluating file ${it.name}")
            val response = evalFile(it, job)
            kordLogger.info("Finished evaluating file ${it.name} with ${response.reports.size} reports")
            response.onFailure {
                response.reports.forEach {
                    it.exception?.printStackTrace()
                    kordLogger.info("Code: ${it.code}. Message: ${it.message}")
                }
            }
            response.onSuccess {
                it.asSuccess()
            }
        }
    }
}