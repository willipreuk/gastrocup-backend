package helper

import io.ktor.application.ApplicationCall

data class Pagination(val offset: Long, val perPage: Int)

object PaginationHelper {
    fun paginate(call: ApplicationCall): Pagination {
        try {
            val offset = call.request.queryParameters["offset"]!!.toLong()
            val perPage = call.request.queryParameters["perPage"]!!.toInt()

            return Pagination(offset, perPage)
        } catch (e: Exception) {
            if (e is NullPointerException || e is NumberFormatException) {
                throw IllegalArgumentException()
            }
            throw e
        }
    }
}