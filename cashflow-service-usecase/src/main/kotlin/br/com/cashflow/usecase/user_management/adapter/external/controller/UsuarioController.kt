package br.com.cashflow.usecase.user_management.adapter.external.controller

import br.com.cashflow.usecase.user_management.adapter.external.dto.EmailUnicoResponseDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.UsuarioCriadoResponseDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.UsuarioListResponseDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.UsuarioRequestDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.UsuarioResponseDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.toUsuarioCriadoResponseDto
import br.com.cashflow.usecase.user_management.adapter.external.dto.toUsuarioResponseDto
import br.com.cashflow.usecase.user_management.port.UserManagementInputPort
import br.com.cashflow.usecase.user_management.port.UsuarioCommand
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
@PreAuthorize("hasAnyAuthority('ADMIN', 'ADMIN_MATRIZ')")
class UsuarioController(
    private val userManagementInputPort: UserManagementInputPort,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) congregacaoId: UUID?,
        @RequestParam(required = false) perfil: String?,
        @RequestParam(required = false) ativo: Boolean?,
    ): ResponseEntity<UsuarioListResponseDto> {
        val resultado = userManagementInputPort.findAll(page, size, email, congregacaoId, perfil, ativo)
        val response =
            UsuarioListResponseDto(
                items = resultado.items.map { it.toUsuarioResponseDto() },
                total = resultado.total,
                page = resultado.page,
                pageSize = resultado.pageSize,
            )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/email-unico")
    fun emailUnico(
        @RequestParam email: String,
        @RequestParam(required = false) excludeId: String?,
    ): ResponseEntity<EmailUnicoResponseDto> {
        val disponivel = userManagementInputPort.isEmailAvailable(email, excludeId)
        return ResponseEntity.ok(EmailUnicoResponseDto(unico = disponivel))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: String,
    ): ResponseEntity<UsuarioResponseDto> {
        val usuario = userManagementInputPort.findById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(usuario.toUsuarioResponseDto())
    }

    @PostMapping
    fun create(
        @Valid @RequestBody dto: UsuarioRequestDto,
    ): ResponseEntity<UsuarioCriadoResponseDto> {
        val command =
            UsuarioCommand(
                nome = dto.nome,
                email = dto.email,
                telefone = dto.telefone,
                perfil = dto.perfil,
                congregacaoId = dto.congregacaoId,
                ativo = dto.ativo,
            )
        val resultado = userManagementInputPort.create(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado.toUsuarioCriadoResponseDto())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody dto: UsuarioRequestDto,
    ): ResponseEntity<UsuarioResponseDto> {
        val command =
            UsuarioCommand(
                nome = dto.nome,
                email = dto.email,
                telefone = dto.telefone,
                perfil = dto.perfil,
                congregacaoId = dto.congregacaoId,
                ativo = dto.ativo,
            )
        val atualizado = userManagementInputPort.update(id, command)
        return ResponseEntity.ok(atualizado.toUsuarioResponseDto())
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: String,
    ): ResponseEntity<Void> {
        userManagementInputPort.delete(id)
        return ResponseEntity.noContent().build()
    }
}
