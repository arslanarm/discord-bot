package me.plony.bot.database

import org.jetbrains.exposed.dao.id.LongIdTable

object BannedNames : LongIdTable() {
    val name = text("name")
}