package santannaf.customer.rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Hooks

@SpringBootApplication(scanBasePackages = ["santannaf"])
class Application

fun main(args: Array<String>) {
    Hooks.enableAutomaticContextPropagation();
    runApplication<Application>(args = args)
}
