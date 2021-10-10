package me.plony.bot.config

open class Config(
    val messages: Messages = Messages(),
    val privateChannels: List<PrivateChannelConfig> = listOf()
) {
    companion object : Config()
}