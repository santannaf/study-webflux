package santannaf.core.provider

import santannaf.core.entity.People
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FetchPeopleProvider {
    fun fetchById(id: UUID): Mono<People>
    fun fetchByTerm(q: String): Flux<Int?>
    fun fetchByIdWithObservability(id: UUID): Mono<People>
}
