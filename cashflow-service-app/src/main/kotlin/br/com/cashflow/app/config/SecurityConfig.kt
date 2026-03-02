package br.com.cashflow.app.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.context.DelegatingSecurityContextRepository
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.web.cors.CorsConfigurationSource
import tools.jackson.databind.json.JsonMapper

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val corsConfigurationSource: CorsConfigurationSource,
    private val jsonMapper: JsonMapper,
) {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        securityContextRepository: SecurityContextRepository,
    ): SecurityFilterChain {
        http.csrf { it.disable() }
        http.cors { it.configurationSource(corsConfigurationSource) }
        http.sessionManagement {
            it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        }
        http.securityContext {
            it.securityContextRepository(securityContextRepository)
            it.requireExplicitSave(true)
        }
        http.authorizeHttpRequests {
            it.requestMatchers("/actuator/**").permitAll()
            it.anyRequest().authenticated()
        }
        http.httpBasic { }
        http.exceptionHandling {
            it.authenticationEntryPoint(json401EntryPoint())
        }
        http.logout { it.disable() }
        return http.build()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager = config.authenticationManager

    @Bean
    fun securityContextRepository(): SecurityContextRepository =
        DelegatingSecurityContextRepository(
            RequestAttributeSecurityContextRepository(),
            HttpSessionSecurityContextRepository(),
        )

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(12)

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val admin =
            User
                .builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .roles("ADMIN")
                .build()
        return InMemoryUserDetailsManager(admin)
    }

    private fun json401EntryPoint(): AuthenticationEntryPoint =
        AuthenticationEntryPoint { request: HttpServletRequest, response: HttpServletResponse, _: AuthenticationException ->
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            val body =
                mapOf(
                    "timestamp" to
                        java.time.Instant
                            .now()
                            .toString(),
                    "status" to HttpStatus.UNAUTHORIZED.value(),
                    "error" to "Unauthorized",
                    "message" to "Full authentication is required to access this resource",
                    "path" to request.requestURI,
                )
            jsonMapper.writeValue(response.outputStream, body)
        }
}
