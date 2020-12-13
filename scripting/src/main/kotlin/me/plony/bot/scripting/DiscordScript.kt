package me.plony.bot.scripting

import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.core.on
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

@KotlinScript(
    fileExtension = "discord",
    compilationConfiguration = DiscordScriptCompilationConfiguration::class,
    evaluationConfiguration = DiscordScriptEvaluationConfiguration::class,
    hostConfiguration = DiscordHostConfiguration::class
)
abstract class DiscordScript

class DiscordScriptReceiver(
    val kord: Kord,
    val parentJob: Job,
    override val coroutineContext: CoroutineContext = kord.coroutineContext + parentJob
) : CoroutineScope

object DiscordScriptCompilationConfiguration: ScriptCompilationConfiguration(
    {
        defaultImports(DiscordScriptReceiver::class)
        implicitReceivers(DiscordScriptReceiver::class)
        jvm {
            dependenciesFromClassContext(
                DiscordScriptCompilationConfiguration::class,
                "scripting",
                "kord-core",
                "kord-common",
                "kord-gateway",
                "kord-rest",
                "kordx.emoji",
                "kotlin-stdlib",
                "kotlin-reflect"
            )
        }
        ide {
            acceptedLocations(ScriptAcceptedLocation.Project) // these scripts are recognized everywhere in the project structure
        }
    }
)

class DiscordScriptEvaluationConfiguration(
    private val receiver: DiscordScriptReceiver
): ScriptEvaluationConfiguration(
    {
        implicitReceivers(receiver)
    }
)

object DiscordHostConfiguration : ScriptingHostConfiguration()

inline fun <reified T: Event> DiscordScriptReceiver.on(noinline consumer: suspend T.() -> Unit) = kord.on(this, consumer)
