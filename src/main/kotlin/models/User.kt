@file:Suppress("UnstableApiUsage")

package models

import com.google.common.hash.Hashing
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.charset.Charset

enum class Role { Admin, Referee, TeamLeader, TeamMember }

data class UserModel(val id: Int, val email: String, val name: String, val surname: String, val password: String, val role: Role)

object Users : IntIdTable() {
    val email = varchar("email", 50).uniqueIndex()
    val name = varchar("name", 50)
    val surname = varchar("surname", 50)
    val password = varchar("password", 64)
    val role = customEnumeration("role", "ENUM('Admin', 'Referee', 'TeamLeader', 'TeamMember')", { value -> Role.valueOf(value as String) }, { it.name })

    fun getById(idString: String?): User {
        val id = idString!!.toInt()

        return transaction { User.findById(id) } ?: throw IllegalArgumentException()
    }

    fun hashPassword(password: String): String {
        return Hashing.sha256().hashString(password, Charset.defaultCharset()).toString()
    }
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var email by Users.email
    var name by Users.name
    var surname by Users.surname
    var password by Users.password
    var role by Users.role

    fun toModel(): UserModel {
        return UserModel(id.value, email, name, surname, password, role)
    }
}