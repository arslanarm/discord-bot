package me.plony.bot.database

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class BannedName(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<BannedName>(BannedNames)
    var name by BannedNames.name
}