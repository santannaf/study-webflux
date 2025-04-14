package santannaf.core.dataprovider.repository.postgres

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import java.time.LocalDate
import java.util.UUID
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import santannaf.core.entity.People
import santannaf.core.provider.FetchCachePeopleProvider
import santannaf.core.provider.FetchPeopleProvider
import santannaf.core.provider.SavePeopleProvider

@Repository
class PeoplePostgresProvider(
    private val template: R2dbcEntityTemplate,
    private val observationRegistry: ObservationRegistry
) : FetchPeopleProvider, FetchCachePeopleProvider, SavePeopleProvider {

    private fun buildPeople(row: Row, rowMetadata: RowMetadata): People {
        return People(
            id = row["id"] as UUID,
            nickname = row["nickname"] as String,
            name = row["name"] as String,
            birthday = row["birthday"] as LocalDate,
            stack = (row["stack"] as? Array<*>)?.filterIsInstance<String>()?.toTypedArray() ?: emptyArray()
        )
    }

    override fun save(people: People): Mono<People> {
        return template.databaseClient
            .sql("insert into pessoas (id,apelido,nome,nascimento,stack,busca) values (:id,:nickname,:name,:birthday,:stack,:search)")
            .bindValues(
                mapOf(
                    "id" to people.id,
                    "nickname" to people.nickname,
                    "name" to people.name,
                    "birthday" to people.birthday,
                    "stack" to people.stack,
                    "search" to people.search
                )
            )
            .fetch()
            .rowsUpdated()
            .map<People> { people }
    }

    override fun saveInCache(triple: Triple<UUID, String, String>): Mono<UUID> {
        return template.databaseClient.sql("insert into cache (key,data) values (:key,:data::jsonb) on conflict (key) do update set data = :data::jsonb")
            .bind("key", triple.second)
            .bind("data", triple.third)
//            .bind(3, triple.third)
            .fetch()
            .rowsUpdated()
            .map<UUID> { triple.first }
    }

    override fun fetchById(id: UUID): Mono<People> {
        return template.databaseClient.sql("select id, apelido as nickname, nome as name, nascimento as birthday, stack from pessoas where id = :id")
            .bind("id", id)
            .map(::buildPeople)
            .one()
    }

    override fun fetchByIdWithObservability(id: UUID): Mono<People> {
        val query =
            "select id, apelido as nickname, nome as name, nascimento as birthday, stack from pessoas where id = :id"
        return observeMono(
            observationRegistry = observationRegistry,
            query = query,
            highCardinalityTags = mapOf("id" to id.toString())
        ) {
            fetchById(id)
        }
    }

    override fun fetchCacheByKey(key: String): Mono<String> {
        return template.databaseClient.sql("select data from cache where key = :key")
            .bind("key", key)
            .mapValue<String>(String::class.java)
            .one()
    }


    private fun <T> observeMono(
        observationRegistry: ObservationRegistry,
        name: String = "r2dbc.query",
        query: String,
        highCardinalityTags: Map<String, String> = emptyMap(),
        monoSupplier: () -> Mono<T>
    ): Mono<T> {
        return Mono.defer {
            val observation = Observation
                .createNotStarted(name, observationRegistry)
                .apply {
                    highCardinalityTags.forEach { (k, v) -> highCardinalityKeyValue(k, v) }
                    lowCardinalityKeyValue("query", query)
                }

            val scope = observation.openScope()
            observation.start()

            monoSupplier()
                .doOnSuccess { observation.stop(); scope.close() }
                .doOnError { e -> observation.error(e); observation.stop(); scope.close() }
        }
    }
}
