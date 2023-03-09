@file:Suppress("NonAsciiCharacters")

package webapp.repository

import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.getBean
import org.springframework.context.ConfigurableApplicationContext
import webapp.*
import webapp.DataTests.accounts
import webapp.DataTests.defaultAccount
import webapp.accounts.models.AccountUtils.generateActivationKey
import webapp.accounts.models.toAccount
import webapp.accounts.repository.AccountRepository
import webapp.accounts.repository.AccountRepositoryR2dbc
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class AccountRepositoryR2dbcTest {
    private lateinit var context: ConfigurableApplicationContext
    private val accountRepository: AccountRepository by lazy { context.getBean<AccountRepositoryR2dbc>() }

    @BeforeAll
    fun `lance le server en profile test`() {
        context = launcher()
    }

    @AfterAll
    fun `arrête le serveur`() = context.close()


    @AfterEach
    fun tearDown() = context.deleteAllAccounts()


    @Test
    fun test_save() {
        mono {
            val countBefore = context.countAccount
            assertEquals(0, countBefore)
            accountRepository.save(defaultAccount)
            assertEquals(countBefore + 1, context.countAccount)
        }
    }

    @Test
    fun test_delete() = runBlocking {
        assertEquals(0, context.countAccount)
        context.createDataAccounts(accounts)
        assertEquals(accounts.size, context.countAccount)
        assertEquals(accounts.size + 1, context.countAccountAuthority)
        accountRepository.delete(defaultAccount.toAccount())
        assertEquals(accounts.size - 1, context.countAccount)
        assertEquals(accounts.size, context.countAccountAuthority)
    }

    @Test
    fun test_findOne_with_Email() = runBlocking {
        assertEquals(0, context.countAccount)
        context.createDataAccounts(accounts)
        assertEquals(accounts.size, context.countAccount)
        assertEquals(
            defaultAccount.login,
            accountRepository.findOne(defaultAccount.email!!)!!.login
        )
    }

    @Test
    fun test_findOne_with_Login() = runBlocking {
        assertEquals(0, context.countAccount)
        context.createDataAccounts(accounts)
        assertEquals(accounts.size, context.countAccount)
        assertEquals(
            defaultAccount.email,
            accountRepository.findOne(defaultAccount.login!!)!!.email
        )
    }

    @Test
    fun test_signup() {
        assertEquals(0, context.countAccount)
        assertEquals(0, context.countAccountAuthority)
        runBlocking {
            accountRepository.signup(
                defaultAccount.copy(
                    activationKey = generateActivationKey,
                    langKey = DEFAULT_LANGUAGE,
                    createdBy = SYSTEM_USER,
                    createdDate = Instant.now(),
                    lastModifiedBy = SYSTEM_USER,
                    lastModifiedDate = Instant.now(),
                    authorities = mutableSetOf(ROLE_USER)
                )
            )
        }
        assertEquals(1, context.countAccount)
        assertEquals(1, context.countAccountAuthority)
    }

    @Test
    fun test_findActivationKeyByLogin() {
        assertEquals(0, context.countAccount)
        context.createDataAccounts(accounts)
        assertEquals(accounts.size, context.countAccount)
        assertEquals(accounts.size + 1, context.countAccountAuthority)
        runBlocking {
            assertEquals(
                context.findOneByEmail(defaultAccount.email!!)!!.activationKey,
                accountRepository.findActivationKeyByLogin(defaultAccount.login!!)
            )
        }
    }

    @Test
    fun test_findOneByActivationKey() {
        assertEquals(0, context.countAccount)
        context.createDataAccounts(accounts)
        assertEquals(accounts.size, context.countAccount)
        assertEquals(accounts.size + 1, context.countAccountAuthority)
        context.findOneByLogin(defaultAccount.login!!).run findOneByLogin@{
            assertNotNull(this@findOneByLogin)
            assertNotNull(this@findOneByLogin.activationKey)
            runBlocking {
                accountRepository.findOneByActivationKey(this@findOneByLogin.activationKey!!)
                    .run findOneByActivationKey@{
                        assertNotNull(this@findOneByActivationKey)
                        assertNotNull(this@findOneByActivationKey.id)
                        assertEquals(
                            this@findOneByLogin.id,
                            this@findOneByActivationKey.id
                        )
                    }
            }
        }
    }
}