package models

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

data class GroupModel(val id: Int, val name: String, val matchField: String, val teams: List<TeamModel>, val createdAt: LocalDateTime, val updatedAt: LocalDateTime)

object Groups : BaseIntIdTable("Groups") {
    val name = varchar("name", 50)
    val matchField = varchar("match_field", 50)

    fun getById(idString: String?): Group {
        val id = idString?.toInt() ?: throw IllegalArgumentException()

        return transaction {
            Group.findById(id) ?: throw NoSuchElementException()
        }
    }
}

class Group(id: EntityID<Int>) : BaseIntEntity(id, Groups) {
    companion object : BaseIntEntityClass<Group>(Groups)

    var name by Groups.name
    var matchField by Groups.matchField
    val teams by Team referrersOn Teams.group

    fun toModel(): GroupModel {
        return GroupModel(id.value, name, matchField, teams.toList().map { it.toModel() }, createdAt, updatedAt)
    }
}