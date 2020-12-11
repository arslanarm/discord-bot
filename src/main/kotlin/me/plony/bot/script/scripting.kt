package me.plony.bot.script

import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.core.on
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class DiscordScriptCompilationConfiguration(private val job: Job): ScriptCompilationConfiguration(
    {
        jvm {
            implicitReceivers(DiscordScriptReceiver::class)

            dependenciesFromClassContext(DiscordScriptCompilationConfiguration::class, wholeClasspath = true)
        }

        refineConfiguration {
            onAnnotations(
                listOf(Description::class),
            ) { resolveAnnotations(it, job) }
        }
        ide {
            acceptedLocations(ScriptAcceptedLocation.Everywhere) // these scripts are recognized everywhere in the project structure
        }
    }
)

class DiscordScriptEvaluationConfiguration(
    private val receiver: DiscordScriptReceiver
): ScriptEvaluationConfiguration(
    {
        scriptsInstancesSharing(true)
        implicitReceivers(receiver)
    }
)

fun Kord.evalFile(scriptFile: File, parentJob: Job): ResultWithDiagnostics<EvaluationResult> {
    val receiver = DiscordScriptReceiver(this, parentJob)
    val compilationConfiguration = DiscordScriptCompilationConfiguration(receiver.parentJob)
    val evalConfiguration = DiscordScriptEvaluationConfiguration(receiver)
    return BasicJvmScriptingHost().eval(scriptFile.toScriptSource(), compilationConfiguration, evalConfiguration)
}

class DiscordScriptReceiver(val kord: Kord, val parentJob: Job, override val coroutineContext: CoroutineContext = kord.coroutineContext + parentJob) : CoroutineScope

inline fun <reified T: Event> DiscordScriptReceiver.on(noinline consumer: suspend T.() -> Unit) = kord.on(this, consumer)

