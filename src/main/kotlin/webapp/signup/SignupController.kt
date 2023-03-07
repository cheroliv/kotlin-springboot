package webapp.signup

import org.springframework.http.HttpStatus.CREATED
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
import webapp.Logging.i
import webapp.ProblemsModel.Companion.defaultProblems
import webapp.accounts.models.AccountCredentials
import webapp.signup.SignupUtils.badResponse
import webapp.signup.SignupUtils.badResponseEmailIsNotAvailable
import webapp.signup.SignupUtils.badResponseLoginIsNotAvailable
import webapp.signup.SignupUtils.emailIsNotAvailable
import webapp.signup.SignupUtils.loginIsNotAvailable
import webapp.signup.SignupUtils.signupChecks
import java.util.*
import java.util.Locale.*

@RestController
@RequestMapping(ACCOUNT_API)
class SignupController(private val signupService: SignupService) {

    companion object {
        val signupProblems = defaultProblems.copy(path = "$ACCOUNT_API$SIGNUP_API")
    }

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
    ): ResponseEntity<ProblemDetail> = account.signupChecks(exchange).run {
        if (isNotEmpty())
            return signupProblems.badResponse(this)
    }.run {
        return when {
            account.loginIsNotAvailable(signupService) -> signupProblems.badResponseLoginIsNotAvailable
            account.emailIsNotAvailable(signupService) -> signupProblems.badResponseEmailIsNotAvailable
            else -> {
                signupService.signup(account)
                ResponseEntity<ProblemDetail>(CREATED)
            }
        }
    }


    /**
     * `GET  /activate` : activate the signed-up user.
     *
     * @param key the activation key.
     * @throws RuntimeException `500 (Internal Application Error)` if the user couldn't be activated.
     */
    @GetMapping(ACTIVATE_API)
    @Throws(SignupException::class)
    suspend fun activateAccount(@RequestParam(ACTIVATE_API_KEY) key: String) {
        if (!signupService.accountByActivationKey(key).run no@{
                return@no when {
                    this == null -> false.apply { i("no activation for key: $key") }
                    else -> signupService
                        .saveAccount(copy(activated = true, activationKey = null))
                        .run yes@{
                            return@yes when {
                                this != null -> true.apply { i("activation: $login") }
                                else -> false
                            }
                        }
                }
            })
        //TODO: remplacer un ResponseEntity<ProblemDetail>
            throw SignupException(MSG_WRONG_ACTIVATION_KEY)
    }


}