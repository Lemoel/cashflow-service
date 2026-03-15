package br.com.cashflow.app.filter

import br.com.cashflow.commons.tenant.TenantContext
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.web.filter.OncePerRequestFilter

class TenantContextFilter :
    OncePerRequestFilter(),
    Ordered {
    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE + 99

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } finally {
            TenantContext.clear()
        }
    }
}
