package webapp.signup

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import webapp.*
import webapp.accounts.models.Account
import webapp.accounts.models.AccountCredentials
import webapp.accounts.models.AccountUtils.generateActivationKey
import webapp.accounts.repository.AccountRepository
import webapp.mail.MailService
import java.time.Instant.now
import java.util.Locale.ENGLISH

@Service
class SignupService(
    private val accountRepository: AccountRepository,
    private val mailService: MailService,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    suspend fun signup(account: AccountCredentials) = now().run {
        account.copy(
            password = passwordEncoder.encode(account.password),
            activationKey = generateActivationKey,
            authorities = setOf(ROLE_USER),
            langKey = when {
                account.langKey.isNullOrBlank() -> ENGLISH.language
                else -> account.langKey
            },
            activated = false,
            createdBy = SYSTEM_USER,
            createdDate = this,
            lastModifiedBy = SYSTEM_USER,
            lastModifiedDate = this
        ).run {
            accountRepository.signup(this).also {
                if (it != null) mailService.sendActivationEmail(it)
            }
        }
    }


    @Transactional(readOnly = true)
    suspend fun accountByActivationKey(key: String) = accountRepository.findOneByActivationKey(key)

    @Transactional(readOnly = true)
    suspend fun accountById(emailOrLogin: String) = accountRepository.findOne(emailOrLogin)

    @Transactional
    suspend fun saveAccount(account: AccountCredentials) = accountRepository.save(account)

    @Transactional
    suspend fun deleteAccount(account: Account) = accountRepository.delete(account)
}