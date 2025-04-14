package santannaf.core.entity

import java.time.LocalDate

interface PeopleAbstractRequest {
    fun fetchNickname(): String
    fun fetchBirthday(): LocalDate
    fun fetchName(): String
    fun fetchStack(): Array<String>?
    fun buildPeople(search: String): People = People(
        nickname = fetchNickname(),
        name = fetchName(),
        birthday = fetchBirthday(),
        stack = fetchStack(),
        search = search
    )

    fun buildSearch(): String = buildString {
        append(fetchNickname())
        append(" ")
        append(fetchName())
        append(" ")
        fetchStack()?.forEach {
            append(it).append(" ")
        }
    }
}
