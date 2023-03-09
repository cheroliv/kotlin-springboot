package webapp.security

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.context.ReactiveSecurityContextHolder.withAuthentication
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import webapp.Constants.AUTHORIZATION_HEADER
import webapp.Constants.BEARER_START_WITH

@Component("jwtFilter")
class JwtFilter(private val security: Security) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        resolveToken(exchange.request).apply token@{
            chain.apply {
                return when {
                    !isNullOrBlank() && security.validateToken(this@token) ->
                        filter(exchange)
                            .contextWrite(withAuthentication(security.authentication(this@token)))

                    else -> filter(exchange)
                }
            }
        }
    }

    private fun resolveToken(request: ServerHttpRequest): String? = request
        .headers
        .getFirst(AUTHORIZATION_HEADER)
        .apply {
            return when {
                !isNullOrBlank() && startsWith(BEARER_START_WITH) ->
                    substring(startIndex = 7)

                else -> null
            }
        }
}