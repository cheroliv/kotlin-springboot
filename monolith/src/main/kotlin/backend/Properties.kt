package backend

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.web.cors.CorsConfiguration


/*=================================================================================*/

@ConfigurationProperties(prefix = "backend", ignoreUnknownFields = false)
@PropertySources(
    PropertySource(
        value = ["classpath:git.properties"],
        ignoreResourceNotFound = true
    ),
    PropertySource(
        value = ["classpath:META-INF/build-info.properties"],
        ignoreResourceNotFound = true
    )
)
@ConstructorBinding
class ApplicationProperties(
    val message: String = "",
    val item: String,
    val goVisitMessage: String,
    val clientApp: ClientApp = ClientApp(),
    val database: Database = Database(),
    val mailslurp: MailSlurp = MailSlurp(),
    val mail: MailSmtp = MailSmtp(),
    val gmail: GoogleMail = GoogleMail(),
    val http: Http = Http(),
    val cache: Cache = Cache(),
    val security: Security = Security(),
    val cors: CorsConfiguration = CorsConfiguration(),
) {
    class MailSlurp(
        val token: String = ""
    )

    class GoogleMail(val token: String = "")
    class ClientApp(val name: String = "")
    class Database(val populatorPath: String = "")
    class MailSmtp(
        val enabled: Boolean = false,
        val from: String = "",
        val baseUrl: String = "",
        val host: String = "",
        val port: Int = -1,
        val password: String = "",
        val property: Property = Property(),
    ) {
        class Property(
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