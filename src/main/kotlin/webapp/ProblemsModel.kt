package webapp

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST


data class ProblemsModel(
    val type: String,
    val title: String,
    val status: Int,
    val path: String = "",
    val message: String,
    val fieldErrors: MutableSet<Map<String, String>> = mutableSetOf()
) {

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        const val PROBLEM_OBJECT_NAME = "objectName"
        const val PROBLEM_FIELD = "field"
        const val PROBLEM_MESSAGE = "message"
        val detailsKeys = setOf(
            PROBLEM_OBJECT_NAME,
            PROBLEM_FIELD,
            PROBLEM_MESSAGE
        )
        val defaultProblems = ProblemsModel(
            type = "https://cheroliv.github.io/problem/constraint-violation",
            title = "Data binding and validation failure",
            message = "error.validation",
            path = "",
            status = BAD_REQUEST.value(),
        )
        val serverErrorProblems = ProblemsModel(
            type = "https://cheroliv.github.io/problem/internal-server-error",
            title = "Internal Server Error",
            message = "error.server",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
        )
    }
}