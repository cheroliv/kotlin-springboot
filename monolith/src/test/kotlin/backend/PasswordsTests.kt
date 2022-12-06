package backend

//import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.getBean
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.web.reactive.server.WebTestClient
//import org.springframework.test.web.reactive.server.returnResult
//import java.net.URI
import kotlin.test.*
@Suppress("unused")
internal class ChangePasswordControllerTests {

    /*
        @Test
        @WithMockUser("change-password-wrong-existing-password")
        fun testChangePasswordWrongExistingPassword() {
            val currentPassword = RandomStringUtils.random(60)
            val user = User(
                password = passwordEncoder.encode(currentPassword),
                login = "change-password-wrong-existing-password",
                createdBy = SYSTEM_ACCOUNT,
                email = "change-password-wrong-existing-password@example.com"
            )

            userRepository.save(user).block()

            accountWebTestClient.post().uri("/api/account/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(convertObjectToJsonBytes(PasswordChangeDTO("1$currentPassword", "new password")))
                .exchange()
                .expectStatus().isBadRequest

            val updatedUser = userRepository.findOneByLogin("change-password-wrong-existing-password").block()
            assertThat(passwordEncoder.matches("new password", updatedUser.password)).isFalse
            assertThat(passwordEncoder.matches(currentPassword, updatedUser.password)).isTrue
        }

        @Test
        @WithMockUser("change-password")
        fun testChangePassword() {
            val currentPassword = RandomStringUtils.random(60)
            val user = User(
                password = passwordEncoder.encode(currentPassword),
                login = "change-password",
                createdBy = SYSTEM_ACCOUNT,
                email = "change-password@example.com"
            )

            userRepository.save(user).block()

            accountWebTestClient.post().uri("/api/account/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(convertObjectToJsonBytes(PasswordChangeDTO(currentPassword, "new password")))
                .exchange()
                .expectStatus().isOk

            val updatedUser = userRepository.findOneByLogin("change-password").block()
            assertThat(passwordEncoder.matches("new password", updatedUser.password)).isTrue
        }

        @Test
        @WithMockUser("change-password-too-small")
        fun testChangePasswordTooSmall() {
            val currentPassword = RandomStringUtils.random(60)
            val user = User(
                password = passwordEncoder.encode(currentPassword),
                login = "change-password-too-small",
                createdBy = SYSTEM_ACCOUNT,
                email = "change-password-too-small@example.com"
            )

            userRepository.save(user).block()

            val newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MIN_LENGTH - 1)

            accountWebTestClient.post().uri("/api/account/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(convertObjectToJsonBytes(PasswordChangeDTO(currentPassword, newPassword)))
                .exchange()
                .expectStatus().isBadRequest

            val updatedUser = userRepository.findOneByLogin("change-password-too-small").block()
            assertThat(updatedUser.password).isEqualTo(user.password)
        }

        @Test
        @WithMockUser("change-password-too-long")
        fun testChangePasswordTooLong() {
            val currentPassword = RandomStringUtils.random(60)
            val user = User(
                password = passwordEncoder.encode(currentPassword),
                login = "change-password-too-long",
                createdBy = SYSTEM_ACCOUNT,
                email = "change-password-too-long@example.com"
            )

            userRepository.save(user).block()

            val newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MAX_LENGTH + 1)

            accountWebTestClient.post().uri("/api/account/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(convertObjectToJsonBytes(PasswordChangeDTO(currentPassword, newPassword)))
                .exchange()
                .expectStatus().isBadRequest

            val updatedUser = userRepository.findOneByLogin("change-password-too-long").block()
            assertThat(updatedUser.password).isEqualTo(user.password)
        }

        @Test
        @WithMockUser("change-password-empty")
        fun testChangePasswordEmpty() {
            val currentPassword = RandomStringUtils.random(60)
            val user = User(
                password = passwordEncoder.encode(currentPassword),
                login = "change-password-empty",
                createdBy = SYSTEM_ACCOUNT,
                email = "change-password-empty@example.com"
            )

            userRepository.save(user).block()

            accountWebTestClient.post().uri("/api/account/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(convertObjectToJsonBytes(PasswordChangeDTO(currentPassword, "")))
                .exchange()
                .expectStatus().isBadRequest

            val updatedUser = userRepository.findOneByLogin("change-password-empty").block()
            assertThat(updatedUser.password).isEqualTo(user.password)
        }
     */

}
internal class ResetPasswordControllerTest {



    private val client: WebTestClient by lazy {
        WebTestClient
            .bindToServer()
            .baseUrl(BASE_URL_DEV)
            .build()
    }
    private lateinit var context: ConfigurableApplicationContext
    private val dao: R2dbcEntityTemplate by lazy { context.getBean() }

    @BeforeAll
    fun `lance le server en profile test`() =
        runApplication<BackendApplication> { testLoader(app = this) }
            .run { context = this }

    @AfterAll
    fun `arrête le serveur`() = context.close()

    @AfterEach
    fun tearDown() = deleteAllAccounts(dao)
    @Test
    fun `test Request Password Reset`(){}
//    val user = User(
//        password = RandomStringUtils.random(60),
//        activated = true,
//        login = "password-reset",
//        createdBy = SYSTEM_ACCOUNT,
//        email = "password-reset@example.com"
//    )
//
//    userRepository.save(user).block()
//
//    accountWebTestClient.post().uri("/api/account/reset-password/init")
//        .bodyValue("password-reset@example.com")
//        .exchange()
//        .expectStatus().isOk


    @Test
    fun `test Request Password Reset UpperCaseEmail`(){}
//    val user = User(
//        password = RandomStringUtils.random(60),
//        activated = true,
//        login = "password-reset-upper-case",
//        createdBy = SYSTEM_ACCOUNT,
//        email = "password-reset-upper-case@example.com"
//    )
//
//    userRepository.save(user).block()
//
//    accountWebTestClient.post().uri("/api/account/reset-password/init")
//    .bodyValue("password-reset-upper-case@EXAMPLE.COM")
//    .exchange()
//    .expectStatus().isOk

    @Test
    fun `test Request Password Reset Wrong Email`() {
//        accountWebTestClient.post().uri("/api/account/reset-password/init")
//            .bodyValue("password-reset-wrong-email@example.com")
//            .exchange()
//            .expectStatus().isOk
    }

    @Test
//    @Throws(Exception::class)
    fun `test Finish Password Reset`() {
//        val user = User(
//            password = RandomStringUtils.random(60),
//            login = "finish-password-reset",
//            email = "finish-password-reset@example.com",
//            resetDate = Instant.now().plusSeconds(60),
//            createdBy = SYSTEM_ACCOUNT,
//            resetKey = "reset key"
//        )
//
//        userRepository.save(user).block()
//
//        val keyAndPassword = KeyAndPasswordVM(key = user.resetKey, newPassword = "new password")
//
//        accountWebTestClient.post().uri("/api/account/reset-password/finish")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(convertObjectToJsonBytes(keyAndPassword))
//            .exchange()
//            .expectStatus().isOk
//
//        val updatedUser = userRepository.findOneByLogin(user.login!!).block()
//        assertThat(passwordEncoder.matches(keyAndPassword.newPassword, updatedUser.password)).isTrue
    }

    @Test
//    @Throws(Exception::class)
    fun `test Finish Password Reset Too Small`() {
//        val user = User(
//            password = RandomStringUtils.random(60),
//            login = "finish-password-reset-too-small",
//            email = "finish-password-reset-too-small@example.com",
//            resetDate = Instant.now().plusSeconds(60),
//            createdBy = SYSTEM_ACCOUNT,
//            resetKey = "reset key too small"
//        )
//
//        userRepository.save(user).block()
//
//        val keyAndPassword = KeyAndPasswordVM(key = user.resetKey, newPassword = "foo")
//
//        accountWebTestClient.post().uri("/api/account/reset-password/finish")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(convertObjectToJsonBytes(keyAndPassword))
//            .exchange()
//            .expectStatus().isBadRequest
//
//        val updatedUser = userRepository.findOneByLogin(user.login!!).block()
//        assertThat(passwordEncoder.matches(keyAndPassword.newPassword, updatedUser.password)).isFalse
    }

    @Test
//    @Throws(Exception::class)
    fun `test Finish Password Reset Wrong Key`() {
//        val keyAndPassword = KeyAndPasswordVM(key = "wrong reset key", newPassword = "new password")
//
//        accountWebTestClient.post().uri("/api/account/reset-password/finish")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(convertObjectToJsonBytes(keyAndPassword))
//            .exchange()
//            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
