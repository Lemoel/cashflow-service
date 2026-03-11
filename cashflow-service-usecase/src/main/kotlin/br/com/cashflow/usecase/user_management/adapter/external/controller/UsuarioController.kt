package br.com.cashflow.usecase.user_management.adapter.external.controller

import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.user_management.adapter.external.dto.EmailUnicoResponseDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.UsuarioCreateRequestDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.UsuarioListResponseDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.UsuarioResponseDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.UsuarioUpdateRequestDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.toUsuarioResponseDto
import br.com.cashflow.usecase.user_management.port.UserManagementInputPort
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("isAuthenticated()")
class UsuarioController(
    private val userManagement: UserManagementInputPort,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) congregacaoId: UUID?,
        @RequestParam(required = false) perfil: String?,
        @RequestParam(required = false) ativo: Boolean?,
    ): UsuarioListResponseDto {
        val pageResult =
            userManagement.findAll(
                page = page,
                size = size.coerceIn(1, 100),
                email = email,
                congregacaoId = congregacaoId,
                perfil = perfil,
                ativo = ativo,
            )
        return UsuarioListResponseDto(
            items = pageResult.items.map { it.toUsuarioResponseDto() },
            total = pageResult.total,
            page = pageResult.page,
            pageSize = pageResult.pageSize,
        )
    }

    @GetMapping("/email-unico")
    fun emailUnico(
        @RequestParam email: String,
        @RequestParam(required = false) excludeId: String?,
    ): ResponseEntity<EmailUnicoResponseDto> {
        val unico = userManagement.isEmailAvailable(email, excludeId)
        return ResponseEntity.ok(EmailUnicoResponseDto(unico = unico))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: String,
    ): ResponseEntity<UsuarioResponseDto> {
        val item =
            userManagement.findById(id)
                ?: throw ResourceNotFoundException("Usuário não encontrado.")
        return ResponseEntity.ok(item.toUsuarioResponseDto())
    }

    @PostMapping
    fun create(
        @Valid @RequestBody request: UsuarioCreateRequestDto,
    ): ResponseEntity<UsuarioResponseDto> {
        val created = userManagement.create(request)
        val body = created.toUsuarioResponseDto()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/v1/usuarios/${body.id}")
            .body(body)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody request: UsuarioUpdateRequestDto,
    ): ResponseEntity<UsuarioResponseDto> {
        val updated = userManagement.update(id, request)
        return ResponseEntity.ok(updated.toUsuarioResponseDto())
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: String,
    ): ResponseEntity<Unit> {
        userManagement.delete(id)
        return ResponseEntity.noContent().build()
    }
}
