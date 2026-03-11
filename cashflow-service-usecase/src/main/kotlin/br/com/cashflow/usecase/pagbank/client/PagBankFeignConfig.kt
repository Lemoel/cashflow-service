package br.com.cashflow.usecase.pagbank.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PagBankFeignConfig {
    @Bean
    fun basicAuthRequestInterceptor(
        @Value("\${pagbank.api.username:}") username: String,
        @Value("\${pagbank.api.password:}") password: String,
    ): feign.auth.BasicAuthRequestInterceptor = feign.auth.BasicAuthRequestInterceptor(username, password)
}
