package santannaf.core.provider

import java.util.UUID
import reactor.core.publisher.Mono
import santannaf.core.entity.People

interface SavePeopleProvider {
    fun save(people: People): Mono<People>
    fun saveInCache(triple: Triple<UUID, String, String>): Mono<UUID>
}
