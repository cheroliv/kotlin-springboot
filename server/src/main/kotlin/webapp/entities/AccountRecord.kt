package webapp.entities

import webapp.models.Account
import webapp.models.AccountCredentials
import java.time.Instant
import java.util.*

/*=================================================================================*/
interface AccountRecord<AUTH : AuthorityRecord> {

    var id: UUID?
    var login: String?
    var password: String?
    var firstName: String?
    var lastName: String?
    var email: String?
    var activated: Boolean
    var langKey: String?
    var imageUrl: String?
    var activationKey: String?
    var resetKey: String?
    var resetDate: Instant?
    var authorities: MutableSet<AUTH>?
    var createdBy: String?
    var createdDate: Instant?
    var lastModifiedBy: String?
    var lastModifiedDate: Instant?

    companion object {
        const val LOGIN_FIELD = "login"
        const val PASSWORD_FIELD = "password"
        const val EMAIL_FIELD = "email"
        const val ACCOUNT_AUTH_USER_ID_FIELD = "userId"
        const val ACTIVATION_KEY_FIELD = "activationKey"
        const val RESET_KEY_FIELD = "resetKey"
        const val FIRST_NAME_FIELD = "firstName"
        const val LAST_NAME_FIELD = "lastName"
    }

    val toModel: Account
        get() = Account(
            id = id,
            login = login,
            firstName = firstName,
            lastName = lastName,
            email = email,
            imageUrl = imageUrl,
            activated = activated,
            langKey = langKey,
            createdBy = createdBy,
            createdDate = createdDate,
            lastModifiedBy = lastModifiedBy,
            lastModifiedDate = lastModifiedDate,
            authorities = authorities?.map { it.role }?.toSet()
        )

    val toCredentialsModel: AccountCredentials
        get() = AccountCredentials(
            id = id,
            login = login,
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            activationKey = activationKey,
            imageUrl = imageUrl,
            activated = activated,
            langKey = langKey,
            createdBy = createdBy,
            createdDate = createdDate,
            lastModifiedBy = lastModifiedBy,
            lastModifiedDate = lastModifiedDate,
            authorities = authorities?.map { it.role }?.toSet()
        )

}