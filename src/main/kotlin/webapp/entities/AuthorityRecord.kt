package webapp.entities

import org.springframework.data.domain.Persistable

/*=================================================================================*/
interface AuthorityRecord : Persistable<String> {
    val role: String
    override fun getId() = role
    override fun isNew() = true

    companion object {
        const val ROLE_FIELD = "role"
    }
}