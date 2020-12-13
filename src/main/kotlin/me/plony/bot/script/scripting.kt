package me.plony.bot.script

import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.core.on
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import me.plony.bot.scripting.DiscordHostConfiguration
import me.plony.bot.scripting.DiscordScript
import me.plony.bot.scripting.DiscordScriptEvaluationConfiguration
import me.plony.bot.scripting.DiscordScriptReceiver
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate


fun Kord.evalFile(scriptFile: File, parentJob: Job): ResultWithDiagnostics<EvaluationResult> {
    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<DiscordScript>()
    val receiver = DiscordScriptReceiver(this, parentJob)
    val evalConfiguration = DiscordScriptEvaluationConfiguration(receiver)
    return BasicJvmScriptingHost(DiscordHostConfiguration).eval(scriptFile.toScriptSource(), compilationConfiguration, evalConfiguration)
}