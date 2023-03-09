@file:Suppress("unused")

package webapp

import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import webapp.logging.i
import kotlin.system.exitProcess

@Component
@Profile(CLI)
class Cli : CommandLineRunner {
    companion object  {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Application>(*args) {
                //before loading config
                setAdditionalProfiles(CLI)
                setDefaultProperties(CLI_PROPS)
            }.run {
                //after loading config
            }
            exitProcess(NORMAL_TERMINATION)
        }
    }
    override fun run(vararg args: String?) = runBlocking {
        i("command line interface: $args")
    }
}