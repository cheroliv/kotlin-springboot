package webapp.entities

/*=================================================================================*/
interface AuthorityRecord  {
    val role: String
    companion object {
        const val ROLE_FIELD = "role"
    }
}