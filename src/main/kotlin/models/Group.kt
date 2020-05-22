package models

import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

data class GroupModel(val id: Int, val name: String, val matchField: String, val createdAt: LocalDateTime, val updatedAt: LocalDateTime)

object Groups : BaseIntIdTable("Groups") {
    val name = varchar("name", 50)
    val matchField = varchar("match_field", 50)
}

class Group(id: EntityID<Int>) : BaseIntEntity(id, Groups) {
    companion object : BaseIntEntityClass<Group>(Groups)

    var name by Groups.name
    var matchField by Groups.matchField

    fun toModel(): GroupModel {
        return GroupModel(id.value, name, matchField, createdAt, updatedAt)
    }
}