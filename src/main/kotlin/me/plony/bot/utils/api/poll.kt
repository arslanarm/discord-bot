package me.plony.bot.utils.api

import com.gitlab.kordlib.kordx.emoji.Emojis
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.live.live
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import me.plony.bot.utils.globals.reactions
import me.plony.bot.utils.shortcuts.addReaction

import me.plony.bot.utils.shortcuts.reaction
import kotlin.time.minutes

suspend fun MessageCreateEvent.choose(title: String, variants: List<String>): Int? {

    val pollMessage = message.channel.createEmbed {
        this.title = title
        description = variants.withIndex()
            .joinToString("\n") { (index, it) ->
                "${index + 1}) $it"
            }
    }

    val reaction = withTimeoutOrNull(2.minutes) {
        launch {
            variants.withIndex()
                .map { (index, _) ->
                    val reaction = reactions.first { it.first == index + 1 }.second
                    pollMessage.addReaction(reaction)
                }
                .also { pollMessage.addReaction(Emojis.x) }
        }
        pollMessage.live()
            .events
            .filterIsInstance<ReactionAddEvent>()
            .filter { it.user == message.author }
            .filter { event -> event.emoji in (reactions.map { it.second.reaction } + Emojis.x.reaction) }
            .first()
    }
    pollMessage.delete()
    reaction ?: return null
    val response = reactions
        .map { it.second.reaction }
        .indexOf(reaction.emoji)
    if (response == -1) return null
    return response
}