package br.com.cashflow.app.security

import br.com.cashflow.commons.auth.CurrentUser
import br.com.cashflow.usecase.acesso.port.AcessoOutputPort
import br.com.cashflow.usecase.user_authentication.port.TokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import tools.jackson.databind.json.JsonMapper

class JwtAuthenticationFilter(
    private val tokenProvider: TokenProvider,
    private val acessoOutputPort: AcessoOutputPort,
    private val jsonMapper: JsonMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }
        val token = authHeader.substring(7).trim()
        if (token.isEmpty()) {
            filterChain.doFilter(request, response)
            return
        }
        val claims = tokenProvider.validateToken(token)
        if (claims == null) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            val body =
                mapOf(
                    "timestamp" to
                        java.time.Instant
                            .now()
                            .toString(),
                    "status" to HttpStatus.UNAUTHORIZED.value(),
                    "error" to "Unauthorized",
                    "message" to "Token inválido ou expirado.",
                    "path" to request.requestURI,
                )
            jsonMapper.writeValue(response.outputStream, body)
            return
        }
        val acesso = acessoOutputPort.findByEmail(claims.sub)
        if (acesso == null || !acesso.ativo) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            val body =
                mapOf(
                    "timestamp" to
                        java.time.Instant
                            .now()
                            .toString(),
                    "status" to HttpStatus.UNAUTHORIZED.value(),
                    "error" to "Unauthorized",
                    "message" to "Token inválido ou expirado.",
                    "path" to request.requestURI,
                )
            jsonMapper.writeValue(response.outputStream, body)
            return
        }
        val authority = SimpleGrantedAuthority("ROLE_${claims.perfil}")
        val currentUser = CurrentUser(email = claims.sub, perfil = claims.perfil, tenantId = claims.tenantId)
        val authentication = UsernamePasswordAuthenticationToken(currentUser, null, listOf(authority))
        SecurityContextHolder.getContext().authentication = authentication
        filterChain.doFilter(request, response)
    }
}
