package webapp

import jakarta.validation.Validation.byProvider
import jakarta.validation.Validator
import org.hibernate.validator.HibernateValidator
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ProblemDetail
import org.springframework.http.ProblemDetail.forStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity.internalServerError
import org.springframework.web.server.ServerWebExchange
import webapp.accounts.entities.AccountRecord.Companion.EMAIL_FIELD
import webapp.accounts.entities.AccountRecord.Companion.FIRST_NAME_FIELD
import webapp.accounts.entities.AccountRecord.Companion.LAST_NAME_FIELD
import webapp.accounts.entities.AccountRecord.Companion.LOGIN_FIELD
import webapp.accounts.entities.AccountRecord.Companion.PASSWORD_FIELD
import webapp.accounts.models.AccountCredentials
import webapp.accounts.models.AccountCredentials.Companion.objectName
import java.net.URI
import java.util.Locale.ENGLISH
import java.util.Locale.of

object ProblemsUtils {
    //TODO: i18n
    fun ProblemsModel.serverErrorResponse(error: String): ResponseEntity<ProblemDetail> {
        return internalServerError().body(
            forStatus(INTERNAL_SERVER_ERROR).apply {
                type = URI(this@serverErrorResponse.type)
                title = title
                status = INTERNAL_SERVER_ERROR.value()
                setProperty("path", path)
                setProperty("message", message)
                setProperty("error", error)
            }
        )
    }

    private val ServerWebExchange.validator: Validator
        get() = byProvider(HibernateValidator::class.java)
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

    fun AccountCredentials.validate(
        exchange: ServerWebExchange
    ): Set<Map<String, String?>> {
        exchange.validator.run {
            return setOf(
                PASSWORD_FIELD,
                EMAIL_FIELD,
                LOGIN_FIELD,
                FIRST_NAME_FIELD,
                LAST_NAME_FIELD
            ).map { field ->
                field to validateProperty(this@validate, field)
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

}