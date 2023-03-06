package webapp.signup

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation.byProvider
import org.hibernate.validator.HibernateValidator
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE
import org.springframework.http.ProblemDetail
import org.springframework.http.ProblemDetail.forStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ServerWebExchange
import webapp.Constants.ACCOUNT_API
import webapp.Constants.ACTIVATE_API
import webapp.Constants.ACTIVATE_API_KEY
import webapp.Constants.MSG_WRONG_ACTIVATION_KEY
import webapp.Constants.SIGNUP_API
import webapp.Logging.i
import webapp.ProblemsModel
import webapp.accounts.entities.AccountRecord.Companion.EMAIL_FIELD
import webapp.accounts.entities.AccountRecord.Companion.FIRST_NAME_FIELD
import webapp.accounts.entities.AccountRecord.Companion.LAST_NAME_FIELD
import webapp.accounts.entities.AccountRecord.Companion.LOGIN_FIELD
import webapp.accounts.entities.AccountRecord.Companion.PASSWORD_FIELD
import webapp.accounts.exceptions.EmailAlreadyUsedException
import webapp.accounts.exceptions.UsernameAlreadyUsedException
import webapp.accounts.models.AccountCredentials
import webapp.accounts.models.AccountCredentials.Companion.objectName
import java.net.URI
import java.util.*
import java.util.Locale.*

@RestController
@RequestMapping(ACCOUNT_API)
class SignupController(private val signupService: SignupService) {

    companion object {
        val problemsModel = ProblemsModel(
            type = "https://cheroliv.github.io/problem/constraint-violation",
            title = "Data binding and validation failure",
            path = "$ACCOUNT_API$SIGNUP_API",
            message = "error.validation",
            status = BAD_REQUEST.value(),
        )
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
    @ResponseStatus(CREATED)
    suspend fun signup(
        @RequestBody account: AccountCredentials,
        exchange: ServerWebExchange
    ): ResponseEntity<ProblemDetail> = account.run acc@{
        signupChecks(exchange, this@acc).run {
            when {
                isNotEmpty() -> return badResponse(problemsModel, this)
                else -> try {
                    isLoginAvailable(this@acc)
                    isEmailAvailable(this@acc)
                } catch (e: UsernameAlreadyUsedException) {
                    return badResponse(
                        problemsModel, setOf(
                            mapOf(
                                "objectName" to objectName,
                                "field" to LOGIN_FIELD,
                                "message" to e.message!!
                            )
                        )
                    )
                } catch (e: EmailAlreadyUsedException) {
                    return badResponse(
                        problemsModel, setOf(
                            mapOf(
                                "objectName" to objectName,
                                "field" to EMAIL_FIELD,
                                "message" to e.message!!
                            )
                        )
                    )
                }
            }
        }
        signupService.signup(this)
        ResponseEntity<ProblemDetail>(CREATED)
    }

    private fun badResponse(
        problemsModel: ProblemsModel,
        fieldErrors: Set<Map<String, String?>>
    ) = badRequest().body(
        forStatus(BAD_REQUEST).apply {
            type = URI(problemsModel.type)
            title = problemsModel.title
            status = BAD_REQUEST.value()
            setProperty("path", problemsModel.path)
            setProperty("message", problemsModel.message)
            setProperty("fieldErrors", fieldErrors)
        }
    )

    private fun signupChecks(
        exchange: ServerWebExchange,
        accountCredentials: AccountCredentials
    ): Set<Map<String, String?>> {
        byProvider(HibernateValidator::class.java)
            .configure()
            .localeResolver {
                try {
                    of(
                        exchange
                            .request
                            .headers
                            .acceptLanguage
                            .first()
                            .range
                    )
                } catch (e: Exception) {
                    ENGLISH
                }
            }
            .buildValidatorFactory()
            .validator.run {
                return setOf(
                    PASSWORD_FIELD,
                    EMAIL_FIELD,
                    LOGIN_FIELD,
                    FIRST_NAME_FIELD,
                    LAST_NAME_FIELD
                ).map { field ->
                    field to validateProperty(accountCredentials, field)
                }.flatMap { violatedField: Pair<String, MutableSet<ConstraintViolation<AccountCredentials>>> ->
                    violatedField.second.map {
                        mapOf<String, String?>(
                            "objectName" to objectName,
                            "field" to violatedField.first,
                            "message" to it.message
                        )
                    }
                }.toSet()
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


    @Throws(UsernameAlreadyUsedException::class)
    private suspend fun isLoginAvailable(model: AccountCredentials) {
        signupService.accountById(model.login!!).run {
            when {
                this != null -> if (!activated) signupService.deleteAccount(toAccount())
                else throw UsernameAlreadyUsedException()
            }
        }
    }

    @Throws(EmailAlreadyUsedException::class)
    private suspend fun isEmailAvailable(model: AccountCredentials) {
        signupService.accountById(model.email!!).run {
            when {
                this != null -> if (!activated) signupService.deleteAccount(toAccount())
                else throw EmailAlreadyUsedException()
            }
        }
    }
}