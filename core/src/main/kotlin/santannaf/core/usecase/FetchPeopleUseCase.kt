package santannaf.core.usecase

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.inject.Named
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import santannaf.core.entity.People
import santannaf.core.provider.FetchCachePeopleProvider
import santannaf.core.provider.FetchPeopleProvider

@Named
@Transactional(propagation = Propagation.NEVER)
class FetchPeopleUseCase(
    private val fetchProvider: FetchPeopleProvider,
    private val fetchCacheProvider: FetchCachePeopleProvider
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val jsonMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    fun fetchPeopleById(id: UUID): Mono<People> {
        return Mono.fromCallable {
            log.info("m=fetchPeopleById, stage=init, i=fetch_people_by_id, id=$id, msg=Fetching people by id")
            id
        }
            .map { id -> "pessoas:id:$id" }
            .flatMap(fetchCacheProvider::fetchCacheByKey)
            .publishOn(Schedulers.boundedElastic())
            .doOnNext { log.info("m=fetchPeopleById, stage=finish, i=fetch_people_by_id, id=$id, msg=People was found at cache") }
            .flatMap { json ->
                Mono.fromCallable<People> {
                    jsonMapper.readValue(json, People::class.java)
                }.subscribeOn(Schedulers.parallel())
            }
            .switchIfEmpty(
                Mono.fromCallable { id }
                    .flatMap(fetchProvider::fetchById)
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext {
                        log.info("m=fetchPeopleById, stage=finish, i=fetch_people_by_id, id=$id, msg=People was found")
                    }
                    .switchIfEmpty(
                        Mono.fromRunnable<People> {
                            log.info("m=fetchPeopleById, stage=finish, id=$id, msg=People is not found")
                        }.then(Mono.empty())
                    )
            )
            .onErrorResume {
                log.error("m=fetchPeopleById, stage=error, e=${it.message}, KClass=${it.javaClass.simpleName}, i=fetch_people_by_id, id=$id, msg=Error from fetch people")
                Mono.error(it)
            }
    }
}
