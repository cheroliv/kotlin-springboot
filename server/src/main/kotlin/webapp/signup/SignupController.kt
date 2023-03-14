package webapp.signup

import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import webapp.*
import webapp.http.badResponse
import webapp.http.serverErrorResponse
import webapp.http.validate
import webapp.logging.d
import webapp.logging.i
import webapp.models.AccountCredentials
import java.util.*
import java.util.Locale.*
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE as PROBLEM

@RestController
@RequestMapping(ACCOUNT_API)
class SignupController(private val signupService: SignupService) {

    /**
     * {@code POST  /signup} : register the user.
     *
     * @param account the managed user View Model.
     */
    @PostMapping(SIGNUP_API, produces = [PROBLEM])
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
            else -> try {
                signupService.signup(account).apply {
                    if (this != null)
                        i("activation link: $BASE_URL_DEV$ACTIVATE_API_PATH$activationKey")
                }
                ResponseEntity(CREATED)
            } catch (e: Exception) {
                serverErrorProblems
                    .copy(path = "$ACCOUNT_API$SIGNUP_API")
                    .serverErrorResponse(e.message!!)
            }
        }
    }


    /**
     * `GET  /activate` : activate the signed-up user.
     *
     * @param key the activation key.
     * @return ResponseEntity `500 (Internal Application Error)` if the user couldn't be activated.
     */
    @GetMapping(ACTIVATE_API, produces = [PROBLEM])
    suspend fun activateAccount(
        @RequestParam(ACTIVATE_API_KEY) key: String
    ): ResponseEntity<ProblemDetail> {
        val problems = serverErrorProblems.copy(path = "$ACCOUNT_API$ACTIVATE_API")
        when (val account = signupService.accountByActivationKey(key)) {
            null -> {
                d("no activation for key: $key")
                return problems.serverErrorResponse(MSG_WRONG_ACTIVATION_KEY)
            }

            else -> return try {
                d("activation: ${account.login}")
                signupService.saveAccount(
                    account.copy(
                        activated = true,
                        activationKey = null
                    )
                )
                ResponseEntity(OK)
            } catch (e: Exception) {
                problems.serverErrorResponse(e.message!!)
            }
        }
    }
}