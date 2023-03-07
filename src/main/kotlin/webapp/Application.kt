@file:Suppress("unused")

package webapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.web.reactive.config.WebFluxConfigurer
import webapp.Logging.bootstrapLog
import webapp.Logging.checkProfileLog
import webapp.Logging.`continue`
import java.util.*


@SpringBootApplication
class Application(
    private val properties: Properties,
    private val context: ApplicationContext
) : WebFluxConfigurer {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = runApplication<Application>(*args)
            .checkProfileLog()
            .bootstrapLog()
            .`continue`()
    }
}

