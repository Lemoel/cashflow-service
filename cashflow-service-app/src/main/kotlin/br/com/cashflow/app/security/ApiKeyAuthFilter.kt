package br.com.cashflow.app.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.web.filter.OncePerRequestFilter

class ApiKeyAuthFilter(
    private val apiKey: String,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!request.requestURI.startsWith("/api/v1/movimentos")) {
            filterChain.doFilter(request, response)
            return
        }
        if (SecurityContextHolder.getContext().authentication?.isAuthenticated == true) {
            filterChain.doFilter(request, response)
            return
        }
        val key = request.getHeader("X-API-KEY")
        when {
            key.isNullOrBlank() -> {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                response.contentType = "text/plain;charset=UTF-8"
                response.writer.write("API Key não informada")
            }
            key != apiKey -> {
                response.status = HttpServletResponse.SC_FORBIDDEN
                response.contentType = "text/plain;charset=UTF-8"
                response.writer.write("API Key inválida")
            }
            else -> {
                val auth =
                    PreAuthenticatedAuthenticationToken(
                        "API_KEY",
                        null,
                        listOf(SimpleGrantedAuthority("ROLE_API")),
                    )
                auth.isAuthenticated = true
                SecurityContextHolder.getContext().authentication = auth
                filterChain.doFilter(request, response)
            }
        }
    }
}
