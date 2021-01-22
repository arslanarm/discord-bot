package me.plony.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import me.plony.bot.database.BannedNames
import me.plony.bot.database.MutedUsers
import me.plony.bot.extensions.ModerationExtension
import me.plony.bot.extensions.PrivateChannelsExtension
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun main(args: Array<String>) {
    configureDatabase()
    val bot = ExtensibleBot(
        args[1],
        args[0]
    )

    bot.run {
        addExtension { ModerationExtension(it) }
        addExtension { PrivateChannelsExtension(it) }
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
