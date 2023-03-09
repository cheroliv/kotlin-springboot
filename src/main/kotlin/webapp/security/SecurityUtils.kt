@file:Suppress("unused")

package webapp.security

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.userdetails.UserDetails
import webapp.Constants.ROLE_ANONYMOUS

object SecurityUtils {

    private fun extractPrincipal(authentication: Authentication?) =
        when (authentication) {
            null -> ""
            else -> when (val principal = authentication.principal) {
                is UserDetails -> principal.username
                is String -> principal
                else -> ""
            }
        }

    suspend fun currentUserLogin() = extractPrincipal(
        getContext().awaitSingle().authentication
    )!!

    suspend fun currentUserJwt() = getContext()
        .map(SecurityContext::getAuthentication)
        .filter { it.credentials is String }
        .map { it.credentials as String }
        .awaitSingle()!!

    suspend fun isAuthenticated() = getContext()
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getAuthorities)
        .map { roles: Collection<GrantedAuthority> ->
            roles.map(GrantedAuthority::getAuthority)
                .none { it == ROLE_ANONYMOUS }
        }.awaitSingle()!!


    suspend fun isCurrentUserInRole(authority: String) = getContext()
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getAuthorities)
        .map { roles: Collection<GrantedAuthority> ->
            roles.map(GrantedAuthority::getAuthority)
                .any { it == authority }
        }.awaitSingle()!!
}