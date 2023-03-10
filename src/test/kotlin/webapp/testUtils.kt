@file:Suppress("unused")

package webapp

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.springframework.beans.factory.getBean
import org.springframework.boot.runApplication
import org.springframework.boot.web.reactive.context.StandardReactiveWebEnvironment
import org.springframework.context.ApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.security.authentication.RememberMeAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import webapp.entities.AccountAuthorityEntity
import webapp.entities.AccountEntity
import webapp.logging.i
import webapp.models.Account
import webapp.models.AccountCredentials
import webapp.models.AccountUtils.generateActivationKey
import webapp.security.Security
import java.io.IOException
import java.lang.Byte.parseByte
import java.time.Instant.now
import java.time.ZonedDateTime
import java.time.ZonedDateTime.parse
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


fun launcher(vararg profiles: String) = runApplication<Application> {
    /**
     * before launching: configuration
     */
    setEnvironment(StandardReactiveWebEnvironment().apply {
        setDefaultProfiles(TEST)
        addActiveProfile(TEST)
        profiles.toSet().map { addActiveProfile(it) }
    })
}.apply {
    /**
     * after launching: verification & post construct
     */
    (if (environment.defaultProfiles.isNotEmpty()) environment
        .defaultProfiles
        .reduce { acc, s -> "$acc, $s" }
    else "").run { i("defaultProfiles: $this") }

    (if (environment.activeProfiles.isNotEmpty()) environment
        .activeProfiles
        .reduce { acc, s -> "$acc, $s" }
    else "").run { i("activeProfiles: $this") }
}

fun ApplicationContext.userToken(accountCredentials: AccountCredentials) = mono {
    getBean<Security>().createToken(
        RememberMeAuthenticationToken(
            accountCredentials.login,
            getBean<ReactiveUserDetailsService>()
                .findByUsername(accountCredentials.email)
                .awaitSingleOrNull(),
            setOf(GrantedAuthority { ROLE_USER }),
        ), true
    )
}.block()!!

fun ApplicationContext.createActivatedUserAndAdmin() {
    val countUserBefore = countAccount
    val countUserAuthBefore = countAccountAuthority
    assertEquals(0, countUserBefore)
    assertEquals(0, countUserAuthBefore)
    createActivatedDataAccounts(setOf(defaultAccount, adminAccount))
    assertEquals(2, countAccount)
    assertEquals(3, countAccountAuthority)
    findOneByEmail(defaultAccount.email!!).run {
        assertNotNull(this)
        assertTrue(activated)
        assertNull(activationKey)
    }
    findOneByEmail(adminAccount.email!!).run {
        assertNotNull(this)
        assertTrue(activated)
        assertNull(activationKey)
    }
}

fun ByteArray.requestToString(): String = map {
    it.toInt().toChar().toString()
}.reduce { acc: String, s: String -> acc + s }

fun ApplicationContext.createDataAccounts(accounts: Set<AccountCredentials>) {
    assertEquals(0, countAccount)
    assertEquals(0, countAccountAuthority)
    accounts.map { acc ->
        AccountEntity(acc.copy(
            activationKey = generateActivationKey,
            langKey = DEFAULT_LANGUAGE,
            createdBy = SYSTEM_USER,
            createdDate = now(),
            lastModifiedBy = SYSTEM_USER,
            lastModifiedDate = now(),
            authorities = mutableSetOf(ROLE_USER).apply {
                if (acc.login == ADMIN) add(ROLE_ADMIN)
            }
        )).run {
            getBean<R2dbcEntityTemplate>().let {
                it.insert(this).block()!!.id!!.let { uuid ->
                    authorities!!.map { authority ->
                        it.insert(
                            AccountAuthorityEntity(
                                userId = uuid,
                                role = authority.role
                            )
                        ).block()
                    }
                }
            }
        }
    }
    assertEquals(accounts.size, countAccount)
    assertTrue(accounts.size <= countAccountAuthority)
}


val ByteArray.logBody: ByteArray
    get() = apply {
        if (isNotEmpty()) map { it.toInt().toChar().toString() }
            .reduce { request, s ->
                request + buildString {
                    append(s)
                    if (s == VIRGULE && request.last().isDigit())
                        append("\n\t")
                }
            }.replace("{\"", "\n{\n\t\"")
            .replace("\"}", "\"\n}")
            .replace("\",\"", "\",\n\t\"")
            .run { i("\nbody:$this") }
    }

val ByteArray.logBodyRaw: ByteArray
    get() = apply {
        if (isNotEmpty()) map {
            it.toInt()
                .toChar()
                .toString()
        }.reduce { request, s -> request + s }
            .run { i(this) }
    }


fun ApplicationContext.createActivatedDataAccounts(accounts: Set<AccountCredentials>) {
    assertEquals(0, countAccount)
    assertEquals(0, countAccountAuthority)
    accounts.map { acc ->
        AccountEntity(
            acc.copy(
                activated = true,
                langKey = DEFAULT_LANGUAGE,
                createdBy = SYSTEM_USER,
                createdDate = now(),
                lastModifiedBy = SYSTEM_USER,
                lastModifiedDate = now(),
                authorities = mutableSetOf(ROLE_USER).apply {
                    if (acc.login == ADMIN) add(ROLE_ADMIN)
                }.toSet()
            )
        ).run {
            getBean<R2dbcEntityTemplate>().let { dao ->
                dao.insert(this).block()!!.id!!.let { uuid ->
                    authorities!!.map { authority ->
                        dao.insert(
                            AccountAuthorityEntity(
                                userId = uuid,
                                role = authority.role
                            )
                        ).block()
                    }
                }
            }
        }
    }
    assertEquals(accounts.size, countAccount)
    assertTrue(accounts.size <= countAccountAuthority)
}

fun ApplicationContext.deleteAllAccounts() {
    deleteAllAccountAuthority()
    deleteAccounts()
    assertEquals(0, countAccount)
    assertEquals(0, countAccountAuthority)
}

fun ApplicationContext.deleteAccounts() {
    getBean<R2dbcEntityTemplate>().delete(AccountEntity::class.java).all().block()
}

fun ApplicationContext.deleteAllAccountAuthority() {
    getBean<R2dbcEntityTemplate>().delete(AccountAuthorityEntity::class.java).all().block()
}

//TODO: revoir les updates avec id!=null
fun ApplicationContext.saveAccount(model: AccountCredentials): Account? =
    getBean<R2dbcEntityTemplate>().run {
        when {
            model.id != null -> update(
                AccountEntity(model).copy(
                    version = selectOne(
                        query(
                            where("login").`is`(model.login!!).ignoreCase(true)
                        ), AccountEntity::class.java
                    ).block()!!.version
                )
            ).block()?.toModel

            else -> insert(AccountEntity(model)).block()?.toModel
        }

    }

fun ApplicationContext.saveAccountAuthority(
    id: UUID,
    role: String,
): AccountAuthorityEntity? = getBean<R2dbcEntityTemplate>()
    .insert(AccountAuthorityEntity(userId = id, role = role))
    .block()


val ApplicationContext.countAccount: Int
    get() = getBean<R2dbcEntityTemplate>()
        .select(AccountEntity::class.java)
        .count().block()?.toInt()!!


val ApplicationContext.countAccountAuthority: Int
    get() = getBean<R2dbcEntityTemplate>()
        .select(AccountAuthorityEntity::class.java)
        .count()
        .block()
        ?.toInt()!!


fun ApplicationContext.findOneByLogin(login: String): AccountCredentials? =
    getBean<R2dbcEntityTemplate>()
        .select<AccountEntity>()
        .matching(query(where("login").`is`(login).ignoreCase(true)))
        .one()
        .block()
        ?.toCredentialsModel

fun ApplicationContext.findOneByEmail(email: String): AccountCredentials? =
    getBean<R2dbcEntityTemplate>()
        .select<AccountEntity>()
        .matching(query(where("email").`is`(email).ignoreCase(true)))
        .one()
        .block()
        ?.toCredentialsModel

val ApplicationContext.findAllAccountAuthority: Set<AccountAuthorityEntity>
    get() = getBean<R2dbcEntityTemplate>()
        .select(AccountAuthorityEntity::class.java)
        .all()
        .toIterable()
        .toHashSet()

private val mapperFactory
    get() = ObjectMapper().apply {
        configure(WRITE_DURATIONS_AS_TIMESTAMPS, false)
        setSerializationInclusion(NON_EMPTY)
        registerModule(JavaTimeModule())
    }

/**
 * Convert an object to JSON byte array.
 *
 * @param object the object to convert.
 * @return the JSON byte array.
 * @throws IOException
 */
@Throws(IOException::class)
fun convertObjectToJsonBytes(`object`: Any): ByteArray = mapperFactory.writeValueAsBytes(`object`)

/**
 * Create a byte array with a specific size filled with specified data.
 *
 * @param size the size of the byte array.
 * @param data the data to put in the byte array.
 * @return the JSON byte array.
 */
fun createByteArray(size: Int, data: String) = ByteArray(size) { parseByte(data, 2) }

/**
 * A matcher that tests that the examined string represents the same instant as the reference datetime.
 */
class ZonedDateTimeMatcher(private val date: ZonedDateTime) : TypeSafeDiagnosingMatcher<String>() {

    override fun matchesSafely(item: String, mismatchDescription: Description): Boolean {
        try {
            if (!date.isEqual(parse(item))) {
                mismatchDescription.appendText("was ").appendValue(item)
                return false
            }
            return true
        } catch (e: DateTimeParseException) {
            mismatchDescription.appendText("was ")
                .appendValue(item)
                .appendText(", which could not be parsed as a ZonedDateTime")
            return false
        }
    }

    override fun describeTo(description: Description) {
        description.appendText("a String representing the same Instant as ")
            .appendValue(date)
    }
}

/**
 * Creates a matcher that matches when the examined string represents the same instant as the reference datetime.
 * @param date the reference datetime against which the examined string is checked.
 */
fun sameInstant(date: ZonedDateTime) = ZonedDateTimeMatcher(date)

/**
 * Verifies the equals/hashcode contract on the domain object.
 */
fun <T : Any> equalsVerifier(clazz: KClass<T>) {
    clazz.createInstance().apply i@{
        assertThat(toString()).isNotNull
        assertThat(this).isEqualTo(this)
        assertThat(hashCode()).isEqualTo(hashCode())
        // Test with an instance of another class
        assertThat(this).isNotEqualTo(Any())
        assertThat(this).isNotEqualTo(null)
        // Test with an instance of the same class
        clazz.createInstance().apply j@{
            assertThat(this@i).isNotEqualTo(this@j)
            // HashCodes are equals because the objects are not persisted yet
            assertThat(this@i.hashCode()).isEqualTo(this@j.hashCode())
        }
    }
}

val token64Zero
    get() = mutableListOf<String>().apply {
        repeat(64) { add(0.toString()) }
    }.reduce { acc, i -> "$acc$i" }
