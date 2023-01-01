package backend

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.web.cors.CorsConfiguration


/*=================================================================================*/

@ConfigurationProperties(prefix = "backend", ignoreUnknownFields = false)
@PropertySources(
    PropertySource("classpath:git.properties", ignoreResourceNotFound = true),
    PropertySource("classpath:META-INF/build-info.properties", ignoreResourceNotFound = true)
)
class ApplicationProperties @ConstructorBinding constructor(
    val message: String = "",
    val item: String,
    val goVisitMessage: String,
    val clientApp: ClientApp = ClientApp(),
    val database: Database = Database(),
    val mail: Mail = Mail(),
    val mailbox: MailBox = MailBox(),
    val http: Http = Http(),
    val cache: Cache = Cache(),
    val security: Security = Security(),
    val cors: CorsConfiguration = CorsConfiguration(),
) {
    class MailBox(
        val signup: Mail = Mail(),
        val password: Mail = Mail(),
        val contact: Mail = Mail(),
        val test: Mail = Mail(),
    )
    class ClientApp(val name: String = "")
    class Database(val populatorPath: String = "")
    class Mail(
        val name: String = "",
        val token: String = "",
        val enabled: Boolean = false,
        val from: String = "",
        val baseUrl: String = "",
        val host: String = "",
        val port: Int = -1,
        val password: String = "",
        val property: SmtpProperty = SmtpProperty(),
    ) {
        class SmtpProperty(
            val debug: Boolean = false,
            val transport: Transport = Transport(),
            val smtp: Smtp = Smtp()
        ) {
            class Transport(val protocol: String = "")
            class Smtp(
                val auth: Boolean = false,
                val starttls: Starttls = Starttls()
            ) {
                class Starttls(val enable: Boolean = false)
            }
        }
    }

    class Http(val cache: Cache = Cache()) {
        class Cache(val timeToLiveInDays: Int = 1461)
    }

    class Cache(val ehcache: Ehcache = Ehcache()) {
        class Ehcache(
            val timeToLiveSeconds: Int = 3600,
            val maxEntries: Long = 100
        )
    }

    class Security(
        val rememberMe: RememberMe = RememberMe(),
        val authentication: Authentication = Authentication(),
        val clientAuthorization: ClientAuthorization = ClientAuthorization()
    ) {
        class RememberMe(var key: String = "")

        class Authentication(val jwt: Jwt = Jwt()) {
            class Jwt(
                val tokenValidityInSecondsForRememberMe: Long = 2592000,
                val tokenValidityInSeconds: Long = 1800,
                var base64Secret: String = "",
                var secret: String = ""
            )
        }

        class ClientAuthorization(
            var accessTokenUri: String = "",
            var tokenServiceId: String = "",
            var clientId: String = "",
            var clientSecret: String = ""
        )
    }
}