package webapp.entities

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/*=================================================================================*/
@Table("`authority`")
data class AuthorityEntity(
    @Id
    @field:NotNull
    @field:Size(max = 50)
    @Column(ROLE_FIELD)
    override val role: String
) : AuthorityRecord, Persistable<String> {
    override fun getId() = role
    override fun isNew() = true

    companion object {
        const val ROLE_FIELD = "role"
    }
}