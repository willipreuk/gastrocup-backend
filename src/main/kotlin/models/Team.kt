package models

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

data class TeamModel(val id: Int, val name: String, val group: Int, val leader: UserModel, val members: List<UserModel>, val createdAt: LocalDateTime, val updatedAt: LocalDateTime)

// junction table
object TeamMembers : Table() {
    val team = reference("team", Teams, ReferenceOption.CASCADE)
    val user = reference("user", Users)
    override val primaryKey = PrimaryKey(team, user)
}

object Teams : BaseIntIdTable("Teams") {
    val name = varchar("name", 50)
    val leader = reference("leader", Users)
    var group = reference("group", Groups, ReferenceOption.RESTRICT)

    fun getById(idString: String?) : Team {
        val id = idString?.toInt() ?: throw IllegalArgumentException()

        return transaction { Team.findById(id) ?: throw NoSuchElementException() }
    }
}

class Team(id: EntityID<Int>): BaseIntEntity(id, Teams) {
    companion object : BaseIntEntityClass<Team>(Teams)

    var name by Teams.name
    var leader by User referencedOn Teams.leader
    var members by User via TeamMembers
    var group by Group referencedOn Teams.group

    fun toModel(): TeamModel {

        // no group serialization because of circular references
        return TeamModel(id.value, name, group.id.value, leader.toModel(), members.toList().map { it.toModel() }, createdAt, updatedAt)
    }
}

