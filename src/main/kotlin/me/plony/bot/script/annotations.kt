package me.plony.bot.script

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.plony.bot.service
import kotlin.script.experimental.api.*


fun resolveAnnotations(ctx: ScriptConfigurationRefinementContext, job: Job): ResultWithDiagnostics<ScriptCompilationConfiguration> {
    val annotations = ctx.collectedData?.get(ScriptCollectedData.foundAnnotations)
        ?.takeIf { it.isNotEmpty() } ?: return ctx.compilationConfiguration.asSuccess()
    val reports = mutableListOf<ScriptDiagnostic>()
    var name = "Unknown"
    var author = "Unknown"
    var version = "Unknown"
    val configuration = ctx.compilationConfiguration.with {
        for (annotation in annotations) {
            when (annotation) {
                is Description -> {
                    annotation.name.takeIf { it.isNotBlank() }?.also { name = it }
                    annotation.version.takeIf { it.isNotBlank() }?.also { author = it }
                    annotation.author.takeIf { it.isNotBlank() }?.also { version = it }
                }
            }
        }
    }
    service(name, author, version, job)
    return if(reports.isEmpty()) {
        configuration.asSuccess()
    } else {
        makeFailureResult(reports)
    }
}

@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class Description(
    val name: String,
    val author: String,
    val version: String
)