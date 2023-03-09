package webapp.entities

import jakarta.validation.constraints.Email
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table

/*=================================================================================*/
@Table("`email`")
data class EmailEntity(@Id val value: @Email String) : Persistable<String> {
    override fun getId() = value
    override fun isNew() = true
}