package me.plony.bot.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.VoiceState
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.supplier.EntitySupplier

val MessageCreateEvent.guild: GuildBehavior?
        get() = guildId?.asGuild(kord)

fun user(id: Snowflake, kord: Kord) = object : UserBehavior {
    override val id: Snowflake = id
    override val kord: Kord = kord
    override val supplier: EntitySupplier = kord.defaultSupplier
}

fun Snowflake.asGuild(kord: Kord) = object : GuildBehavior {
    override val id: Snowflake = this@asGuild
    override val kord: Kord = kord
    override val supplier: EntitySupplier = kord.defaultSupplier
}

val VoiceState.member: MemberBehavior
    get() {
        val guildId = guildId
        val kord = kord
        return object : MemberBehavior {
            override val guildId: Snowflake = guildId
            override val id: Snowflake = userId
            override val kord: Kord = kord
            override val supplier: EntitySupplier = kord.defaultSupplier

        }
    }