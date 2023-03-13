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
    fun `le currentPassword est invalid`() {
        i("le currentPassword est invalid")
    }

    @Test
    fun `le newPassword est invalid`() {
        i("le newPassword est invalid")
    }

    @Test
    fun `le currentPassword ne match pas`() {
        i("le currentPassword ne match pas")

    }

    @Test
    fun `le currentPassword et le newPassword sont valide`() {
        i("le currentPassword et le newPassword sont valide")
    }
}