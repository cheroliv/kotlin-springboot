@file:Suppress("NonAsciiCharacters")

package webapp.password

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.springframework.context.ConfigurableApplicationContext
import webapp.deleteAllAccounts
import webapp.launcher
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
    fun `canary`() {

    }
}