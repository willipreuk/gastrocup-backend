import models.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class DB() {
    init {
        Database.connect("jdbc:mysql://10.0.20.104:3306/gastrocup", driver = "com.mysql.jdbc.Driver", user = "gastrocupDev", password = "gastrocupDev")
    }

    fun createSchemas() {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users)
        }
    }
}