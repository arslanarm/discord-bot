package me.plony.bot.services

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.kordx.emoji.Emojis
import com.gitlab.kordlib.kordx.emoji.addReaction
import com.gitlab.kordlib.kordx.emoji.toReaction
import io.ktor.client.request.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import me.plony.bot.utils.client
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.awt.Color
import javax.swing.text.html.HTML
import kotlin.time.minutes

@Module
fun DiscordReceiver.wikipedia() {
    val reactions = listOf(
        1 to Emojis.one,
        2 to Emojis.two,
        3 to Emojis.three,
        4 to Emojis.four,
        5 to Emojis.five
    )

    on<MessageCreateEvent> {
        val content = message.content.trim().toLowerCase()
        if (!content.startsWith("википедия")) return@on
        val query = content.removePrefix("википедия").trim()
        if (query.isBlank()) return@on message.respond("Вы должны указать что мне искать")

        val result = WikipediaAPI.search(query)
        if (result.names.isEmpty())
            return@on message.respond("Ничего не нашлось")

        val pollMessage = message.channel.createEmbed {
            title = "Выберите подходящий"
            description = result.names.withIndex()
                .joinToString("\n") { (index, it) ->
                "${index + 1}) [$it](${result.urls[index]})"
            }
        }


        val reaction = withTimeoutOrNull(2.minutes) {
            launch {
                result.names.withIndex()
                    .map { (index, _) ->
                        val reaction = reactions.first { it.first == index + 1 }.second
                        pollMessage.addReaction(reaction)
                    }
                    .also { pollMessage.addReaction(Emojis.x) }
            }
            kord.events
                .filterIsInstance<ReactionAddEvent>()
                .filter { it.message == pollMessage }
                .filter { it.user == message.author }
                .filter { event -> event.emoji in (reactions.map { it.second.toReaction() } + Emojis.x) }
                .first()
        }
        pollMessage.delete()
        reaction ?: return@on message.respond("Вы не ответили за 2 минуты")
        val response = reactions
            .map { it.second.toReaction() }
            .indexOf(reaction.emoji)
        if (response == -1) return@on

        message.channel.createEmbed {
            title = result.names[response]
            color = Color.CYAN
            description = WikipediaAPI.extractInfo(result.urls[response])
            url = result.urls[response]
        }
    }
}

object WikipediaAPI {
    @Suppress("UNCHECKED_CAST")
    suspend fun search(query: String): Search =
        client.get("https://ru.wikipedia.org/w/api.php?action=opensearch&search=$query&format=json")
    private const val threshold = 1500
    const val url = "https://ru.wikipedia.org"
    suspend fun extractInfo(url: String): String {
        var length = 0
        return Jsoup.parse(client.get<String>(url))
            .selectFirst("div.mw-body-content > div.mw-content-ltr > div.mw-parser-output")
            .children()
            .first { it.tagName() == "p" }
            .recursiveText()
            .split(". ")
            .takeWhile { (length < threshold).apply { length += it.length } }
            .joinToString(" ") { "$it." }
    }
}

private fun Element.recursiveText(): String = childNodes()
    .filterNot { it is Element && it.`is`("sup.reference") }
    .filterNot { it is Element && it.text().isBlank() }
    .joinToString("") {
        when (it) {
            is TextNode -> it.text()
            is Element -> it.recursiveText()
            else -> ""
        }
    }.run {
        when (tagName()) {
            "a" -> "[$this](${WikipediaAPI.url}${attr("href")})"
            else -> this
        }
    }

@Serializable(SearchSerializer::class)
data class Search(val query: String, val names: List<String>, val unknown: List<String>, val urls: List<String>)

object SearchSerializer : KSerializer<Search> {
    override fun deserialize(decoder: Decoder): Search {
        require(decoder is JsonDecoder)
        val array = decoder.decodeJsonElement().jsonArray
        return Search(
            query = array[0].jsonPrimitive.content,
            names = array[1].jsonArray.map { it.jsonPrimitive.content }.take(5),
            unknown = array[2].jsonArray.map { it.jsonPrimitive.content }.take(5),
            urls = array[3].jsonArray.map { it.jsonPrimitive.content }.take(5)
        )
    }

    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("Search")

    override fun serialize(encoder: Encoder, value: Search) {
        TODO("Not yet implemented")
    }
}