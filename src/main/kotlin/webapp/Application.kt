@file:Suppress("unused")

package webapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.*
import webapp.Logging.bootstrapLog
import webapp.Logging.checkProfileLog
import webapp.Logging.`continue`
import java.util.*


@SpringBootApplication
class Application

fun main(args: Array<String>) = runApplication<Application>(*args)
    .checkProfileLog()
    .bootstrapLog()
    .`continue`()