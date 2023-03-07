package webapp.signup

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.hibernate.validator.HibernateValidator
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ProblemDetail.forStatus
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.web.server.ServerWebExchange
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
import java.util.Locale.ENGLISH
import java.util.Locale.of


object SignupUtils {
    private val ServerWebExchange.validator: Validator
        get() = Validation.byProvider(HibernateValidator::class.java)
            .configure()
            .localeResolver {
                try {
                    of(
                        request
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
            .validator

    fun ServerWebExchange.signupChecks(
        accountCredentials: AccountCredentials
    ): Set<Map<String, String?>> {
        validator.run {
            return setOf(
                PASSWORD_FIELD,
                EMAIL_FIELD,
                LOGIN_FIELD,
                FIRST_NAME_FIELD,
                LAST_NAME_FIELD
            ).map { field ->
                field to validateProperty(accountCredentials, field)
            }.flatMap { violatedField ->
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

    fun ProblemsModel.badResponse(
        fieldErrors: Set<Map<String, String?>>
    ) = badRequest().body(
        forStatus(BAD_REQUEST).apply {
            type = URI(this@badResponse.type)
            title = title
            status = BAD_REQUEST.value()
            setProperty("path", path)
            setProperty("message", message)
            setProperty("fieldErrors", fieldErrors)
        }
    )

    val AccountCredentials.emailIsNotAvailable: Boolean
        get() = false

    val AccountCredentials.loginIsNotAvailable: Boolean
        get() = false

    @Throws(UsernameAlreadyUsedException::class)
    suspend fun AccountCredentials.loginAvailable(signupService: SignupService) {
        signupService.accountById(login!!).run {
            when {
                this != null -> if (!activated) signupService.deleteAccount(toAccount())
                else throw UsernameAlreadyUsedException()
            }
        }
    }

    @Throws(EmailAlreadyUsedException::class)
    suspend fun AccountCredentials.emailAvailable(signupService: SignupService) {
        signupService.accountById(email!!).run {
            when {
                this != null -> if (!activated) signupService.deleteAccount(toAccount())
                else throw EmailAlreadyUsedException()
            }
        }
    }
}