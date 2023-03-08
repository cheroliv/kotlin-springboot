package webapp.signup

import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import webapp.Constants.ACCOUNT_API
import webapp.Constants.ACTIVATE_API
import webapp.Constants.ACTIVATE_API_KEY
import webapp.Constants.MSG_WRONG_ACTIVATION_KEY
import webapp.Constants.SIGNUP_API
import webapp.Constants.serverErrorProblems
import webapp.Constants.validationProblems
import webapp.Logging.d
import webapp.accounts.models.AccountCredentials
import webapp.signup.SignupUtils.badResponse
import webapp.signup.SignupUtils.badResponseEmailIsNotAvailable
import webapp.signup.SignupUtils.badResponseLoginIsNotAvailable
import webapp.signup.SignupUtils.emailIsNotAvailable
import webapp.signup.SignupUtils.loginIsNotAvailable
import webapp.signup.SignupUtils.serverErrorResponse
import webapp.signup.SignupUtils.validate
import java.util.*
import java.util.Locale.*

@RestController
@RequestMapping(ACCOUNT_API)
class SignupController(private val signupService: SignupService) {

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
        d("signup attempt: ${account.login} ${account.email}")
        val errors = account.validate(exchange)
        val problems = validationProblems
            .copy(path = "$ACCOUNT_API$SIGNUP_API")
        return when {
            errors.isNotEmpty() -> problems.badResponse(errors)
            account.loginIsNotAvailable(signupService) -> problems.badResponseLoginIsNotAvailable
            account.emailIsNotAvailable(signupService) -> problems.badResponseEmailIsNotAvailable
            else -> {
                try {
                    signupService.signup(account)
                } catch (e: Exception) {
                    return serverErrorProblems
                        .copy(path = "$ACCOUNT_API$SIGNUP_API")
                        .serverErrorResponse(e.message!!)
                }
                ResponseEntity(CREATED)
            }
        }
    }


    /**
     * `GET  /activate` : activate the signed-up user.
     *
     * @param key the activation key.
     * @return ResponseEntity `500 (Internal Application Error)` if the user couldn't be activated.
     */
    @GetMapping(ACTIVATE_API)
    suspend fun activateAccount(
        @RequestParam(ACTIVATE_API_KEY) key: String
    ): ResponseEntity<ProblemDetail> {
        val problems = serverErrorProblems.copy(path = "$ACCOUNT_API$ACTIVATE_API")
        when (val account = signupService.accountByActivationKey(key)) {
            null -> {
                d("no activation for key: $key")
                return problems.serverErrorResponse(MSG_WRONG_ACTIVATION_KEY)
            }

            else -> {
                d("activation: ${account.login}")
                try {
                    signupService.saveAccount(
                        account.copy(
                            activated = true,
                            activationKey = null
                        )
                    )
                } catch (e: Exception) {
                    return problems.serverErrorResponse(e.message!!)
                }
                return ResponseEntity(OK)
            }
        }
    }
}