package me.plony.bot.database

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object MutedUsers: LongIdTable() {
    val userId = long("userId")
    val until = datetime("until")
}