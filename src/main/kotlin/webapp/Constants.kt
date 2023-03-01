@file:Suppress("unused")

package webapp

import java.net.URI
import java.net.URI.create


object Constants {
    const val EMPTY_STRING = ""
    const val JUMP_LINE = "\n"
    const val VIRGULE = ","
    const val BASE_URL_DEV = "http://localhost:8080"



    //SignupController
    @JvmStatic
    val ALLOWED_ORDERED_PROPERTIES = arrayOf(
        "id",
        "login",
        "firstName",
        "lastName",
        "email",
        "activated",
        "langKey"
    )
    const val NORMAL_TERMINATION = 0

    const val DOMAIN_DEV_URL = "acme.com"
    private const val DOMAIN_URL = "https://cheroliv.github.io"
    const val STARTUP_HOST_WARN_LOG_MSG = "The host name could not be determined, using `localhost` as fallback"
    const val SPRING_APPLICATION_NAME = "spring.application.name"
    const val SERVER_SSL_KEY_STORE = "server.ssl.key-store"
    const val SERVER_PORT = "server.port"
    const val SERVER_SERVLET_CONTEXT_PATH = "server.servlet.context-path"
    const val EMPTY_CONTEXT_PATH = "/"
    const val HTTPS = "https"
    const val HTTP = "http"
    const val PROFILE_SEPARATOR = ","
    val CLI_PROPS = mapOf("spring.main.web-application-type" to "none")
    const val SPRING_PROFILE_CONF_DEFAULT_KEY = "spring.profiles.default"
    const val MSG_WRONG_ACTIVATION_KEY = "No user was found for this activation key"


    //Spring profiles
    const val DEFAULT = "default"
    const val DEVELOPMENT = "dev"
    const val PRODUCTION = "prod"
    const val CLOUD = "cloud"
    const val TEST = "test"
    const val HEROKU = "heroku"
    const val AWS_ECS = "aws-ecs"
    const val AZURE = "azure"
    const val SWAGGER = "swagger"
    const val NO_LIQUIBASE = "no-liquibase"
    const val K8S = "k8s"
    const val CLI = "cli"
    const val GMAIL = "gmail"
    const val MAILSLURP = "mailslurp"

    //Config
    const val DEV_HOST = "localhost"

    //HTTP param
    const val REQUEST_PARAM_LANG = "lang"
    const val CONTENT_SECURITY_POLICY =
        "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:"
    const val FEATURE_POLICY =
        "geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'"

    //Security
    const val ROLE_ADMIN = "ADMIN"
    const val ROLE_USER = "USER"
    const val ROLE_ANONYMOUS = "ANONYMOUS"
    const val AUTHORITIES_KEY = "auth"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val BEARER_START_WITH = "Bearer "
    const val AUTHORIZATION_ID_TOKEN = "id_token"
    const val VALID_TOKEN = true
    const val INVALID_TOKEN = false

    //Email
    const val MAIL_DEBUG = "mail.debug"
    const val MAIL_TRANSPORT_STARTTLS_ENABLE = "mail.smtp.starttls.enable"
    const val MAIL_SMTP_AUTH = "mail.smtp.auth"
    const val MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol"

    //REST API
    //URIs

    const val AUTHORITY_API = "/api/authorities"
    const val ACCOUNT_API = "/api/accounts"
    const val SIGNUP_API = "/signup"
    const val SIGNUP_API_PATH = "$ACCOUNT_API$SIGNUP_API"
    const val ACTIVATE_API = "/activate"
    const val ACTIVATE_API_PATH = "$ACCOUNT_API$ACTIVATE_API?key="
    const val ACTIVATE_API_PARAM = "{activationKey}"
    const val ACTIVATE_API_KEY = "key"
    const val RESET_PASSWORD_API_INIT = "/reset-password/init"
    const val RESET_PASSWORD_API_FINISH = "/reset-password/finish"
    const val CHANGE_PASSWORD_API = "/change-password"

    //properties
    const val PROP_ITEM = "webapp.item"
    const val PROP_MESSAGE = "webapp.message"
    const val PROP_MAIL_BASE_URL = "webapp.mail.base-url"
    const val PROP_MAIL_FROM = "webapp.mail.from"
    const val PROP_MAIL_HOST = "webapp.mail.host"
    const val PROP_MAIL_PORT = "webapp.mail.port"
    const val PROP_MAIL_PASSWORD = "webapp.mail.password"
    const val PROP_MAIL_PROPERTY_DEBUG = "webapp.mail.property.debug"
    const val PROP_MAIL_PROPERTY_TRANSPORT_PROTOCOL = "webapp.mail.property.transport.protocol"
    const val PROP_MAIL_PROPERTY_SMTP_AUTH = "webapp.mail.property.smtp.auth"
    const val PROP_MAIL_PROPERTY_SMTP_STARTTLS_ENABLE = "webapp.mail.property.smtp.starttls.enable"
    const val PROP_DATABASE_POPULATOR_PATH = "webapp.database.populator-path"
    const val STARTUP_LOG_MSG_KEY = "startup.log.msg"


    //Email activation
    const val USER = "user"
    const val ADMIN = "admin"
    const val BASE_URL = "baseUrl"

    const val TEMPLATE_NAME_SIGNUP = "mail/activationEmail"

    const val TITLE_KEY_SIGNUP = "email.activation.title"

    const val TEMPLATE_NAME_CREATION = "mail/creationEmail"

    const val TEMPLATE_NAME_PASSWORD = "mail/passwordResetEmail"

    const val TITLE_KEY_PASSWORD = "email.reset.title"

    // Regex for acceptable logins
    const val LOGIN_REGEX =
        "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$"
    const val SYSTEM_USER = "system"


    @Suppress("SpellCheckingInspection")
    const val ANONYMOUS_USER: String = "anonymoususer"
    const val DEFAULT_LANGUAGE = "en"
    const val PASSWORD_MIN: Int = 4
    const val PASSWORD_MAX: Int = 24
    const val ERR_CONCURRENCY_FAILURE: String = "error.concurrencyFailure"
    const val ERR_VALIDATION: String = "error.validation"
    const val USER_INITIAL_ACTIVATED_VALUE = false
    const val IMAGE_URL_DEFAULT = "http://placehold.it/50x50"
    private const val PROBLEM_BASE_URL: String = "$DOMAIN_URL/problem"


    @JvmField
    val DEFAULT_TYPE: URI = create("$PROBLEM_BASE_URL/problem-with-message")

    @JvmField
    val CONSTRAINT_VIOLATION_TYPE: URI = create("$PROBLEM_BASE_URL/constraint-violation")

    @JvmField
    val INVALID_PASSWORD_TYPE: URI = create("$PROBLEM_BASE_URL/invalid-password")

    @JvmField
    val EMAIL_ALREADY_USED_TYPE: URI = create("$PROBLEM_BASE_URL/email-already-used")

    @JvmField
    val LOGIN_ALREADY_USED_TYPE: URI = create("$PROBLEM_BASE_URL/login-already-used")
}