package webapp


import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import java.net.InetAddress.getLocalHost
import java.net.UnknownHostException
import java.util.*
import java.util.Locale.getDefault


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
@Suppress("UnusedReceiverParameter")
fun ApplicationContext.`continue`() = Unit

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
    if (profiles.isNotBlank()) JUMP_LINE + buildString {
        append("Profile(s): ")
        append(profiles)
    } else EMPTY_STRING
}${
    if (activeProfiles.isNotBlank()) JUMP_LINE + buildString {
        append("Active(s) profile(s): ")
        append(activeProfiles)
    } else EMPTY_STRING
}
----------------------------------------------------------
$JUMP_LINE$JUMP_LINE""".trimIndent()


/*=================================================================================*/

internal fun ApplicationContext.checkProfileLog() = apply {
    environment.activeProfiles.run {
        if (contains(DEVELOPMENT) && contains(PRODUCTION))
            e(
                getBean<MessageSource>().getMessage(
                    STARTUP_LOG_MSG_KEY,
                    arrayOf(DEVELOPMENT, PRODUCTION),
                    getDefault()
                )
            )
        if (contains(DEVELOPMENT) && contains(CLOUD))
            e(
                getBean<MessageSource>().getMessage(
                    STARTUP_LOG_MSG_KEY,
                    arrayOf(DEVELOPMENT, CLOUD),
                    getDefault()
                )
            )
    }
}

/*=================================================================================*/

internal fun ApplicationContext.bootstrapLog() = apply {
    i(startupLogMessage(
        appName = environment.getProperty(SPRING_APPLICATION_NAME),
        goVisitMessage = getBean<Properties>().goVisitMessage,
        protocol = if (environment.getProperty(SERVER_SSL_KEY_STORE) != null) HTTPS
        else HTTP,
        serverPort = environment.getProperty(SERVER_PORT),
        contextPath = environment.getProperty(SERVER_SERVLET_CONTEXT_PATH)
            ?: EMPTY_CONTEXT_PATH,
        hostAddress = try {
            getLocalHost().hostAddress
        } catch (e: UnknownHostException) {
            w(STARTUP_HOST_WARN_LOG_MSG)
            DEV_HOST
        },
        profiles = if (environment.defaultProfiles.isNotEmpty())
            environment
                .defaultProfiles
                .reduce { accumulator, profile -> "$accumulator, $profile" }
        else EMPTY_STRING,
        activeProfiles = if (environment.activeProfiles.isNotEmpty())
            environment
                .activeProfiles
                .reduce { accumulator, profile -> "$accumulator, $profile" }
        else EMPTY_STRING,
    ))
}

/*=================================================================================*/
