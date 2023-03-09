package webapp.password

import jakarta.validation.Validator
import jakarta.validation.constraints.Email
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import webapp.*
import webapp.entities.AccountRecord.Companion.PASSWORD_FIELD
import webapp.exceptions.InvalidPasswordException
import webapp.http.badResponse
import webapp.logging.w
import webapp.models.AccountCredentials
import webapp.models.KeyAndPassword
import webapp.models.PasswordChange
import webapp.mail.MailService

/*=================================================================================*/
@Suppress("unused")
@RestController
@RequestMapping(ACCOUNT_API)
class PasswordController(
    private val passwordService: PasswordService,
    private val mailService: MailService,
    private val validator: Validator
) {
    internal class PasswordException(message: String) : RuntimeException(message)
    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user.
     */
    @PostMapping(RESET_PASSWORD_API_INIT)//TODO: retourner des problemDetails
    suspend fun requestPasswordReset(@RequestBody @Email mail: String) =
        with(passwordService.requestPasswordReset(mail)) {
            when {
                this == null -> w("Password reset requested for non existing mail")
                else -> mailService.sendPasswordResetMail(this)
            }
        }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordProblem {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException         {@code 500 (Internal Application Error)} if the password could not be reset.
     */
    @PostMapping(RESET_PASSWORD_API_FINISH)//TODO: retourner des problemDetails
    suspend fun finishPasswordReset(@RequestBody keyAndPassword: KeyAndPassword): Unit =
        InvalidPasswordException().run {
            when {
                validator
                    .validateProperty(
                        AccountCredentials(password = keyAndPassword.newPassword),
                        PASSWORD_FIELD
                    ).isNotEmpty() -> throw this
                keyAndPassword.newPassword != null
                        && keyAndPassword.key != null
                        && passwordService.completePasswordReset(
                    keyAndPassword.newPassword,
                    keyAndPassword.key
                ) == null -> throw PasswordException("No user was found for this reset key")
            }
        }

    /**
     * authenticated
     *
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChange current and new password.
     * @throws InvalidPasswordProblem {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(CHANGE_PASSWORD_API)//TODO: retourner des problemDetails
    suspend fun changePassword(@RequestBody passwordChange: PasswordChange): ResponseEntity<ProblemDetail> =
        InvalidPasswordException().run {
            if (validator
                    .validateProperty(
                        AccountCredentials(password = passwordChange.newPassword),
                        PASSWORD_FIELD
                    ).isNotEmpty()
            ) return validationProblems.badResponse(setOf(mapOf("" to "")))
            if (passwordChange.currentPassword != null
                && passwordChange.newPassword != null
            ) {
                passwordService.changePassword(
                    passwordChange.currentPassword,
                    passwordChange.newPassword
                )
            }
            ResponseEntity(OK)
        }

}