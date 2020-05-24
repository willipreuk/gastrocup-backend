package models

import org.jetbrains.exposed.dao.id.EntityID

data class PendingUserModel(val name: String, val surname: String, val invitedBy: String, val email: String, val invitationLink: String, val invitationLinkValid: Boolean)

object PendingUsers: BaseIntIdTable(name = "PendingUsers") {
    val name = varchar("name", 50)
    val surname = varchar("surname", 50)
    val email = varchar("email", 50)
    val invitedBy = reference("invited_by", Users)
    val invitationLink = varchar("invitation_link", 100)
    val invitationLinkValid = bool("invitation_link_used")
}

class PendingUser(id: EntityID<Int>) : BaseIntEntity(id, PendingUsers) {
    companion object : BaseIntEntityClass<PendingUser>(PendingUsers)

    var name by PendingUsers.name
    var surname by PendingUsers.surname
    var email by PendingUsers.email
    var invitedBy by User referencedOn PendingUsers.invitedBy
    var invitationLink by PendingUsers.invitationLink
    var invitationLinkValid by PendingUsers.invitationLinkValid

    fun sendInvitationEmail() {

    }
}