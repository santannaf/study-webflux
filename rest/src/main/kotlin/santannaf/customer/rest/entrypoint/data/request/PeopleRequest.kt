package santannaf.customer.rest.entrypoint.data.request

import java.time.LocalDate
import santannaf.core.entity.PeopleAbstractRequest

data class PeopleRequest(
    val apelido: String,
    val nome: String,
    val nascimento: LocalDate,
    val stack: List<String>? = null
) : PeopleAbstractRequest {

    init {
        require(apelido.length in 0..32 && apelido.toDoubleOrNull() == null) { "Apelido length must be between 0 and 32 or type invalid" }
        require(nome.length in 0..100 && nome.toDoubleOrNull() == null) { "Nome length must be between 0 and 100 or type invalid" }
        stack?.forEach {
            require(it.length in 0..32 && it.toDoubleOrNull() == null) { "Stack length must be between 0 and 32 or type invalid" }
        }
    }

    override fun fetchNickname(): String = apelido
    override fun fetchBirthday(): LocalDate = nascimento
    override fun fetchName(): String = nome
    override fun fetchStack(): Array<String>? = stack?.toTypedArray()
}
