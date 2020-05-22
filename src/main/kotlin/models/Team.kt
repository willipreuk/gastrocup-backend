package models

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

data class TeamModel(val id: Int, val name: String, val leader: UserModel, val members: List<UserModel>, val createdAt : LocalDateTime, val updatedAt: LocalDateTime)

// junction table
object TeamMembers : Table() {
    val team = reference("team", Teams, ReferenceOption.CASCADE)
    val user = reference("user", Users)
    override val primaryKey = PrimaryKey(team, user)
}

object Teams : BaseIntIdTable("Teams") {
    val name = varchar("name", 50)
    val leader = reference("leader", Users)

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

    fun toModel(): TeamModel {
         return TeamModel(id.value, name, leader.toModel(), members.toList().map { member -> member.toModel() }, createdAt, updatedAt)
    }
}

