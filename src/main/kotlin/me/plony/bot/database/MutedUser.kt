package me.plony.bot.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class MutedUser(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<MutedUser>(MutedUsers)
    var userId by MutedUsers.userId
    var until by MutedUsers.until
}