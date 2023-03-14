package webapp.signup

import webapp.entities.AccountRecord.Companion.EMAIL_FIELD
import webapp.entities.AccountRecord.Companion.LOGIN_FIELD
import webapp.http.badResponse
import webapp.models.AccountCredentials
import webapp.models.AccountCredentials.Companion.objectName
import webapp.models.ProblemsModel
import webapp.models.toAccount


val ProblemsModel.badResponseLoginIsNotAvailable
    get() = badResponse(
        setOf(
            mapOf(
                "objectName" to objectName,
                "field" to LOGIN_FIELD,
                "message" to "Login name already used!"
            )
        )
    )
val ProblemsModel.badResponseEmailIsNotAvailable
    get() = badResponse(
        setOf(
            mapOf(
                "objectName" to objectName,
                "field" to EMAIL_FIELD,
                "message" to "Email is already in use!"
            )
        )
    )

suspend fun AccountCredentials.loginIsNotAvailable(
    signupService: SignupService
) = signupService.accountById(login!!).run {
    if (this == null) return@run false
    idsIsNotAvailable(signupService)
}

suspend fun AccountCredentials.emailIsNotAvailable(
    signupService: SignupService
) = signupService.accountById(email!!).run {
    if (this == null) return@run false
    idsIsNotAvailable(signupService)
}

private suspend fun AccountCredentials.idsIsNotAvailable(
    signupService: SignupService
) = when {
    !activated -> {//TODO: try catch return
        // internal server error with error exception message
        signupService.deleteAccount(toAccount())
        false
    }

    else -> true
}