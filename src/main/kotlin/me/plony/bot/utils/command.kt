package me.plony.bot.utils

import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.Kord

val Command.kord: Kord
    get() = extension.bot.kord
val Extension.kord: Kord
    get() = bot.kord