package webapp.logging


import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import webapp.*
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Locale.getDefault

/*=================================================================================*/
private val log: Logger by lazy { getLogger(Application::class.java) }

fun i(message: String) = log.info(message)
fun d(message: String) = log.debug(message)
fun w(message: String) = log.warn(message)
fun t(message: String) = log.trace(message)
fun e(message: String) = log.error(message)
fun e(message: String, defaultMessage: String?) = log.error(message, defaultMessage)
fun e(message: String, e: Exception?) = log.error(message, e)
fun w(message: String, e: Exception?) = log.warn(message, e)
/*=================================================================================*/
internal val ApplicationContext.startupLog
    get() = logProfiles.run {
        i(startupLogMessage(
            appName = environment.getProperty(SPRING_APPLICATION_NAME),
            goVisitMessage = getBean<Properties>().goVisitMessage,
            protocol = if (environment.getProperty(SERVER_SSL_KEY_STORE) != null) HTTPS
            else HTTP,
            serverPort = environment.getProperty(SERVER_PORT),
            contextPath = environment.getProperty(SERVER_SERVLET_CONTEXT_PATH)
                ?: EMPTY_CONTEXT_PATH,
            hostAddress = try {
                InetAddress.getLocalHost().hostAddress
            } catch (e: UnknownHostException) {
                w(STARTUP_HOST_WARN_LOG_MSG)
                DEV_HOST
            },
            profiles = when {
                environment.defaultProfiles.isNotEmpty() -> environment
                    .defaultProfiles
                    .reduce { accumulator, profile -> "$accumulator, $profile" }
                else -> EMPTY_STRING
            },
            activeProfiles = when {
                environment.activeProfiles.isNotEmpty() -> environment
                    .activeProfiles
                    .reduce { accumulator, profile -> "$accumulator, $profile" }
                else -> EMPTY_STRING
            },
        ))
    }
/*=================================================================================*/
private fun startupLogMessage(
    appName: String?,
    goVisitMessage: String,
    protocol: String,
    serverPort: String?,
    contextPath: String,
    hostAddress: String,
    profiles: String,
    activeProfiles: String
): String = """$JUMP_LINE$JUMP_LINE$JUMP_LINE
----------------------------------------------------------
Go visit $goVisitMessage    
----------------------------------------------------------
Application '$appName' is running!
Access URLs
    Local:      $protocol://localhost:$serverPort$contextPath
    External:   $protocol://$hostAddress:$serverPort$contextPath${
    when {
        profiles.isNotBlank() -> JUMP_LINE + buildString {
            append("Profile(s): ")
            append(profiles)
        }
        else -> EMPTY_STRING
    }
}${
    when {
        activeProfiles.isNotBlank() -> JUMP_LINE + buildString {
            append("Active(s) profile(s): ")
            append(activeProfiles)
        }
        else -> EMPTY_STRING
    }
}
----------------------------------------------------------
$JUMP_LINE$JUMP_LINE""".trimIndent()
/*=================================================================================*/
private val ApplicationContext.logProfiles: ApplicationContext
    get() = apply {
        environment.activeProfiles.run {
            when {
                contains(DEVELOPMENT) && contains(PRODUCTION) -> e(
                    getBean<MessageSource>().getMessage(
                        STARTUP_LOG_MSG_KEY,
                        arrayOf(DEVELOPMENT, PRODUCTION),
                        getDefault()
                    )
                )

                contains(DEVELOPMENT) && contains(CLOUD) -> e(
                    getBean<MessageSource>().getMessage(
                        STARTUP_LOG_MSG_KEY,
                        arrayOf(DEVELOPMENT, CLOUD),
                        getDefault()
                    )
                )
            }
        }
    }
/*=================================================================================*/