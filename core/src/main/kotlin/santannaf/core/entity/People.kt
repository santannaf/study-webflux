package santannaf.core.entity

import java.time.LocalDate
import java.util.UUID

data class People(
    val id: UUID = UUID.randomUUID(),
    val nickname: String,
    val name: String,
    val birthday: LocalDate,
    val stack: Array<String>?,
    val search: String = ""
)
