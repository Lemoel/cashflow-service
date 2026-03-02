package br.com.cashflow.app.config

import br.com.cashflow.commons.audit.AuditorAwareImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware

@Configuration
class AuditingConfig {
    @Bean
    fun auditorProvider(): AuditorAware<String> = AuditorAwareImpl()
}
