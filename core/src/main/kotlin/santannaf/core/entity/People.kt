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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as People

        if (id != other.id) return false
        if (nickname != other.nickname) return false
        if (name != other.name) return false
        if (birthday != other.birthday) return false
        if (!stack.contentEquals(other.stack)) return false
        if (search != other.search) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + nickname.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + birthday.hashCode()
        result = 31 * result + (stack?.contentHashCode() ?: 0)
        result = 31 * result + search.hashCode()
        return result
    }
}
