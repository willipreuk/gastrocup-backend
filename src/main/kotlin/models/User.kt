@file:Suppress("UnstableApiUsage")

package models

import com.google.common.hash.Hashing
import java.nio.charset.StandardCharsets
import java.util.*

class User(val username: String, password: String) {
    val password : String = Hashing.sha256()
            .hashString(password, StandardCharsets.UTF_8)
            .toString()
    val dateCreated = Date().toString()
}