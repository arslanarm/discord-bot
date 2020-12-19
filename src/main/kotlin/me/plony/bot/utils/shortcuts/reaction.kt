package me.plony.bot.utils.shortcuts

import com.gitlab.kordlib.kordx.emoji.DiscordEmoji
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.entity.ReactionEmoji

val DiscordEmoji.reaction: ReactionEmoji.Unicode
    get() = ReactionEmoji.Unicode(unicode)

suspend fun MessageBehavior.addReaction(reaction: DiscordEmoji) =
    addReaction(ReactionEmoji.Unicode(reaction.unicode))

