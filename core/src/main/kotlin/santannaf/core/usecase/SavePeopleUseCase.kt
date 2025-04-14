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
import santannaf.core.entity.PeopleAbstractRequest
import santannaf.core.provider.SavePeopleProvider

@Named
@Transactional(propagation = Propagation.NEVER)
class SavePeopleUseCase(
    private val saveProvider: SavePeopleProvider
) {
    private val log = LoggerFactory.getLogger(SavePeopleUseCase::class.java)
    private val jsonMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    fun savePeople(request: PeopleAbstractRequest): Mono<UUID> {
        return Mono.fromCallable(request::buildSearch)
            .map(request::buildPeople)
            .flatMap(saveProvider::save)
            .publishOn(Schedulers.boundedElastic())
            .doOnNext { log.info("m=savePeople, stage=finish, i=save_people, msg=People was save") }
            .flatMap { saved ->
                Mono.fromCallable<Triple<UUID, String, String>> {
                    Triple(saved.id, "pessoas:id:${saved.id}", jsonMapper.writeValueAsString(saved))
                }.subscribeOn(Schedulers.parallel())
            }
            .publishOn(Schedulers.boundedElastic())
            .flatMap(saveProvider::saveInCache)
            .publishOn(Schedulers.boundedElastic())
            .doOnSuccess { log.info("m=savePeople, stage=finish, i=save_people_in_cache, msg=People was save in cache") }
            .onErrorResume {
                log.error("m=savePeople, stage=error, e=${it.message}, KClass=${it.javaClass.simpleName}, i=save_people, msg=Error from save people")
                Mono.error(it)
            }
    }
}
