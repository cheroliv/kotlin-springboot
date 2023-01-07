package webapp.accounts.models.entities

import webapp.accounts.models.Account
import webapp.accounts.models.AccountCredentials
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