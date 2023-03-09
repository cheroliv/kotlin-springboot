package webapp.signup

import webapp.ProblemsModel
import webapp.accounts.entities.AccountRecord.Companion.EMAIL_FIELD
import webapp.accounts.entities.AccountRecord.Companion.LOGIN_FIELD
import webapp.accounts.models.AccountCredentials
import webapp.accounts.models.AccountCredentials.Companion.objectName
import webapp.accounts.models.toAccount
import webapp.badResponse


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