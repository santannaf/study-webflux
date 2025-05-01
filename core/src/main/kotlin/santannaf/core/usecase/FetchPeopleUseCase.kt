package santannaf.core.usecase

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.inject.Named
import java.util.UUID
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import santannaf.core.entity.People
import santannaf.core.provider.FetchCachePeopleProvider
import santannaf.core.provider.FetchPeopleProvider
import santannaf.core.provider.SavePeopleProvider

@Named
@Transactional(propagation = Propagation.NEVER)
class FetchPeopleUseCase(
    private val fetchProvider: FetchPeopleProvider,
    private val fetchCacheProvider: FetchCachePeopleProvider,
    private val saveProvider: SavePeopleProvider
) {
    private val jsonMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    fun fetchPeopleById(id: UUID): Mono<People> {
        return Mono.fromCallable { id }
            .map { id -> "pessoas:id:$id" }
            .flatMap(fetchCacheProvider::fetchCacheByKey)
            .flatMap { json ->
                Mono.fromCallable<People> {
                    jsonMapper.readValue(json, People::class.java)
                }.subscribeOn(Schedulers.parallel())
            }
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty(
                Mono.fromCallable { id }
                    .flatMap(fetchProvider::fetchById)
                    .switchIfEmpty(
                        Mono.fromRunnable<People> { }.then(Mono.empty())
                    )
            )
            .onErrorResume {
                Mono.error(it)
            }
    }

    fun fetchPeopleByTerm(term: String): Flux<String?> {
        return Mono.fromCallable { "cache:findByTerm:$term" }
            .flatMapMany(fetchCacheProvider::fetchCacheByKey)
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty(
                Mono.fromCallable { "%$term%" }
                    .flatMapMany(fetchProvider::fetchByTerm)
                    .switchIfEmpty(Mono.just(1))
                    .publishOn(Schedulers.parallel())
                    .map {
                        Triple(
                            UUID.randomUUID(),
                            "cache:findByTerm:$term",
                            jsonMapper.writeValueAsString(it)
                        )
                    }
                    .publishOn(Schedulers.boundedElastic())
                    .flatMap(saveProvider::saveInCache)
                    .map { it.toString() }
            )
            .onErrorResume {
                Mono.error(it)
            }
    }
}
