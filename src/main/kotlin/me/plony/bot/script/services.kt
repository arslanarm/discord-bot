package me.plony.bot.script

import dev.kord.core.Kord
import dev.kord.core.kordLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.plony.bot.service
import java.io.File
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.onFailure
import kotlin.script.experimental.api.onSuccess

fun Kord.startServices(filePath: String) {
    kordLogger.info("Starting services")
    val files = File(filePath).listFiles { file -> file.name.endsWith("discord.kts")  }
        ?: error("Directory 'services' not found")
    files.forEach { file ->
        val job = Job()
        launch(job) {
            kordLogger.info("Evaluating file ${file.name}")
            val response = evalFile(file, job)
            kordLogger.info("Finished evaluating file ${file.name} with ${response.reports.size} reports")
            response.onFailure {
                job.cancel()
                response.reports.forEach {
                    it.exception?.printStackTrace()
                    kordLogger.info("Code: ${it.code}. Message: ${it.message}")
                }
            }
            response.onSuccess {
                service(file.name, job)
                it.asSuccess()
            }
        }
    }
}