package models

import helper.Mailer
import helper.Template
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.random.Random

object PendingUsers: BaseIntIdTable(name = "PendingUsers") {
    val name = varchar("name", 50)
    val surname = varchar("surname", 50)
    val email = varchar("email", 50).uniqueIndex()
    val invitedBy = reference("invited_by", Users)
    val invitationKey = varchar("invitation_key", 100)
    val invitationKeyValid = bool("invitation_key_valid")
    val team = reference("team", Teams)

    fun generateInvitationKey(): String {
        val key = Random.nextBytes(64)

        return Base64.getUrlEncoder().withoutPadding().encodeToString(key)
    }
}

class PendingUser(id: EntityID<Int>) : BaseIntEntity(id, PendingUsers) {
    companion object : BaseIntEntityClass<PendingUser>(PendingUsers)

    var name by PendingUsers.name
    var surname by PendingUsers.surname
    var email by PendingUsers.email
    var invitedBy by User referencedOn PendingUsers.invitedBy
    var invitationKey by PendingUsers.invitationKey
    var invitationKeyValid by PendingUsers.invitationKeyValid
    var team by Team referencedOn PendingUsers.team

    fun sendInvitationEmail(mailer: Mailer) {
        val st = Template("invite").getTemplate()
        transaction {
            st.add("name", name)
            st.add("surname", surname)
            st.add("team", team.name)
            st.add("link", "http://localhost:8080/invite/$invitationKey")
        }

        //mailer.sendEmail(email, "Einladung Gastrocup", st.render())
    }
}