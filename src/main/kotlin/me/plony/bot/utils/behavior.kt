package me.plony.bot.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.supplier.EntitySupplier

val MessageCreateEvent.guild: GuildBehavior?
        get() {
            val guildId = guildId ?: return null
            val kord = kord
            val supplier = supplier
            return object : GuildBehavior {
                override val id: Snowflake = guildId
                override val kord: Kord = kord
                override val supplier: EntitySupplier = supplier
            }
        }

fun user(id: Snowflake, kord: Kord) = object : UserBehavior {
    override val id: Snowflake = id
    override val kord: Kord = kord
    override val supplier: EntitySupplier = kord.defaultSupplier

}