@file:Suppress("unused")

package webapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.*
import java.util.*


@SpringBootApplication
class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = runApplication<Application>(*args).startupLog
    }
}

