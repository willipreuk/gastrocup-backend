package models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

data class TeamModel(val id: Int, val name: String, val leader: User, val members: List<User>)

// junction table
object TeamMembers : Table() {
    val team = reference("team", Teams)
    val user = reference("user", Users)
    override val primaryKey = PrimaryKey(team, user)
}

object Teams : IntIdTable() {
    val name = varchar("name", 50)
    val leader = reference("leader", Users)
}

class Team(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Team>(Teams)

    var name by Teams.name
    var leader by User referencedOn Teams.leader
    var members by User via TeamMembers

    fun toModel(): TeamModel {
        return TeamModel(id.value, name, leader, members.toList())
    }
}