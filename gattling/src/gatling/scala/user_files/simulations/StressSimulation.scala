package user_files.simulations

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class StressSimulation extends Simulation {
    private val protocol = http.baseUrl("http://localhost:8080").userAgentHeader("Stress Test")
    private val searchPeople = scenario("Busca de Pessoas")
            .feed(tsv("rinha_public_pessoas.tsv").circular())
            .exec(http("busca válida").get("/pessoas/#{id}"))

    setUp(
        searchPeople.inject(
            constantUsersPerSec(2).during(25.seconds),
            rampUsersPerSec(6).to(1000).during(3.minutes) // Are you ready ???
        )
    ).protocols(protocol)
}
