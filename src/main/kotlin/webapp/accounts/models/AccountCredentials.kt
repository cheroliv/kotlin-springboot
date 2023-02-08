@file:Suppress("unused")

package webapp.accounts.models

import jakarta.validation.constraints.*
import webapp.Constants.IMAGE_URL_DEFAULT
import webapp.Constants.LOGIN_REGEX
import webapp.Constants.PASSWORD_MAX_LENGTH
import webapp.Constants.PASSWORD_MIN_LENGTH
import webapp.Utils.objectName
import java.time.Instant
import java.util.*

/**
 * Représente l'account domain model avec le password et l'activationKey
 * pour la vue
 */
data class AccountCredentials(
    @field:NotNull(
//        groups = [SignupGroup::class]
    )
    @field:Size(
        min = PASSWORD_MIN_LENGTH,
        max = PASSWORD_MAX_LENGTH,
//        groups = [SignupGroup::class]
    )
    val password: String? = null,
    val activationKey: String? = null,
    val resetKey: String? = null,
    val id: UUID? = null,
    @field:NotBlank(
//        groups = [SignupGroup::class]
    )
    @field:Pattern(
        regexp = LOGIN_REGEX,
//        groups = [SignupGroup::class]
    )
    @field:Size(
        min = 1, max = 50,
//        groups = [SignupGroup::class]
    )
    val login: String? = null,
    @field:Size(
        max = 50,
//        groups = [SignupGroup::class]
    )
    val firstName: String? = null,
    @field:Size(
        max = 50,
//        groups = [SignupGroup::class]
    )
    val lastName: String? = null,
    @field:Email(
//        groups = [SignupGroup::class]
    )
    @field:Size(
        min = 5, max = 254,
//        groups = [SignupGroup::class]
    )
    val email: String? = null,
    @field:Size(max = 256)
    val imageUrl: String? = IMAGE_URL_DEFAULT,
    val activated: Boolean = false,
    @field:Size(min = 2, max = 10)
    val langKey: String? = null,
    val createdBy: String? = null,
    val createdDate: Instant? = null,
    val lastModifiedBy: String? = null,
    val lastModifiedDate: Instant? = null,
    val authorities: Set<String>? = null
) {

    constructor(account: Account) : this(
        id = account.id,
        login = account.login,
        email = account.email,
        firstName = account.firstName,
        lastName = account.lastName,
        langKey = account.langKey,
        activated = account.activated,
        createdBy = account.createdBy,
        createdDate = account.createdDate,
        lastModifiedBy = account.lastModifiedBy,
        lastModifiedDate = account.lastModifiedDate,
        imageUrl = account.imageUrl,
        authorities = account.authorities?.map { it }?.toMutableSet()
    )


    fun toAccount(): Account = Account(
        id = id,
        login = login,
        firstName = firstName,
        lastName = lastName,
        email = email,
        activated = activated,
        langKey = langKey,
        createdBy = createdBy,
        createdDate = createdDate,
        lastModifiedBy = lastModifiedBy,
        lastModifiedDate = lastModifiedDate,
        authorities = authorities
    )

    companion object {
        @JvmStatic
        val objectName = AccountCredentials::class.java.simpleName.objectName
    }
}