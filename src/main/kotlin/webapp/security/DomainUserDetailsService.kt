package webapp.security

import jakarta.validation.Validator
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import webapp.*
import webapp.entities.AccountRecord.Companion.EMAIL_FIELD
import webapp.exceptions.UserNotActivatedException
import webapp.models.AccountCredentials
import webapp.repository.AccountRepository

@Suppress("unused")
@Component("userDetailsService")
class DomainUserDetailsService(
    private val accountRepository: AccountRepository,
    private val validator: Validator,
) : ReactiveUserDetailsService {

    @Transactional
    override fun findByUsername(emailOrLogin: String): Mono<UserDetails> =
        d("Authenticating $emailOrLogin").run {
            return if (validator.validateProperty(AccountCredentials(email = emailOrLogin), EMAIL_FIELD).isEmpty()) mono {
                accountRepository.findOneWithAuthorities(emailOrLogin).apply {
                    if (this == null) throw UsernameNotFoundException("User with email $emailOrLogin was not found in the database")
                }
            }.map { createSpringSecurityUser(emailOrLogin, it) }
            else mono {
                accountRepository.findOneWithAuthorities(emailOrLogin).apply {
                    if (this == null) throw UsernameNotFoundException("User $emailOrLogin was not found in the database")
                }
            }.map { createSpringSecurityUser(emailOrLogin, it) }
        }


    private fun createSpringSecurityUser(
        lowercaseLogin: String,
        account: AccountCredentials
    ) = when {
        !account.activated -> throw UserNotActivatedException("User $lowercaseLogin was not activated")
        else -> User(
            account.login!!,
            account.password!!,
            account.authorities!!.map { SimpleGrantedAuthority(it) }
        )
    }
}