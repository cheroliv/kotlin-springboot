package webapp.signup

import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.internalServerError
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import webapp.Constants.ACCOUNT_API
import webapp.Constants.ACTIVATE_API
import webapp.Constants.ACTIVATE_API_KEY
import webapp.Constants.SIGNUP_API
import webapp.Logging.i
import webapp.accounts.models.AccountCredentials
import webapp.signup.SignupUtils.badResponse
import webapp.signup.SignupUtils.badResponseEmailIsNotAvailable
import webapp.signup.SignupUtils.badResponseLoginIsNotAvailable
import webapp.signup.SignupUtils.emailIsNotAvailable
import webapp.signup.SignupUtils.loginIsNotAvailable
import webapp.signup.SignupUtils.signupProblems
import webapp.signup.SignupUtils.validate
import java.util.*
import java.util.Locale.*

@RestController
@RequestMapping(ACCOUNT_API)
class SignupController(private val signupService: SignupService) {

    internal class SignupException(message: String) : RuntimeException(message)

    /**
     * {@code POST  /signup} : register the user.
     *
     * @param account the managed user View Model.
     */
    @PostMapping(
        SIGNUP_API,
        produces = [APPLICATION_PROBLEM_JSON_VALUE]
    )
    suspend fun signup(
        @RequestBody account: AccountCredentials,
        exchange: ServerWebExchange
    ): ResponseEntity<ProblemDetail> {
        i("signup attempt: ${account.login} ${account.email}")
        val errors = account.validate(exchange)
        return when {
            errors.isNotEmpty() -> signupProblems.badResponse(errors)
            account.loginIsNotAvailable(signupService) -> signupProblems.badResponseLoginIsNotAvailable
            account.emailIsNotAvailable(signupService) -> signupProblems.badResponseEmailIsNotAvailable
            else -> {
                try {
                    signupService.signup(account)
                } catch (_: Exception) {
                    return internalServerError().build()
                }
                ResponseEntity<ProblemDetail>(CREATED)
            }
        }
    }


    /**
     * `GET  /activate` : activate the signed-up user.
     *
     * @param key the activation key.
     * @return ResponseEntity<ProblemDetail> `500 (Internal Application Error)` if the user couldn't be activated.
     */
    //TODO: Problem avec model et message i18n
    @GetMapping(ACTIVATE_API)
    suspend fun activateAccount(
        @RequestParam(ACTIVATE_API_KEY) key: String
    ): ResponseEntity<ProblemDetail> {
        when (val account = signupService.accountByActivationKey(key)) {
            null -> {
                i("no activation for key: $key")//MSG_WRONG_ACTIVATION_KEY
                return internalServerError().build()
            }

            else -> {
                i("activation: ${account.login}")
                try {
                    signupService.saveAccount(
                        account.copy(
                            activated = true,
                            activationKey = null
                        )
                    )
                } catch (_: Exception) {
                    return internalServerError().build()
                }
                return ok().build()
            }
        }
    }
}