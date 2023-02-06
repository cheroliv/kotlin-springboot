package webapp

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.getBean
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.MessageSource
import webapp.Constants.DEVELOPMENT
import webapp.Constants.PRODUCTION
import webapp.Constants.STARTUP_LOG_MSG_KEY
import webapp.Logging.i
import java.util.*
import java.util.Locale.FRENCH
import java.util.Locale.getDefault
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigurationsTests {

    private lateinit var context: ConfigurableApplicationContext
    private val messageSource: MessageSource by lazy { context.getBean() }
    private val properties: Properties by lazy { context.getBean() }

    @BeforeAll
    fun `lance le server en profile test`() = launcher().run { context = this }

    @Suppress("NonAsciiCharacters")
    @AfterAll
    fun `arrête le serveur`() = context.close()

    @Test
    fun `MessageSource test email_activation_greeting message fr`() {
        "artisan-logiciel".apply {
            assertEquals(
                expected = "Cher $this",
                actual = messageSource.getMessage(
                    "email.activation.greeting",
                    arrayOf(this),
                    FRENCH
                )
            )
        }
    }

    @Test
    fun `MessageSource test message startupLog`() {
        val i18nMsg = messageSource.getMessage(
            STARTUP_LOG_MSG_KEY,
            arrayOf(
                DEVELOPMENT,
                PRODUCTION
            ),
            getDefault()
        ).run {
            i(this)
            assertEquals(buildString {
                append("You have misconfigured your application!\n")
                append("It should not run with both the $DEVELOPMENT\n")
                append("and $PRODUCTION profiles at the same time.")
            }, this)
        }
    }

    @Test
    fun `test go visit message`() {
        assertEquals(
            "https://github.com/artisan-logiciel/kotlin-springboot",
            properties.goVisitMessage
        )

    }
}