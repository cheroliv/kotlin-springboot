package webapp.entities

import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

/*=================================================================================*/
@Table("`user_authority`")
data class AccountAuthorityEntity(
    @Id var id: Long? = null,
    @field:NotNull
    val userId: UUID,
    @field:NotNull
    val role: String
)