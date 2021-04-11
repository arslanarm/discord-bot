package me.plony.bot.utils

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import kotlin.time.*

class DurationConvertor(val displayName: String, val description: String) : SingleConverter<Duration>() {
    override val signatureTypeString: String = "1d1h1m1s"
    override val errorTypeString: String = "<DAYS>d<HOURS>h<MINUTES>m<SECONDS>s"
    private val regex = Regex("(?=\\S+)([\\d]d)*([\\d]h)*([\\d]m)*([\\d]s)*")
    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        if (!regex.matches(arg)) return false
        val result = regex.find(arg)!!
        fun MatchResult.extractValue(suffix: String): Int {
            val regex = Regex("([\\d]$suffix)")
            val value = groupValues.firstOrNull { it.endsWith(suffix) }
            return value?.let {
                regex.find(it)
                    ?.value
                    ?.removeSuffix(suffix)
                    ?.toInt()
            } ?: 0
        }
        val days = result.extractValue("d").days
        val hours = result.extractValue("h").hours
        val minutes = result.extractValue("m").minutes
        val seconds = result.extractValue("s").seconds
        parsed = days + hours + minutes + seconds
        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder = StringChoiceBuilder(displayName, description)


    companion object {
        fun Arguments.duration(displayName: String, description: String) = arg(displayName, description, DurationConvertor(displayName, description))
    }
}