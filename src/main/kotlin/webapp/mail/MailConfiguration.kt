package webapp.mail

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import webapp.Constants
import webapp.ApplicationProperties

@Configuration
class MailConfiguration(private val properties:ApplicationProperties) {
    @Bean
    @Profile("!${Constants.MAILSLURP} & !${Constants.GMAIL}")
    fun javaMailSender(): JavaMailSender = JavaMailSenderImpl().apply {
        host = properties.mail.host
        port = properties.mail.port
        username = properties.mail.from
        password = properties.mail.password
        mapOf(
            Constants.MAIL_TRANSPORT_PROTOCOL to properties.mail.property.transport.protocol,
            Constants.MAIL_SMTP_AUTH to properties.mail.property.smtp.auth,
            Constants.MAIL_TRANSPORT_STARTTLS_ENABLE to properties.mail.property.smtp.starttls.enable,
            Constants.MAIL_DEBUG to properties.mail.property.debug,
            "spring.mail.test-connection" to true,
            "mail.smtp.ssl.trust" to true,
            "mail.connect_timeout" to 60000,
            "mail.auth_api_key" to "",
        ).forEach { javaMailProperties[it.key] = it.value }
    }
}