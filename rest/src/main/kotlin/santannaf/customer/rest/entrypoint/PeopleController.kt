package santannaf.customer.rest.entrypoint

import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import santannaf.core.usecase.FetchPeopleUseCase
import santannaf.core.usecase.SavePeopleUseCase
import santannaf.customer.rest.entrypoint.data.request.PeopleRequest

@RestController
class PeopleController(
    private val fetchUseCase: FetchPeopleUseCase,
    private val saveUseCase: SavePeopleUseCase
) {
    @PostMapping(path = ["/pessoas"])
    @ResponseStatus(HttpStatus.CREATED)
    fun createPeople(
        @RequestBody request: PeopleRequest
    ): Mono<ResponseEntity<*>> {
        return Mono.fromCallable { request }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(saveUseCase::savePeople)
            .map {
                ResponseEntity.created(
                    UriComponentsBuilder
                        .fromPath("/pessoas/{id}")
                        .buildAndExpand(it)
                        .toUri()
                ).build<Void>()
            }
    }

    @GetMapping(path = ["/pessoas/{id}"])
    fun fetchPeopleById(@PathVariable id: UUID): Mono<ResponseEntity<*>> {
        return Mono.fromCallable { id }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(fetchUseCase::fetchPeopleById)
            .map { ResponseEntity.ok().body(it) }
    }

    @GetMapping(path = ["/pessoas"])
    fun fetchPeopleByTerm(@RequestParam t: String): ResponseEntity<Flux<*>> {
        val result = fetchUseCase.fetchPeopleByTerm(t).subscribeOn(Schedulers.boundedElastic())
        return ResponseEntity.ok().body(result)
    }
}
