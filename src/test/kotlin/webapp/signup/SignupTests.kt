@file:Suppress("NonAsciiCharacters")

package webapp.signup

import jakarta.validation.Validation.byProvider
import jakarta.validation.Validator
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.validator.HibernateValidator
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.getBean
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.bindToServer
import org.springframework.test.web.reactive.server.returnResult
import webapp.*
import webapp.Constants.ACTIVATE_API_PARAM
import webapp.Constants.ACTIVATE_API_PATH
import webapp.Constants.BASE_URL_DEV
import webapp.Constants.ROLE_ADMIN
import webapp.Constants.SIGNUP_API_PATH
import webapp.DataTests.defaultAccount
import webapp.accounts.entities.AccountRecord.Companion.EMAIL_FIELD
import webapp.accounts.entities.AccountRecord.Companion.FIRST_NAME_FIELD
import webapp.accounts.entities.AccountRecord.Companion.LAST_NAME_FIELD
import webapp.accounts.entities.AccountRecord.Companion.LOGIN_FIELD
import webapp.accounts.entities.AccountRecord.Companion.PASSWORD_FIELD
import webapp.accounts.models.AccountCredentials
import webapp.accounts.models.AccountUtils.generateActivationKey
import java.net.URI
import java.util.Locale.*
import kotlin.test.*


internal class SignupTests {
    private lateinit var context: ConfigurableApplicationContext
//    private val dao: R2dbcEntityTemplate by lazy { context.getBean() }
    private val validator: Validator by lazy { context.getBean() }
    private val client: WebTestClient by lazy { bindToServer().baseUrl(BASE_URL_DEV).build() }

    @BeforeAll
    fun `lance le server en profile test`() {
        context = launcher()
    }

    @AfterAll
    fun `arrête le serveur`() = context.close()

    @AfterEach
    fun tearDown() = context.deleteAllAccounts()

    @Test
    fun `vérifie que la requête contient bien des données cohérentes`() {
        client
            .post()
            .uri("")
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount)
            .exchange()
            .returnResult<Unit>()
            .requestBodyContent!!
            .logBody()
            .requestToString()
            .run {
                defaultAccount.run {
                    setOf(
                        "\"$LOGIN_FIELD\":\"${login}\"",
                        "\"$PASSWORD_FIELD\":\"${password}\"",
                        "\"$FIRST_NAME_FIELD\":\"${firstName}\"",
                        "\"$LAST_NAME_FIELD\":\"${lastName}\"",
                        "\"$EMAIL_FIELD\":\"${email}\"",
                    ).map { assertTrue(contains(it)) }
                }
            }
    }


    @Test //TODO: mock sendmail
    fun `test signup avec un account valide`() {
        val countUserBefore = context.countAccount()
        val countUserAuthBefore = context.countAccountAuthority()
        assertEquals(0, countUserBefore)
        assertEquals(0, countUserAuthBefore)
        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount)
            .exchange()
            .expectStatus()
            .isCreated
            .returnResult<ResponseEntity<ProblemDetail>>()
            .responseBodyContent!!
            .isEmpty()
            .run { assertTrue(this) }
        assertEquals(countUserBefore + 1, context.countAccount())
        assertEquals(countUserAuthBefore + 1, context.countAccountAuthority())
        context.findOneByEmail(defaultAccount.email!!).run {
            assertNotNull(this)
            assertFalse(activated)
            assertNotNull(activationKey)
        }
    }

    @Test
    fun `test signup account validator avec login invalid`() {
        validator
            .validateProperty(AccountCredentials(login = "funky-log(n"), LOGIN_FIELD)
            .run viol@{
                assertTrue(isNotEmpty())
                first().run {
                    assertEquals(
                        "{${Pattern::class.java.name}.message}",
                        messageTemplate
                    )
                }
            }
    }

    @Test
    fun `test signup account avec login invalid`() {
        assertEquals(0, context.countAccount())
        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .header(ACCEPT_LANGUAGE, FRENCH.language)
            .bodyValue(defaultAccount.copy(login = "funky-log(n"))
            .exchange()
            .expectStatus()
            .isBadRequest
            .returnResult<ResponseEntity<ProblemDetail>>()
            .responseBodyContent!!
            .logBody()
            .isNotEmpty()
            .run { assertTrue(this) }
        assertEquals(0, context.countAccount())
    }


    @Test
    fun `test signup account avec un email invalid`() {
        val countBefore = context.countAccount()
        assertEquals(0, countBefore)
        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount.copy(password = "inv"))
            .exchange()
            .expectStatus()
            .isBadRequest
            .returnResult<ResponseEntity<ProblemDetail>>()
            .responseBodyContent!!
            .isNotEmpty()
            .run { assertTrue(this) }
        assertEquals(0, countBefore)
    }

    @Test
    fun `test signup account validator avec un password invalid`() {
        val wrongPassword = "123"
        validator
            .validateProperty(AccountCredentials(password = wrongPassword), PASSWORD_FIELD)
            .run {
                assertTrue(isNotEmpty())
                first().run {
                    assertEquals(
                        "{${Size::class.java.name}.message}",
                        messageTemplate
                    )
                }
            }
    }

    @Test
    fun `test signup account avec un password invalid`() {
        assertEquals(0, context.countAccount())
        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount.copy(password = "123"))
            .exchange()
            .expectStatus()
            .isBadRequest
            .returnResult<ResponseEntity<ProblemDetail>>()
            .responseBodyContent!!
            .logBody()
            .isNotEmpty()
            .run { assertTrue(this) }
        assertEquals(0, context.countAccount())
    }

    @Test
    fun `test signup account avec un password null`() {
        assertEquals(0, context.countAccount())
        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount.copy(password = null))
            .exchange()
            .expectStatus()
            .isBadRequest
            .returnResult<ResponseEntity<ProblemDetail>>()
            .responseBodyContent!!
            .isNotEmpty()
            .run { assertTrue(this) }
        assertEquals(0, context.countAccount())
    }

    @Test
    fun `test signup account activé avec un email existant`() {
        assertEquals(0, context.countAccount())
        assertEquals(0, context.countAccountAuthority())
        //activation de l'account
        context.createActivatedDataAccounts(setOf(defaultAccount))
        assertEquals(1, context.countAccount())
        assertEquals(1, context.countAccountAuthority())
        context.findOneByEmail(defaultAccount.email!!).run {
            assertNotNull(this)
            assertTrue(activated)
            assertNull(activationKey)
        }

        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount.copy(login = "foo"))
            .exchange()
            .expectStatus()
            .isBadRequest
            .returnResult<ResponseEntity<ProblemDetail>>()
            .responseBodyContent!!
            .isNotEmpty()
            .run { assertTrue(this) }
    }


    @Test
    fun `test signup account activé avec un login existant`() {
        assertEquals(0, context.countAccount())
        assertEquals(0, context.countAccountAuthority())
        //activation de l'account
        context.createActivatedDataAccounts(setOf(defaultAccount))
        context.findOneByEmail(defaultAccount.email!!).run {
            assertNotNull(this)
            assertTrue(activated)
            assertNull(activationKey)
        }
        assertEquals(1, context.countAccount())
        assertEquals(1, context.countAccountAuthority())

        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount.copy(email = "foo@localhost"))
            .exchange()
            .expectStatus()
            .isBadRequest
            .returnResult<ResponseEntity<ProblemDetail>>()
            .responseBodyContent!!
            .isNotEmpty()
            .run { assertTrue(this) }
    }

    @Test//TODO: mock sendmail
    fun `test signup account avec un email dupliqué`() {

        assertEquals(0, context.countAccount())
        assertEquals(0, context.countAccountAuthority())
        // premier user
        // sign up premier user
        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount)
            .exchange()
            .expectStatus()
            .isCreated
            .returnResult<Unit>()
            .responseBodyContent!!
            .isEmpty()
            .run { assertTrue(this) }
        assertEquals(1, context.countAccount())
        assertEquals(1, context.countAccountAuthority())
        assertFalse(context.findOneByEmail(defaultAccount.email!!)!!.activated)

        // email dupliqué, login different
        // sign up un second user (non activé)
        val secondLogin = "foo"
        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount.copy(login = secondLogin))
            .exchange()
            .expectStatus()
            .isCreated
            .returnResult<Unit>()
            .responseBodyContent!!
            .isEmpty()
            .run { assertTrue(this) }
        assertEquals(1, context.countAccount())
        assertEquals(1, context.countAccountAuthority())
        assertNull(context.findOneByLogin(defaultAccount.login!!))
        context.findOneByLogin(secondLogin).run {
            assertNotNull(this)
            assertEquals(defaultAccount.email!!, email)
            assertFalse(activated)
        }

        // email dupliqué - avec un email en majuscule, login différent
        // sign up un troisieme user (non activé)
        val thirdLogin = "bar"
        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount.copy(login = thirdLogin, email = defaultAccount.email!!.uppercase()))
            .exchange()
            .expectStatus()
            .isCreated
            .returnResult<Unit>()
            .responseBodyContent!!
            .isEmpty()
            .run { assertTrue(this) }
        assertEquals(1, context.countAccount())
        assertEquals(1, context.countAccountAuthority())
        context.findOneByLogin(thirdLogin).run {
            assertNotNull(this)
            assertEquals(defaultAccount.email!!, email!!.lowercase())
            assertFalse(activated)
            //activation du troisieme user
            saveAccount(copy(activated = true, activationKey = null), context.getBean())
        }
        //validation que le troisieme est actif et activationKey est null
        context.findOneByLogin(thirdLogin).run {
            assertNotNull(this)
            assertTrue(activated)
            assertNull(activationKey)
        }
        val fourthLogin = "baz"
        // sign up un quatrieme user avec login different et meme email
        // le user existant au meme mail est deja activé
        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .bodyValue(defaultAccount.copy(login = fourthLogin))
            .exchange()
            .expectStatus()
            .isBadRequest
            .returnResult<Unit>()
            .responseBodyContent!!
            .isNotEmpty()
            .run { assertTrue(this) }
        assertEquals(1, context.countAccount())
        assertEquals(1, context.countAccountAuthority())
        assertNull(context.findOneByLogin(fourthLogin))
        //meme id
        assertEquals(context.findOneByLogin(thirdLogin).apply {
            assertNotNull(this)
            assertTrue(activated)
            assertNull(activationKey)
            assertTrue(defaultAccount.email!!.equals(email!!, true))
        }!!.id, context.findOneByEmail(defaultAccount.email!!).apply {
            assertNotNull(this)
            assertTrue(activated)
            assertNull(activationKey)
            assertTrue(thirdLogin.equals(login, true))
        }!!.id
        )
    }

    @Test//TODO: mock sendmail
    fun `test signup account en renseignant l'autorité admin qui sera ignoré et le champ activé qui sera mis à false`() {
        val countUserBefore = context.countAccount()
        val countUserAuthBefore = context.countAccountAuthority()
        assertEquals(0, countUserBefore)
        assertEquals(0, countUserAuthBefore)
        val login = "badguy"
        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .bodyValue(
                AccountCredentials(
                    login = login,
                    password = "password",
                    firstName = "Bad",
                    lastName = "Guy",
                    email = "badguy@example.com",
                    activated = true,
                    imageUrl = "http://placehold.it/50x50",
                    langKey = Constants.DEFAULT_LANGUAGE,
                    authorities = setOf(ROLE_ADMIN),
                )
            )
            .exchange()
            .expectStatus()
            .isCreated
            .returnResult<Unit>()
            .responseBodyContent.run {
                assertNotNull(this)
                assertTrue(isEmpty())
            }
        assertEquals(countUserBefore + 1, context.countAccount())
        assertEquals(countUserAuthBefore + 1, context.countAccountAuthority())
        context.findOneByLogin(login).run {
            assertNotNull(this)
            assertFalse(activated)
            assertFalse(activationKey.isNullOrBlank())
        }
        assertTrue(context.findAllAccountAuthority().none {
            it.role.equals(ROLE_ADMIN, true)
        })
    }

    @Test
    fun `vérifie l'internationalisation des validations par validator factory avec mauvais login en italien`() {
        byProvider(HibernateValidator::class.java)
            .configure()
            .defaultLocale(ENGLISH)
            .locales(FRANCE, ITALY, US)
            .localeResolver {
                // get the locales supported by the client from the Accept-Language header
                val acceptLanguageHeader = "it-IT;q=0.9,en-US;q=0.7"
                val acceptedLanguages = LanguageRange.parse(acceptLanguageHeader)
                val resolvedLocales = filter(acceptedLanguages, it.supportedLocales)
                if (resolvedLocales.size > 0) resolvedLocales[0]
                else it.defaultLocale
            }
            .buildValidatorFactory()
            .validator
            .validateProperty(defaultAccount.copy(login = "funky-log(n"), LOGIN_FIELD)
            .run viol@{
                assertTrue(isNotEmpty())
                first().run {
                    assertEquals(
                        "{${Pattern::class.java.name}.message}",
                        messageTemplate
                    )
                    assertEquals(false, message.contains("doit correspondre à"))
                    assertContains(
                        "deve corrispondere a \"^(?>[a-zA-Z0-9!\$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)\$\"",
                        message
                    )
                }
            }
    }

    @Test
    fun `vérifie l'internationalisation des validations par REST avec mot de passe non conforme en francais`() {
        assertEquals(0, context.countAccount())
        client
            .post()
            .uri(SIGNUP_API_PATH)
            .contentType(APPLICATION_JSON)
            .header(ACCEPT_LANGUAGE, FRENCH.language)
            .bodyValue(defaultAccount.copy(password = "123"))
            .exchange()
            .expectStatus()
            .isBadRequest
            .returnResult<ResponseEntity<ProblemDetail>>()
            .responseBodyContent!!
            .run {
                assertTrue(isNotEmpty())
                assertContains(requestToString(), "la taille doit")
            }
        assertEquals(0, context.countAccount())

    }


    @Test
    fun `test activate avec une mauvaise clé`() {
        client
            .get()
            .uri("$ACTIVATE_API_PATH$ACTIVATE_API_PARAM", "wrongActivationKey")
            .exchange()
            .expectStatus()
            .is5xxServerError
            .returnResult<ProblemDetail>()
    }

    @Test
    fun `test activate avec une clé valide`() {
        assertEquals(0, context.countAccount())
        assertEquals(0, context.countAccountAuthority())
        context.createDataAccounts(setOf(defaultAccount))
        assertEquals(1, context.countAccount())
        assertEquals(1, context.countAccountAuthority())

        client
            .get()
            .uri(
                "$ACTIVATE_API_PATH$ACTIVATE_API_PARAM",
                context.findOneByLogin(defaultAccount.login!!)!!.apply {
                    assertTrue(activationKey!!.isNotBlank())
                    assertFalse(activated)
                }.activationKey
            ).exchange()
            .expectStatus()
            .isOk
            .returnResult<ProblemDetail>()

        context.findOneByLogin(defaultAccount.login!!)!!.run {
            assertNull(activationKey)
            assertTrue(activated)
        }
    }

    @Test
    fun `vérifie que la requête avec mauvaise URI renvoi la bonne URL erreur`() {
        generateActivationKey.run {
            client
                .get()
                .uri("$ACTIVATE_API_PATH$ACTIVATE_API_PARAM", this)
                .exchange()
                .returnResult<ProblemDetail>()
                .url
                .let { assertEquals(URI("$BASE_URL_DEV$ACTIVATE_API_PATH$this"), it) }
        }
    }
}