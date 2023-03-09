package webapp.entities

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table

/*=================================================================================*/
@Table("`country_phone_code`")
data class CountryPhoneCodeEntity(
    @Id val code: String,
    val countryCode: String
) : Persistable<String> {
    override fun getId() = code
    override fun isNew() = true
}