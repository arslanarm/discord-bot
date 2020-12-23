package me.plony.bot.services


import dev.kord.common.kColor
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import io.ktor.client.request.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import me.plony.bot.utils.api.choose
import me.plony.bot.utils.globals.client
import me.plony.bot.utils.globals.prefix
import me.plony.bot.utils.shortcuts.respond
import me.plony.processor.DiscordReceiver
import me.plony.processor.Module
import me.plony.processor.on
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode


@Module("Модуль дающий возможность получить краткую информацию из википедии. Команда: %prefix%википедия %query%")
fun DiscordReceiver.wikipedia() {
    val localPrefix = "${prefix}википедия "
    on<MessageCreateEvent> {
        val content = message.content.trim().toLowerCase()
        if (!content.startsWith(localPrefix)) return@on
        val query = content.removePrefix(localPrefix).trim()
        if (query.isBlank()) return@on message.respond("Вы должны указать что мне искать")

        val result = WikipediaAPI.search(query)
        if (result.names.isEmpty())
            return@on message.respond("Ничего не нашлось")

        val response =
            if (result.names.size > 1)
                choose(
                    title = "Выберите подходящий",
                    variants = result.names
                        .withIndex()
                        .map { (index, it) ->
                            "[$it](${result.urls[index]})"
                        }
                ) ?: return@on
            else 0

        val (imageUrl, info) = WikipediaAPI.extractInfo(result.urls[response])
        message.channel.createEmbed {
            title = result.names[response]
            color = java.awt.Color.CYAN.kColor
            description = info
            url = result.urls[response]
            image = imageUrl
        }
    }
}


object WikipediaAPI {
    @Suppress("UNCHECKED_CAST")
    suspend fun search(query: String): Search =
        client.get("https://ru.wikipedia.org/w/api.php?action=opensearch&search=$query&format=json")
    private const val threshold = 1500
    const val url = "https://ru.wikipedia.org"
    suspend fun extractInfo(url: String): WikipediaInfo {
        var length = 0
        val dom = Jsoup.parse(client.get<String>(url))
        val body = dom
            .selectFirst("div.mw-body-content > div.mw-content-ltr > div.mw-parser-output")
        val info = body
            .children()
            .first { it.tagName() == "p" }
            .recursiveText()
            .split(". ")
            .takeWhile { (length < threshold).apply { length += it.length } }
            .joinToString(" ") { "$it." }
        val image = body
            .selectFirst("table.infobox")
            ?.selectFirst("td.infobox-image")
            ?.selectFirst("img")
            ?.attr("src")
            ?.run {
                when {
                    startsWith("//") -> "http:$this"
                    startsWith("http") -> this
                    else -> "$url$this"
                }
            }
        return WikipediaInfo(image, info)
    }
}

data class WikipediaInfo(val imageUrl: String?, val description: String)

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