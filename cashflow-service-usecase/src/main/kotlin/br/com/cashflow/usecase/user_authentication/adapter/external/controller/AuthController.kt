package br.com.cashflow.usecase.user_authentication.adapter.external.controller

import br.com.cashflow.commons.auth.CurrentUser
import br.com.cashflow.usecase.user_authentication.adapter.external.dto.ChangePasswordRequestDto
import br.com.cashflow.usecase.user_authentication.adapter.external.dto.LoginRequestDto
import br.com.cashflow.usecase.user_authentication.adapter.external.dto.RefreshTokenRequestDto
import br.com.cashflow.usecase.user_authentication.port.AuthInputPort
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authInputPort: AuthInputPort,
) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequestDto,
    ): ResponseEntity<*> {
        val response = authInputPort.login(request.email, request.password)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody request: RefreshTokenRequestDto,
    ): ResponseEntity<*> {
        val response = authInputPort.refresh(request.refreshToken)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal currentUser: CurrentUser,
    ): ResponseEntity<*> {
        val response = authInputPort.getCurrentUser(currentUser.email)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/password")
    fun changePassword(
        @AuthenticationPrincipal currentUser: CurrentUser,
        @Valid @RequestBody request: ChangePasswordRequestDto,
    ): ResponseEntity<Void> {
        authInputPort.changePassword(
            currentUser.email,
            request.currentPassword,
            request.newPassword,
        )
        return ResponseEntity.noContent().build()
    }
}
