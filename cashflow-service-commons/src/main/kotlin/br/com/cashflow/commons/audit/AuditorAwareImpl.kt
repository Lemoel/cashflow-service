package br.com.cashflow.commons.audit

import br.com.cashflow.commons.auth.CurrentUser
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.util.Optional

class AuditorAwareImpl : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return Optional.of("system")
        }
        val principal = authentication.principal
        return when (principal) {
            is CurrentUser -> Optional.of(principal.email)
            is UserDetails -> Optional.of(principal.username)
            is String -> Optional.of(principal)
            else -> Optional.of("system")
        }
    }
}
