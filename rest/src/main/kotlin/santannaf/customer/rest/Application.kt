package santannaf.customer.rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["santannaf"])
@EnableR2dbcRepositories
class Application

fun main(args: Array<String>) {
    runApplication<Application>(args = args)
}
