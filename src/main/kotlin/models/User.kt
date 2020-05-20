@file:Suppress("UnstableApiUsage")

package models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

enum class Role {Admin, Referee, TeamLeader, TeamMember}

object User : IntIdTable() {
    val email: Column<String> = varchar("email", 50)
    val name: Column<String> = varchar("name", 50)
    val surname: Column<String> = varchar("surname", 50)
    val password: Column<String> = varchar("password", 64)
    val role = customEnumeration("role", "ENUM('Admin', 'Referee', 'TeamLeader', 'TeamMember')", { value -> Role.valueOf(value as String)}, {it.name})
}