package me.plony.bot

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import dev.kord.common.entity.Permission
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.jakejmattson.discordkt.api.dsl.bot
import me.jakejmattson.discordkt.api.locale.Language
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class RunBot : CliktCommand() {
    val token: String by argument()
    val prefix: String by option().default("%%")

    override fun run() {

        configureDatabase()
        bot(token) {
            prefix { prefix }
            configure {
                intents += listOf(
                    Intent.GuildVoiceStates
                )
                commandReaction = null
            }
            localeOf(Language.EN) {}
        }
    }
}

fun main(args: Array<String>) = RunBot().main(args)

private operator fun Intents.plus(listOf: List<Intent.GuildVoiceStates>): Intents =
    listOf.fold(this) { acc, new -> acc + new }

fun configureDatabase() {
    Database.connect("jdbc:h2:./data", "org.h2.Driver")
    transaction {
    }
}
