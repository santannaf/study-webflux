package santannaf.customer.rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactor.core.publisher.Hooks
import santannaf.demo.analyse.queries.analysequeries.annotation.EnableQueryAnalysis

@SpringBootApplication(scanBasePackages = ["santannaf"])
@EnableQueryAnalysis(appName = "customer-webflux")
class Application

fun main(args: Array<String>) {
    Hooks.enableAutomaticContextPropagation();
    runApplication<Application>(args = args)
}
