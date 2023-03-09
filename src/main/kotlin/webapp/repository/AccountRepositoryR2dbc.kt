package webapp.repository

import jakarta.validation.Validator
import kotlinx.coroutines.reactive.collect
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.dao.DataAccessException
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository
import webapp.ROLE_USER
import webapp.entities.AccountAuthorityEntity
import webapp.entities.AccountEntity
import webapp.entities.AccountRecord
import webapp.models.Account
import webapp.models.AccountCredentials

@Repository
class AccountRepositoryR2dbc(
    private val dao: R2dbcEntityTemplate,
    private val validator: Validator,
) : AccountRepository {
    override suspend fun save(model: AccountCredentials) =
        try {
            if (model.id != null) dao.update(
                AccountEntity(model).copy(
                    version = dao.selectOne(
                        Query.query(
                            Criteria.where(AccountRecord.LOGIN_FIELD).`is`(model.login!!).ignoreCase(true)
                                .or(Criteria.where(AccountRecord.EMAIL_FIELD).`is`(model.email!!).ignoreCase(true))
                        ), AccountEntity::class.java
                    ).awaitSingle()!!.version
                )
            ).awaitSingle()?.toModel
            else dao.insert(AccountEntity(model)).awaitSingle()?.toModel
        } catch (_: DataAccessException) {
            null
        } catch (_: NoSuchElementException) {
            null
        }

    override suspend fun findOne(emailOrLogin: String) = dao
        .select<AccountEntity>()
        .matching(
            Query.query(
                Criteria.where(
                    if (validator.validateProperty(
                            AccountCredentials(email = emailOrLogin),
                            AccountRecord.EMAIL_FIELD
                        ).isEmpty()
                    ) AccountRecord.EMAIL_FIELD else AccountRecord.LOGIN_FIELD
                ).`is`(emailOrLogin).ignoreCase(true)
            )
        ).awaitOneOrNull()
        ?.toCredentialsModel

    private suspend fun withAuthorities(account: AccountCredentials?) = when {
        account == null -> null
        account.id == null -> null
        else -> account.copy(authorities = mutableSetOf<String>().apply {
            dao.select<AccountAuthorityEntity>()
                .matching(Query.query(Criteria.where(AccountRecord.ACCOUNT_AUTH_USER_ID_FIELD).`is`(account.id)))
                .all()
                .collect { add(it.role) }
        })
    }

    override suspend fun findOneWithAuthorities(emailOrLogin: String) = withAuthorities(findOne(emailOrLogin))


    override suspend fun findActivationKeyByLogin(login: String) =
        dao.select<AccountEntity>()
            .matching(Query.query(Criteria.where(AccountRecord.LOGIN_FIELD).`is`(login).ignoreCase(true)))
            .awaitOneOrNull()
            ?.activationKey

    override suspend fun signup(accountCredentials: AccountCredentials): AccountCredentials? = dao
        .insert(AccountEntity(accountCredentials))
        .awaitSingleOrNull().run {
            return when {
                this != null && id != null ->  toCredentialsModel.apply {
                    dao.insert(AccountAuthorityEntity(userId = id!!, role = ROLE_USER))
                        .awaitSingleOrNull()
                }
                else -> null
            }
        }


    override suspend fun findOneByActivationKey(key: String) = dao
        .selectOne(
            Query.query(Criteria.where(AccountRecord.ACTIVATION_KEY_FIELD).`is`(key)),
            AccountEntity::class.java
        ).awaitSingleOrNull()
        ?.toCredentialsModel

    override suspend fun findOneByResetKey(key: String) = dao
        .selectOne(
            Query.query(Criteria.where(AccountRecord.RESET_KEY_FIELD).`is`(key)),
            AccountEntity::class.java
        ).awaitSingleOrNull() //as  AccountRecord<*>?

    override suspend fun delete(account: Account) {
        when {
            account.login != null || account.email != null && account.id == null -> (
                    when {
                        account.login != null -> findOne(account.login)
                        account.email != null -> findOne(account.email)
                        else -> null
                    }).run {
                if (this != null) dao
                    .delete<AccountAuthorityEntity>()
                    .matching(Query.query(Criteria.where(AccountRecord.ACCOUNT_AUTH_USER_ID_FIELD).`is`(id!!)))
                    .allAndAwait()
                    .also { dao.delete(AccountEntity(this)).awaitSingle() }
            }
        }
    }
}