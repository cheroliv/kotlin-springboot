@file:Suppress("unused")

package backend.accounts.management


import backend.Constants.AUTHORITY_API
import backend.Log.log
import backend.accounts.AccountCredentials
import backend.accounts.AccountRepository
import backend.accounts.AuthorityRepository
import backend.accounts.UserNotActivatedException
import kotlinx.coroutines.reactor.mono
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus.OK
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

/*=================================================================================*/

@Component("userDetailsService")
class DomainUserDetailsService(
    private val accountRepository: AccountRepository
) : ReactiveUserDetailsService {

    @Transactional
    override fun findByUsername(login: String): Mono<UserDetails> = log
        .debug("Authenticating $login").run {
            return if (EmailValidator().isValid(login, null)) mono {
                accountRepository.findOneByEmailWithAuthorities(login).apply {
                    if (this == null) throw UsernameNotFoundException(
                        "User with email $login was not found in the database"
                    )
                }
            }.map { createSpringSecurityUser(login, it) }
            else mono {
                accountRepository.findOneByLoginWithAuthorities(login).apply {
                    if (this == null) throw UsernameNotFoundException(
                        "User $login was not found in the database"
                    )
                }
            }.map { createSpringSecurityUser(login, it) }
        }


    private fun createSpringSecurityUser(
        lowercaseLogin: String,
        account: AccountCredentials
    ): User = if (!account.activated)
        throw UserNotActivatedException("User $lowercaseLogin was not activated")
    else User(
        account.login!!,
        account.password!!,
        account.authorities!!.map {
            SimpleGrantedAuthority(it)
        }
    )
}

/*=================================================================================*/
@RestController
@RequestMapping(AUTHORITY_API)
class AuthorityController(
    private val authorityRepository: AuthorityRepository
) {
    @GetMapping
    @ResponseStatus(OK)
    suspend fun getAuthorities() = authorityRepository.findAll()

    @GetMapping("count")
    @ResponseStatus(OK)
    suspend fun count() = authorityRepository.count()
}


//@RestController
//@RequestMapping("api")
//class AccountController(
//    private val accountService: AccountService
//) {
//    internal class AccountException(message: String) : RuntimeException(message)
//
////
////    /**
////     * `GET  /authenticate` : check if the user is authenticated, and return its login.
////     *
////     * @param request the HTTP request.
////     * @return the login if the user is authenticated.
////     */
////    @GetMapping("/authenticate")
////    suspend fun isAuthenticated(request: ServerWebExchange): String? =
////        request.getPrincipal<Principal>().map(Principal::getName).awaitFirstOrNull().also {
////            log.debug("REST request to check if the current user is authenticated")
////        }
////
////
////    /**
////     * {@code GET  /account} : get the current user.
////     *
////     * @return the current user.
////     * @throws RuntimeException {@code 500 (Internal BackendApplication Error)} if the user couldn't be returned.
////     */
////    @GetMapping("account")
////    suspend fun getAccount(): Account = log.info("controller getAccount").run {
////        userService.getUserWithAuthorities().run<User?, Nothing> {
////            if (this == null) throw AccountException("User could not be found")
////            else return Account(user = this)
////        }
////    }
////
////    /**
////     * {@code POST  /account} : update the current user information.
////     *
////     * @param account the current user information.
////     * @throws EmailAlreadyUsedProblem {@code 400 (Bad Request)} if the email is already used.
////     * @throws RuntimeException          {@code 500 (Internal BackendApplication Error)} if the user login wasn't found.
////     */
////    @PostMapping("account")
////    suspend fun saveAccount(@Valid @RequestBody account: Account): Unit {
////        getCurrentUserLogin().apply principal@{
////            if (isBlank()) throw AccountException("Current user login not found")
////            else {
////                userService.findAccountByEmail(account.email!!).apply {
////                    if (!this?.login?.equals(this@principal, true)!!)
////                        throw EmailAlreadyUsedException()
////                }
////                userService.findAccountByLogin(account.login!!).apply {
////                    if (this == null)
////                        throw AccountException("User could not be found")
////                }
////                userService.updateUser(
////                    account.firstName,
////                    account.lastName,
////                    account.email,
////                    account.langKey,
////                    account.imageUrl
////                )
////            }
////        }
////    }
////
////    /**
////     * {@code POST  /account/change-password} : changes the current user's password.
////     *
////     * @param passwordChange current and new password.
////     * @throws InvalidPasswordProblem {@code 400 (Bad Request)} if the new password is incorrect.
////     */
////    @PostMapping("account/change-password")
////    suspend fun changePassword(@RequestBody passwordChange: PasswordChange): Unit =
////        passwordChange.run {
////            InvalidPasswordException().apply { if (isPasswordLengthInvalid(newPassword)) throw this }
////            if (currentPassword != null && newPassword != null)
////                userService.changePassword(currentPassword, newPassword)
////        }
////
////    /**
////     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
////     *
////     * @param mail the mail of the user.
////     */
////    @PostMapping("account/reset-password/init")
////    suspend fun requestPasswordReset(@RequestBody mail: String): Unit =
////        userService.requestPasswordReset(mail).run {
////            if (this == null) log.warn("Password reset requested for non existing mail")
////            else mailService.sendPasswordResetMail(this)
////        }
////
////    /**
////     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
////     *
////     * @param keyAndPassword the generated key and the new password.
////     * @throws InvalidPasswordProblem {@code 400 (Bad Request)} if the password is incorrect.
////     * @throws RuntimeException         {@code 500 (Internal BackendApplication Error)} if the password could not be reset.
////     */
////    @PostMapping("account/reset-password/finish")
////    suspend fun finishPasswordReset(@RequestBody keyAndPassword: KeyAndPassword): Unit {
////        keyAndPassword.run {
////            InvalidPasswordException().apply { if (isPasswordLengthInvalid(newPassword)) throw this }
////            if (newPassword != null && key != null)
////                if (userService.completePasswordReset(newPassword, key) == null)
////                    throw AccountException("No user was found for this reset key")
////        }
////    }
//}

/*=================================================================================*/


/**
 * REST controller for managing users.
 * <p>
 * This class accesses the {@link User} entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
//@RestController
//@RequestMapping("api/admin")
//class AccountController(
//    private val userService: UserService,
//    private val mailService: MailService,
//    private val properties: ApplicationProperties
//) {
//    companion object {
//        private val ALLOWED_ORDERED_PROPERTIES =
//            arrayOf(
//                "id",
//                "login",
//                "firstName",
//                "lastName",
//                "email",
//                "activated",
//                "langKey"
//            )
//    }
//
//    /**
//     * {@code POST  /admin/users}  : Creates a new user.
//     * <p>
//     * Creates a new user if the login and email are not already used, and sends an
//     * mail with an activation link.
//     * The user needs to be activated on creation.
//     *
//     * @param account the user to create.
//     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user,
//     * or with status {@code 400 (Bad Request)} if the login or email is already in use.
//     * @throws AlertProblem {@code 400 (Bad Request)} if the login or email is already in use.
//     */
//    @PostMapping("users")
//    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(\"$ROLE_ADMIN\")")
//    suspend fun createUser(@Valid @RequestBody account: Account): ResponseEntity<User> {
//        account.apply requestAccount@{
//            log.debug("REST request to save User : {}", account)
//            if (id != null) throw AlertProblem(
//                defaultMessage = "A new user cannot already have an ID",
//                entityName = "userManagement",
//                errorKey = "idexists"
//            )
//            userService.findAccountByLogin(login!!).apply retrieved@{
//                if (this@retrieved?.login?.equals(
//                        this@requestAccount.login,
//                        true
//                    ) == true
//                ) throw LoginAlreadyUsedProblem()
//            }
//            userService.findAccountByEmail(email!!).apply retrieved@{
//                if (this@retrieved?.email?.equals(
//                        this@requestAccount.email,
//                        true
//                    ) == true
//                ) throw EmailAlreadyUsedProblem()
//            }
//            userService.createUser(this).apply {
//                mailService.sendActivationEmail(this)
//                try {
//                    return created(URI("/api/admin/users/$login"))
//                        .headers(
//                            createAlert(
//                                properties.clientApp.name,
//                                "userManagement.created",
//                                login
//                            )
//                        ).body(this)
//                } catch (e: URISyntaxException) {
//                    throw RuntimeException(e)
//                }
//            }
//        }
//    }
//
//    /**
//     * {@code PUT /admin/users} : Updates an existing User.
//     *
//     * @param account the user to update.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user.
//     * @throws EmailAlreadyUsedProblem {@code 400 (Bad Request)} if the email is already in use.
//     * @throws LoginAlreadyUsedProblem {@code 400 (Bad Request)} if the login is already in use.
//     */
//    @PutMapping("/users")
//    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(\"$ROLE_ADMIN\")")
//    suspend fun updateUser(@Valid @RequestBody account: Account): ResponseEntity<Account> {
//        log.debug("REST request to update User : {}", account)
//        userService.findAccountByEmail(account.email!!).apply {
//            if (this == null) throw ResponseStatusException(NOT_FOUND)
//            if (id != account.id) throw EmailAlreadyUsedProblem()
//        }
//        userService.findAccountByLogin(account.login!!).apply {
//            if (this == null) throw ResponseStatusException(NOT_FOUND)
//            if (id != account.id) throw LoginAlreadyUsedProblem()
//        }
//        return ok()
//            .headers(
//                createAlert(
//                    properties.clientApp.name,
//                    "userManagement.updated",
//                    account.login
//                )
//            ).body(userService.updateUser(account))
//    }
//
//    /**
//     * {@code GET /admin/users} : get all users with all the details -
//     * calling this are only allowed for the administrators.
//     *
//     * @param request a {@link ServerHttpRequest} request.
//     * @param pageable the pagination information.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
//     */
//    @GetMapping("/users")
//    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(\"$ROLE_ADMIN\")")
//    suspend fun getAllUsers(request: ServerHttpRequest, pageable: Pageable): ResponseEntity<Flow<Account>> =
//        log.debug("REST request to get all User for an admin").run {
//            return if (!onlyContainsAllowedProperties(pageable)) {
//                badRequest().build()
//            } else ok()
//                .headers(
//                    generatePaginationHttpHeaders(
//                        fromHttpRequest(request),
//                        PageImpl<Account>(
//                            mutableListOf(),
//                            pageable,
//                            userService.countUsers()
//                        )
//                    )
//                ).body(userService.getAllManagedUsers(pageable))
//        }
//
//
//    private fun onlyContainsAllowedProperties(pageable: Pageable): Boolean = pageable
//        .sort
//        .stream()
//        .map(Order::getProperty)
//        .allMatch(ALLOWED_ORDERED_PROPERTIES::contains)
//
//
//    /**
//     * {@code GET /admin/users/:login} : get the "login" user.
//     *
//     * @param login the login of the user to find.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)}
//     * and with body the "login" user, or with status {@code 404 (Not Found)}.
//     */
//    @GetMapping("/users/{login}")
//    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(\"$ROLE_ADMIN\")")
//    suspend fun getUser(@PathVariable login: String): Account =
//        log.debug("REST request to get User : {}", login).run {
//            return Account(userService.getUserWithAuthoritiesByLogin(login).apply {
//                if (this == null) throw ResponseStatusException(NOT_FOUND)
//            }!!)
//        }
//
//    /**
//     * {@code DELETE /admin/users/:login} : delete the "login" User.
//     *
//     * @param login the login of the user to delete.
//     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
//     */
//    @DeleteMapping("/users/{login}")
//    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(\"$ROLE_ADMIN\")")
//    @ResponseStatus(code = NO_CONTENT)
//    suspend fun deleteUser(
//        @PathVariable @Pattern(regexp = LOGIN_REGEX) login: String
//    ): ResponseEntity<Unit> {
//        log.debug("REST request to delete User: {}", login).run {
//            userService.deleteUser(login).run {
//                return noContent().headers(
//                    createAlert(
//                        properties.clientApp.name,
//                        "userManagement.deleted",
//                        login
//                    )
//                ).build()
//            }
//        }
//    }
//}

/*=================================================================================*/


/**
 * Controller to authenticate users.
 */

//@RestController
//@RequestMapping("/api")
//@Suppress("unused")
//class AuthenticationController(
//    private val tokenProvider: TokenProvider,
//    private val authenticationManager: ReactiveAuthenticationManager
//) {
//    /**
//     * Object to return as body in Jwt Authentication.
//     */
//    class JwtToken(@JsonProperty(AUTHORIZATION_ID_TOKEN) val idToken: String)
//
//    @PostMapping("/authenticate")
//    suspend fun authorize(@Valid @RequestBody loginVm: Login)
//            : ResponseEntity<JwtToken> = tokenProvider.createToken(
//        authenticationManager.authenticate(
//            UsernamePasswordAuthenticationToken(
//                loginVm.username,
//                loginVm.password
//            )
//        ).awaitSingle(), loginVm.rememberMe!!
//    ).run {
//        return ResponseEntity<JwtToken>(
//            JwtToken(idToken = this),
//            HttpHeaders().apply {
//                add(
//                    AUTHORIZATION_HEADER,
//                    "$BEARER_START_WITH$this"
//                )
//            },
//            OK
//        )
//    }
//}


/*=================================================================================*/


//import backend.BackendApplication.Log.log
//import common.domain.Avatar
//import backend.http.util.PaginationUtil.generatePaginationHttpHeaders
////import backend.accounts.management.UserService
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.toCollection
//import org.springframework.data.domain.PageImpl
//import org.springframework.data.domain.Pageable
//import org.springframework.data.domain.Sort.Order
//import org.springframework.http.ResponseEntity
//import org.springframework.http.ResponseEntity.badRequest
//import org.springframework.http.ResponseEntity.ok
//import org.springframework.http.server.reactive.ServerHttpRequest
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//import org.springframework.web.util.UriComponentsBuilder.fromHttpRequest

//@RestController
//@RequestMapping("/api")
//@Suppress("unused")
//class AvatarController(
//    private val userService: UserService
//) {
//    companion object {
//        private val ALLOWED_ORDERED_PROPERTIES =
//            arrayOf(
//                "id",
//                "login",
//                "firstName",
//                "lastName",
//                "email",
//                "activated",
//                "langKey"
//            )
//    }
//
//    /**
//     * {@code GET /users} : get all users with only the public informations - calling this are allowed for anyone.
//     *
//     * @param request a {@link ServerHttpRequest} request.
//     * @param pageable the pagination information.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
//     */
//    @GetMapping("/users")
//    suspend fun getAllAvatars(
//        request: ServerHttpRequest,
//        pageable: Pageable
//    ): ResponseEntity<Flow<Avatar>> = log
//        .debug("REST request to get all public User names").run {
//            return if (!onlyContainsAllowedProperties(pageable)) badRequest().build()
//            else {
//                ok().headers(
//                    generatePaginationHttpHeaders(
//                        fromHttpRequest(request),
//                        PageImpl<Avatar>(
//                            mutableListOf(),
//                            pageable,
//                            userService.countUsers()
//                        )
//                    )
//                ).body(userService.getAvatars(pageable))
//            }
//        }
//
//    private fun onlyContainsAllowedProperties(
//        pageable: Pageable
//    ): Boolean = pageable
//        .sort
//        .stream()
//        .map(Order::getProperty)
//        .allMatch(ALLOWED_ORDERED_PROPERTIES::contains)
//
//    /**
//     * Gets a list of all roles.
//     * @return a string list of all roles.
//     */
//    @GetMapping("/authorities")
//    suspend fun getAuthorities(): List<String> = userService
//        .getAuthorities()
//        .toCollection(mutableListOf())
//}
/*=================================================================================*/





@Service("userService")
//@Suppress("unused")
class UserService
    (
//    private val passwordEncoder: PasswordEncoder,
//    private val userRepository: UserRepository,
//    private val iUserRepository: IUserRepository,
//    private val userRepositoryPageable: UserRepositoryPageable,
//    private val userAuthRepository: UserAuthRepository,
//    private val authorityRepository: AuthorityRepository
    private val context: ApplicationContext,
) {
//    @PostConstruct
//    private fun init() = checkProfileLog(context)
//
//    @Transactional
//    suspend fun activateRegistration(key: String): User? =
//        log.debug("Activating user for activation key {}", key).run {
//            return@run iUserRepository.findOneByActivationKey(key).apply {
//                if (this != null) {
//                    activated = true
//                    activationKey = null
//                    saveUser(user = this).run {
//                        log.debug("Activated user: {}", this)
//                    }
//                } else log.debug("No user found with activation key {}", key)
//            }
//        }
//
//
//    suspend fun completePasswordReset(newPassword: String, key: String): User? =
//        log.debug("Reset user password for reset key {}", key).run {
//            userRepository.findOneByResetKey(key).apply {
//                return if (this != null &&
//                    resetDate?.isAfter(now().minusSeconds(86400)) == true
//                ) saveUser(
//                    apply {
//                        password = passwordEncoder.encode(newPassword)
//                        resetKey = null
//                        resetDate = null
//                    })
//                else null
//            }
//        }
//
//
//    @Transactional
//    suspend fun requestPasswordReset(mail: String): User? {
//        return userRepository
//            .findOneByEmail(mail)
//            .apply {
//                if (this != null && this.activated) {
//                    resetKey = generateResetKey
//                    resetDate = now()
//                    saveUser(this)
//                } else return null
//            }
//    }
//
//    @Transactional
//    suspend fun register(account: Account, password: String): User? = userRepository
//        .findOneByLogin(account.login!!)
//        ?.apply isActivatedOnCheckLogin@{
//            if (!activated) return@isActivatedOnCheckLogin userRepository.delete(user = this)
//            else throw UsernameAlreadyUsedException()
//        }
//        .also {
//            userRepository.findOneByEmail(account.email!!)
//                ?.apply isActivatedOnCheckEmail@{
//                    if (!activated) return@isActivatedOnCheckEmail userRepository.delete(user = this)
//                    else throw EmailAlreadyUsedException()
//                }
//        }
//        .apply {
//            return@register userRepository.save(
//                User(
//                    login = account.login,
//                    password = passwordEncoder.encode(password),
//                    firstName = account.firstName,
//                    lastName = account.lastName,
//                    email = account.email,
//                    imageUrl = account.imageUrl,
//                    langKey = account.langKey,
//                    activated = USER_INITIAL_ACTIVATED_VALUE,
//                    activationKey = generateActivationKey,
//                    authorities = mutableSetOf<AuthorityEntity>().apply {
//                        add(AuthorityEntity(role = ROLE_USER))
//                    })
//            )
//        }
//
//    @Transactional
//    suspend fun createUser(account: Account): User =
//        saveUser(account.toUser().apply {
//            password = passwordEncoder.encode(generatePassword)
//            resetKey = generateResetKey
//            resetDate = now()
//            activated = true
//            account.authorities?.map {
//                authorities?.remove(AuthorityEntity(it))
//                authorityRepository.findById(it).apply auth@{
//                    if (this@auth != null) authorities!!.add(this@auth)
//                }
//            }
//        }).also {
//            log.debug("Created Information for User: {}", it)
//        }
//
//
//    /**
//     * Update all information for a specific user, and return the modified user.
//     *
//     * @param account user to update.
//     * @return updated user.
//     */
//    @Transactional
//    suspend fun updateUser(account: Account): Account =
//        if (account.id != null) account
//        else {
//            val user = iUserRepository.findById(account.id!!)
//            if (user == null) account
//            else Account(saveUser(user.apply {
//                login = account.login
//                firstName = account.firstName
//                lastName = account.lastName
//                email = account.email
//                imageUrl = account.imageUrl
//                activated = account.activated
//                langKey = account.langKey
//                if (!authorities.isNullOrEmpty()) {
//                    account.authorities!!.forEach {
//                        authorities?.remove(AuthorityEntity(it))
//                        authorityRepository.findById(it).apply auth@{
//                            if (this@auth != null) authorities!!.add(this@auth)
//                        }
//                    }
//                    authorities!!.clear()
//                    userAuthRepository.deleteAllUserAuthoritiesByUser(account.id!!)
//                }
//            }).also {
//                log.debug("Changed Information for User: {}", it)
//            })
//        }
//
//
//    @Transactional
//    suspend fun deleteUser(login: String): Unit =
//        userRepository.findOneByLogin(login).apply {
//            userRepository.delete(this!!)
//        }.run { log.debug("Changed Information for User: $this") }
//
//    /**
//     * Update basic information (first name, last name, email, language) for the current user.
//     *
//     * @param firstName first name of user.
//     * @param lastName  last name of user.
//     * @param email     email id of user.
//     * @param langKey   language key.
//     * @param imageUrl  image URL of user.
//     */
//    @Transactional
//    suspend fun updateUser(
//        firstName: String?,
//        lastName: String?,
//        email: String?,
//        langKey: String?,
//        imageUrl: String?
//    ): Unit = SecurityUtils.getCurrentUserLogin().run {
//        userRepository.findOneByLogin(login = this)?.apply {
//            this.firstName = firstName
//            this.lastName = lastName
//            this.email = email
//            this.langKey = langKey
//            this.imageUrl = imageUrl
//            saveUser(user = this).also {
//                log.debug("Changed Information for User: {}", it)
//            }
//        }
//    }
//
//
//    @Transactional
//    suspend fun saveUser(user: User): User = SecurityUtils.getCurrentUserLogin()
//        .run currentUserLogin@{
//            user.apply user@{
//                SYSTEM_USER.apply systemUser@{
//                    if (createdBy.isNullOrBlank()) {
//                        createdBy = this@systemUser
//                        lastModifiedBy = this@systemUser
//                    } else lastModifiedBy = this@currentUserLogin
//                }
//                userRepository.save(this@user)
//            }
//        }
//
//    @Transactional
//    suspend fun changePassword(currentClearTextPassword: String, newPassword: String) {
//        SecurityUtils.getCurrentUserLogin().apply {
//            if (!isNullOrBlank()) {
//                userRepository.findOneByLogin(this).apply {
//                    if (this != null) {
//                        if (!passwordEncoder.matches(
//                                currentClearTextPassword,
//                                password
//                            )
//                        ) throw InvalidPasswordException()
//                        else saveUser(this.apply {
//                            password = passwordEncoder.encode(newPassword)
//                        }).run {
//                            log.debug("Changed password for User: {}", this)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//
//    @Transactional(readOnly = true)
//    suspend fun getAllManagedUsers(pageable: Pageable): Flow<Account> =
//        userRepositoryPageable
//            .findAllByIdNotNull(pageable)
//            .asFlow()
//            .map {
//                Account(
//                    userRepository.findOneWithAuthoritiesByLogin(it.login!!)!!
//                )
//            }
//
//
//    @Transactional(readOnly = true)
//    suspend fun getAvatars(pageable: Pageable)
//            : Flow<Avatar> = userRepositoryPageable
//        .findAllByActivatedIsTrue(pageable)
//        .filter { it != null }
//        .map { Avatar(it) }
//        .asFlow()
//
//    @Transactional(readOnly = true)
//    suspend fun countUsers(): Long = userRepository.count()
//
//    @Transactional(readOnly = true)
//    suspend fun getUserWithAuthoritiesByLogin(login: String): User? =
//        userRepository.findOneByLogin(login)
//
//    suspend fun findAccountByEmail(email: String): Account? =
//        Account(userRepository.findOneByEmail(email).apply {
//            if (this == null) return null
//        }!!)
//
//    suspend fun findAccountByLogin(login: String): Account? =
//        Account(userRepository.findOneWithAuthoritiesByLogin(login).apply {
//            if (this == null) return null
//        }!!)
//
//    /**
//     * Gets a list of all the authorities.
//     * @return a list of all the authorities.
//     */
//    @Transactional(readOnly = true)
//    suspend fun getAuthorities(): Flow<String> =
//        authorityRepository
//            .findAll()
//            .map { it.role }
//
//    @Transactional(readOnly = true)
//    suspend fun getUserWithAuthorities(): User? =
//        SecurityUtils.getCurrentUserLogin().run {
//            return@run if (isNullOrBlank()) null
//            else userRepository
//                .findOneWithAuthoritiesByLogin(this)
//        }
//
//    /**
//     * Not activated users should be automatically deleted after 3 days.
//     *
//     *
//     * This is scheduled to get fired everyday, at 01:00 (am).
//     */
//    @Scheduled(cron = "0 0 1 * * ?")
//    fun removeNotActivatedUsers() {
//        runBlocking {
//            removeNotActivatedUsersReactively()
//                .collect()
//        }
//    }
//
//    @Transactional
//    suspend fun removeNotActivatedUsersReactively(): Flow<User> = userRepository
//        .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(
//            ofInstant(
//                now().minus(3, DAYS),
//                UTC
//            )
//        ).map {
//            it.apply {
//                userRepository.delete(this).also {
//                    log.debug("Deleted User: {}", this)
//                }
//            }
//        }
}

/*=================================================================================*/
