package webapp.signup

import jakarta.validation.Valid
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE
import org.springframework.http.ProblemDetail.forStatusAndDetail
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.*
import webapp.Constants.ACCOUNT_API
import webapp.Constants.ACTIVATE_API
import webapp.Constants.ACTIVATE_API_KEY
import webapp.Constants.INVALID_PASSWORD_TYPE
import webapp.Constants.MSG_WRONG_ACTIVATION_KEY
import webapp.Constants.SIGNUP_API
import webapp.Logging.i
import webapp.models.AccountCredentials
import webapp.models.exceptions.InvalidPasswordException

@Suppress("unused")
@RestController
@RequestMapping(ACCOUNT_API)
class SignupController(private val signupService: SignupService) {
    internal class SignupException(message: String) : RuntimeException(message)

    /**
     * {@code POST  /signup} : register the user.
     *
     * @param accountCredentials the managed user View Model.
     * @throws webapp.InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws webapp.EmailAlreadyUsedProblem {@code 400 (Bad Request)} if the email is already used.
     * @throws webapp.LoginAlreadyUsedProblem {@code 400 (Bad Request)} if the login is already used.
     */
    @PostMapping(
        SIGNUP_API,
        produces = [APPLICATION_PROBLEM_JSON_VALUE]
    )
    @ResponseStatus(CREATED)
    @Throws(InvalidPasswordProblem::class)
    suspend fun signup(
        @Valid
        @RequestBody
        accountCredentials: AccountCredentials
    ) {
        i("on entre dans le controller")
        signupService.signup(accountCredentials)
    }

    class InvalidPasswordProblem(
        exception: InvalidPasswordException
    ) : ErrorResponseException(
        BAD_REQUEST,
        forStatusAndDetail(
            BAD_REQUEST,
            exception.message!!
        ).apply {
            type = INVALID_PASSWORD_TYPE
            title = "Incorrect password"
        },
        exception
    )

    /**
     * `GET  /activate` : activate the signed-up user.
     *
     * @param key the activation key.
     * @throws RuntimeException `500 (Internal Application Error)` if the user couldn't be activated.
     */
    @GetMapping(ACTIVATE_API)
    suspend fun activateAccount(@RequestParam(ACTIVATE_API_KEY) key: String) {
        if (!signupService.activate(key)) throw SignupException(MSG_WRONG_ACTIVATION_KEY)
    }
}