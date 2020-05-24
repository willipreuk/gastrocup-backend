package helper

import com.sun.mail.smtp.SMTPTransport
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class MailerConfigurationException : Exception() {
    override val message: String?
        get() = "Mailer is not configured properly, please check if all envs are properly set."
}

class Mailer {
    var session: Session? = null
    var fromEmail: InternetAddress? = null

    init {
        val env = System.getenv()
        val properties = Properties()

        try {
            fromEmail = InternetAddress(env["SMTP_FROM_EMAIL"]!!)

            properties["mail.smtp.host"] = env["SMTP_HOST"]!!
            properties["mail.smtp.port"] = env["SMTP_PORT"]!!
            properties["mail.smtp.auth"] = env["SMTP_AUTH"]!!
            properties["mail.smtp.starttls.enable"] = env["SMTP_STARTTLS"]!!

            val auth: Authenticator = object : Authenticator() {

                val fromEmail = env["SMTP_FROM_EMAIL"]!!
                val password = env["SMTP_PASSWORD"]!!

                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(fromEmail, password)
                }
            }

            session = Session.getInstance(properties, auth)
        } catch (e: NullPointerException) {
            throw e
        }
    }

    fun sendEmail(recipient: String, subject: String, messageText: String) {
        val message = MimeMessage(session)
        message.setFrom(fromEmail)
        message.setHeader("X-Mailer", "Gastrocup - ktor-server")
        message.sentDate = Date()
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false))
        message.subject = subject
        message.setText(messageText)

        val transport: SMTPTransport = session?.getTransport("smtp") as SMTPTransport
        transport.connect()
        transport.sendMessage(message, message.allRecipients)
        transport.close()
    }
}