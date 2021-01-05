package me.plony.bot.utils.api.inline

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.channel.TextChannelBehavior
import dev.kord.core.behavior.channel.VoiceChannelBehavior
import dev.kord.core.supplier.EntitySupplier

fun Snowflake.asGuild(kord: Kord) = object : GuildBehavior {
    override val id: Snowflake = this@asGuild
    override val kord: Kord = kord
    override val supplier: EntitySupplier = kord.defaultSupplier
}

fun Snowflake.asMember(kord: Kord, guildId: Snowflake) = object : MemberBehavior {
    override val guildId: Snowflake = guildId
    override val id: Snowflake = this@asMember
    override val kord: Kord = kord
    override val supplier: EntitySupplier = kord.defaultSupplier
}

fun Snowflake.asUser(kord: Kord) = object : UserBehavior {
    override val id: Snowflake = this@asUser
    override val kord: Kord = kord
    override val supplier: EntitySupplier = kord.defaultSupplier
}

fun Snowflake.asMessage(kord: Kord, channelId: Snowflake) = object : MessageBehavior {
    override val channelId: Snowflake = channelId
    override val id: Snowflake = this@asMessage
    override val kord: Kord = kord
    override val supplier: EntitySupplier = kord.defaultSupplier
}

fun Snowflake.asChannel(kord: Kord) = object : ChannelBehavior {
    override val id: Snowflake = this@asChannel
    override val kord: Kord = kord
    override val supplier: EntitySupplier = kord.defaultSupplier
}

fun Snowflake.asTextChannel(kord: Kord, guildId: Snowflake) = object : TextChannelBehavior {
    override val guildId: Snowflake = guildId
    override val id: Snowflake = this@asTextChannel
    override val kord: Kord = kord
    override val supplier: EntitySupplier = kord.defaultSupplier
}

fun Snowflake.asVoiceChannel(kord: Kord, guildId: Snowflake) = object : VoiceChannelBehavior {
    override val guildId: Snowflake = guildId
    override val id: Snowflake = this@asVoiceChannel
    override val kord: Kord = kord
    override val supplier: EntitySupplier = kord.defaultSupplier
}