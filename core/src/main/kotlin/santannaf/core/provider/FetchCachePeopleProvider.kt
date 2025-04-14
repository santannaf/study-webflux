package santannaf.core.provider

import reactor.core.publisher.Mono

interface FetchCachePeopleProvider {
    fun fetchCacheByKey(key: String): Mono<String>
}
