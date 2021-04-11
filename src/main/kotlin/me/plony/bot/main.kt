package me.plony.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import me.plony.bot.database.BannedNames
import me.plony.bot.database.MutedUsers
import me.plony.bot.extensions.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

@PrivilegedIntent
suspend fun main(args: Array<String>) {
    configureDatabase()
    val bot = ExtensibleBot.invoke(args[1]) {
        commands {
            prefix { args[0] }
        }
        intents {
            +Intents.all
        }
    }

    bot.run {
        addExtension { ModerationExtension(it) }
        addExtension { PrivateChannelsExtension(it) }
        addExtension { CubicExtension(it) }
        addExtension { VoiceChannelRoleExtension(it) }
        addExtension { MiscExtension(it) }
    }

    bot.start()
}

fun configureDatabase() {
    Database.connect("jdbc:h2:./data", "org.h2.Driver")
    transaction {
        SchemaUtils.create(
            BannedNames,
            MutedUsers
        )
    }
}
