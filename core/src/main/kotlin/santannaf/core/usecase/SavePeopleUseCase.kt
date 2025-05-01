package santannaf.core.usecase

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.inject.Named
import java.util.UUID
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
    private val jsonMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    fun savePeople(request: PeopleAbstractRequest): Mono<UUID> {
        return Mono.fromCallable(request::buildSearch)
            .map(request::buildPeople)
            .flatMap(saveProvider::save)
            .flatMap { saved ->
                Mono.fromCallable {
                    Triple(saved.id, "pessoas:id:${saved.id}", jsonMapper.writeValueAsString(saved))
                }.subscribeOn(Schedulers.parallel())
            }
            .publishOn(Schedulers.boundedElastic())
            .flatMap(saveProvider::saveInCache)
            .onErrorResume { Mono.error(it) }
    }
}
