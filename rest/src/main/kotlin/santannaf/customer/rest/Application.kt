package santannaf.customer.rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import reactor.core.publisher.Hooks

@SpringBootApplication(scanBasePackages = ["santannaf"])
@EnableR2dbcRepositories
class Application

fun main(args: Array<String>) {
    Hooks.enableAutomaticContextPropagation();
    runApplication<Application>(args = args)
}
