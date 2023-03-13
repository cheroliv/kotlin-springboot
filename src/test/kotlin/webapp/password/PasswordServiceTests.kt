@file:Suppress("NonAsciiCharacters")

package webapp.password

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.springframework.context.ConfigurableApplicationContext
import webapp.deleteAllAccounts
import webapp.launcher
import webapp.logging.i
import kotlin.test.Test

class PasswordServiceTests {
    private lateinit var context: ConfigurableApplicationContext

    @BeforeAll
    fun `lance le server en profile test`() {
        context = launcher()
    }

    @AfterAll
    fun `arrête le serveur`() = context.close()

    @AfterEach
    fun tearDown() = context.deleteAllAccounts()

    @Test
    fun `changePassword(), le currentPassword est invalid`() {
        i("changePassword(): le currentPassword est invalid")
    }

    @Test
    fun `changePassword(), le newPassword est invalid`() {
        i("le newPassword(): est invalid")
    }

    @Test
    fun `changePassword(), le currentPassword ne match pas`() {
        i("changePassword(): le currentPassword ne match pas")

    }

    @Test
    fun `changePassword(), le currentPassword et le newPassword sont valide`() {
        i("changePassword(): le currentPassword et le newPassword sont valide")
    }

    @Test
    fun `completePasswordReset(), le newPassword n'est pas valide`() {
        i("completePasswordReset(): le currentPassword et le newPassword sont valide")
    }

    @Test
    fun `completePasswordReset(), la key n'existe pas`() {
        i("completePasswordReset(): la key n'existe pas")
    }
    @Test
    fun `completePasswordReset(), le newPassword est valide & la key existe`() {
        i("completePasswordReset(): le newPassword est valide & la key existe")
    }

    @Test
    fun `requestPasswordReset(), l'email n'existe pas`() {
        i("requestPasswordReset(): l'email n'existe pas")
    }

    @Test
    fun `requestPasswordReset(), l'email existe`() {
        i("requestPasswordReset(): l'email existe")
    }
}