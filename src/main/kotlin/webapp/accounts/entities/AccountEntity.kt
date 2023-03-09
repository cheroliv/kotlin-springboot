package webapp.accounts.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.data.annotation.*
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import webapp.*
import webapp.accounts.models.AccountCredentials
import java.time.Instant
import java.util.*

/*=================================================================================*/
@Table("`user`")
data class AccountEntity @JvmOverloads constructor(
    @Id override var id: UUID? = null,

    @field:NotNull
    @field:Pattern(regexp = LOGIN_REGEX)
    @field:Size(min = 1, max = 50)
    override var login: String? = null,

    @JsonIgnore @Column("password_hash")
    @field:NotNull
    @field:Size(min = 60, max = 60)
    override var password: String? = null,

    @field:Size(max = 50)
    override var firstName: String? = null,

    @field:Size(max = 50)
    override var lastName: String? = null,

    @field:Email
    @field:Size(min = 5, max = 254)
    override var email: String? = null,

    @field:NotNull
    override var activated: Boolean = false,

    @field:Size(min = 2, max = 10)
    override var langKey: String? = null,

    @field:Size(max = 256)
    override var imageUrl: String? = null,

    @JsonIgnore
    @field:Size(max = 20)
    override var activationKey: String? = null,

    @JsonIgnore
    @field:Size(max = 20)
    override var resetKey: String? = null,

    override var resetDate: Instant? = null,

    @JsonIgnore @Transient
    override var authorities: MutableSet<AuthorityEntity>? = mutableSetOf(),

    @JsonIgnore
    override var createdBy: String? = null,

    @JsonIgnore @CreatedDate
    override var createdDate: Instant? = Instant.now(),

    @JsonIgnore
    override var lastModifiedBy: String? = null,

    @JsonIgnore @LastModifiedDate
    override var lastModifiedDate: Instant? = Instant.now(),

    @Version @JsonIgnore var version: Long? = null
) : AccountRecord<AuthorityEntity> {
    @PersistenceCreator
    constructor(
        id: UUID?,
        login: String?,
        password: String?,
        firstName: String?,
        lastName: String?,
        email: String?,
        activated: Boolean,
        langKey: String?,
        imageUrl: String?,
        activationKey: String?,
        resetKey: String?,
        resetDate: Instant?,
        createdBy: String?,
        createdDate: Instant?,
        lastModifiedBy: String?,
        lastModifiedDate: Instant?
    ) : this(
        id,
        login,
        password,
        firstName,
        lastName,
        email,
        activated,
        langKey,
        imageUrl,
        activationKey,
        resetKey,
        resetDate,
        mutableSetOf(),
        createdBy,
        createdDate,
        lastModifiedBy,
        lastModifiedDate
    )
    constructor(model: AccountCredentials) : this() {
        id = model.id
        login = model.login
        email = model.email
        firstName = model.firstName
        lastName = model.lastName
        langKey = model.langKey
        activated = model.activated
        createdBy = model.createdBy
        createdDate = model.createdDate
        lastModifiedBy = model.lastModifiedBy
        lastModifiedDate = model.lastModifiedDate
        imageUrl = model.imageUrl
        authorities = model.authorities?.map { AuthorityEntity(it) }?.toMutableSet()
        password = model.password
        activationKey = model.activationKey
    }
}