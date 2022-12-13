@file:Suppress("NonAsciiCharacters")

package backend

import backend.Constants.SPRING_PROFILE_GMAIL
import backend.Constants.SPRING_PROFILE_MAILSLURP
import backend.RandomUtils.generateResetKey
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doNothing
import org.mockito.MockitoAnnotations.openMocks
import org.springframework.beans.factory.getBean
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.MessageSource
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.thymeleaf.spring5.SpringWebFluxTemplateEngine
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.URI
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern
import java.util.regex.Pattern.compile
import javax.mail.Multipart
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import kotlin.test.Test
import kotlin.test.assertNotNull

/*=================================================================================*/

class GmailServiceTests {

    private lateinit var context: ConfigurableApplicationContext
    private lateinit var mailService: MailService
    lateinit var javaMailSender: JavaMailSenderImpl

    @BeforeAll
    fun `lance le server en profile test`() {
        context = launcher(SPRING_PROFILE_GMAIL)
    }

    @AfterAll
    fun `arrête le serveur`() = context.close()
}
/*=================================================================================*/

class MailSlurpServiceTests {

    private lateinit var context: ConfigurableApplicationContext
    private lateinit var mailService: MailService
    lateinit var javaMailSender: JavaMailSenderImpl

    @BeforeAll
    fun `lance le server en profile test`() {
        context = launcher(SPRING_PROFILE_MAILSLURP)
    }

    @AfterAll
    fun `arrête le serveur`() = context.close()
}
/*=================================================================================*/

class DefaultMailServiceTests {

    private lateinit var context: ConfigurableApplicationContext
    private lateinit var mailService: MailService

    private val properties: ApplicationProperties by lazy { context.getBean() }

    private val messageSource: MessageSource by lazy { context.getBean() }

    private val templateEngine: SpringWebFluxTemplateEngine by lazy { context.getBean() }

    @Spy
    lateinit var javaMailSender: JavaMailSenderImpl

    @Captor
    lateinit var messageCaptor: ArgumentCaptor<MimeMessage>


    @BeforeAll
    fun `lance le server en profile test`() {
        context = launcher()
    }


    @Suppress("NonAsciiCharacters")
    @AfterAll
    fun `arrête le serveur`() = context.close()

    @BeforeEach
    fun setup() {
        openMocks(this)
        doNothing()
            .`when`(javaMailSender)
            .send(any(MimeMessage::class.java))
        mailService = MailService(
            properties,
            javaMailSender,
            messageSource,
            templateEngine
        )
    }


    @Test
    fun `test sendEmail`() {
        mailService.sendEmail(
            "john.doe@acme.com", "testSubject", "testContent", false, false
        )
        Mockito.verify(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        Assertions.assertThat(message.subject).isEqualTo("testSubject")
        Assertions.assertThat(message.allRecipients[0]).hasToString("john.doe@acme.com")
        Assertions.assertThat(message.from[0]).hasToString(properties.mail.from)
        Assertions.assertThat(message.content).isInstanceOf(String::class.java)
        Assertions.assertThat(message.content).hasToString("testContent")
        Assertions.assertThat(message.dataHandler.contentType).isEqualTo("text/plain; charset=UTF-8")
    }

    @Test
    fun `test sendMail SendHtmlEmail`() {
        mailService.sendEmail(
            "john.doe@acme.com", "testSubject", "testContent", isMultipart = false, isHtml = true
        )
        Mockito.verify(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        Assertions.assertThat(message.subject).isEqualTo("testSubject")
        Assertions.assertThat("${message.allRecipients[0]}").isEqualTo("john.doe@acme.com")
        Assertions.assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
        Assertions.assertThat(message.content).isInstanceOf(String::class.java)
        Assertions.assertThat(message.content.toString()).isEqualTo("testContent")
        Assertions.assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    fun `test sendMail SendMultipartEmail`() {
        mailService.sendEmail(
            "john.doe@acme.com", "testSubject", "testContent", isMultipart = true, isHtml = false
        )
        Mockito.verify(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        val part =
            ((message.content as MimeMultipart).getBodyPart(0).content as MimeMultipart).getBodyPart(0) as MimeBodyPart
        val aos = ByteArrayOutputStream()
        part.writeTo(aos)
        Assertions.assertThat(message.subject).isEqualTo("testSubject")
        Assertions.assertThat("${message.allRecipients[0]}").isEqualTo("john.doe@acme.com")
        Assertions.assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
        Assertions.assertThat(message.content).isInstanceOf(Multipart::class.java)
        Assertions.assertThat("$aos").isEqualTo("\r\ntestContent")
        Assertions.assertThat(part.dataHandler.contentType).isEqualTo("text/plain; charset=UTF-8")
    }

    @Test
    fun `test sendMail SendMultipartHtmlEmail`() {
        mailService.sendEmail(
            "john.doe@acme.com", "testSubject", "testContent", isMultipart = true, isHtml = true
        )
        Mockito.verify(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        val part =
            ((message.content as MimeMultipart).getBodyPart(0).content as MimeMultipart).getBodyPart(0) as MimeBodyPart
        val aos = ByteArrayOutputStream()
        part.writeTo(aos)
        Assertions.assertThat(message.subject).isEqualTo("testSubject")
        Assertions.assertThat("${message.allRecipients[0]}").isEqualTo("john.doe@acme.com")
        Assertions.assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
        Assertions.assertThat(message.content).isInstanceOf(Multipart::class.java)
        Assertions.assertThat("$aos").isEqualTo("\r\ntestContent")
        Assertions.assertThat(part.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    fun `test SendEmailFromTemplate`() {
        val user = AccountCredentials(
            login = "john", email = "john.doe@acme.com", langKey = "en"
        )
        mailService.sendEmailFromTemplate(
            user, "mail/testEmail", "email.test.title"
        )
        Mockito.verify(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        Assertions.assertThat(message.subject).isEqualTo("test title")
        Assertions.assertThat("${message.allRecipients[0]}").isEqualTo(user.email)
        Assertions.assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
        Assertions.assertThat(message.content.toString()).isEqualToNormalizingNewlines(
            "<html>test title, http://127.0.0.1:8080, john</html>"
        )
        Assertions.assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    fun testSendActivationEmail() {
        val user = AccountCredentials(
            langKey = Constants.DEFAULT_LANGUAGE, login = "john", email = "john.doe@acme.com"
        )
        mailService.sendActivationEmail(user)
        Mockito.verify(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        Assertions.assertThat("${message.allRecipients[0]}").isEqualTo(user.email)
        Assertions.assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
        Assertions.assertThat(message.content.toString()).isNotEmpty
        Assertions.assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    fun testCreationEmail() {
        val user = AccountCredentials(
            langKey = Constants.DEFAULT_LANGUAGE,
            login = "john",
            email = "john.doe@acme.com",
            resetKey = generateResetKey
        )
        mailService.sendCreationEmail(user)
        Mockito.verify(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        Assertions.assertThat("${message.allRecipients[0]}").isEqualTo(user.email)
        Assertions.assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
        Assertions.assertThat(message.content.toString()).isNotEmpty
        Assertions.assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    fun testSendPasswordResetMail() {
        val user = AccountCredentials(
            langKey = Constants.DEFAULT_LANGUAGE, login = "john", email = "john.doe@acme.com"
        )
        mailService.sendPasswordResetMail(user)
        Mockito.verify(javaMailSender).send(messageCaptor.capture())
        val message = messageCaptor.value
        Assertions.assertThat("${message.allRecipients[0]}").isEqualTo(user.email)
        Assertions.assertThat("${message.from[0]}").isEqualTo(properties.mail.from)
        Assertions.assertThat(message.content.toString()).isNotEmpty
        Assertions.assertThat(message.dataHandler.contentType).isEqualTo("text/html;charset=UTF-8")
    }

    @Test
    fun testSendEmailWithException() {
        Mockito.doThrow(MailSendException::class.java).`when`(javaMailSender)
            .send(any(MimeMessage::class.java))
        try {
            mailService.sendEmail(
                "john.doe@acme.com", "testSubject", "testContent", isMultipart = false, isHtml = false
            )
        } catch (e: Exception) {
            Assertions.fail<String>("Exception shouldn't have been thrown")
        }
    }

    val languages = arrayOf(
        "en", "fr", "de", "it", "es"
    )
    val PATTERN_LOCALE_3: Pattern = compile("([a-z]{2})-([a-zA-Z]{4})-([a-z]{2})")
    val PATTERN_LOCALE_2: Pattern = compile("([a-z]{2})-([a-z]{2})")

    @Test
    fun testSendLocalizedEmailForAllSupportedLanguages() {
        val user = AccountCredentials(
            login = "john", email = "john.doe@acme.com"
        )
        for (langKey in languages) {
            mailService.sendEmailFromTemplate(
                user.copy(langKey = langKey), "mail/testEmail", "email.test.title"
            )
            Mockito.verify(javaMailSender, Mockito.atLeastOnce()).send(messageCaptor.capture())
            val message = messageCaptor.value

            val resource = this::class.java.classLoader.getResource(
                "i18n/messages_${
                    getJavaLocale(langKey)
                }.properties"
            )
            assertNotNull(resource)
            val prop = Properties()
            prop.load(
                InputStreamReader(
                    FileInputStream(File(URI(resource.file).path)), Charset.forName("UTF-8")
                )
            )

            val emailTitle = prop["email.test.title"] as String
            Assertions.assertThat(message.subject).isEqualTo(emailTitle)
            Assertions.assertThat(message.content.toString())
                .isEqualToNormalizingNewlines("<html>$emailTitle, http://127.0.0.1:8080, john</html>")
        }
    }

    private fun getJavaLocale(langKey: String): String {
        var javaLangKey = langKey
        val matcher2 = PATTERN_LOCALE_2.matcher(langKey)
        if (matcher2.matches()) javaLangKey = "${
            matcher2.group(1)
        }_${
            matcher2.group(2).uppercase()
        }"
        val matcher3 = PATTERN_LOCALE_3.matcher(langKey)
        if (matcher3.matches()) javaLangKey = "${
            matcher3.group(1)
        }_${
            matcher3.group(2)
        }_${
            matcher3.group(3).uppercase()
        }"
        return javaLangKey
    }
}
/*=================================================================================*/
