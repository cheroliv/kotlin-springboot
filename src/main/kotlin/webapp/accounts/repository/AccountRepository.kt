package webapp.accounts.repository

import webapp.accounts.entities.AccountRecord
import webapp.accounts.models.Account
import webapp.accounts.models.AccountCredentials

interface AccountRepository {
    suspend fun save(model: AccountCredentials): Account?
    suspend fun findOne(emailOrLogin: String): AccountCredentials?
    suspend fun findOneWithAuthorities(emailOrLogin: String): AccountCredentials?
    suspend fun findOneByActivationKey(key: String): AccountCredentials?
    suspend fun findOneByResetKey(key: String): AccountRecord<*>?//<AuthorityRecord>?
    suspend fun findActivationKeyByLogin(login: String): String?
    suspend fun delete(account: Account)
    suspend fun signup(accountCredentials: AccountCredentials):AccountCredentials?
}