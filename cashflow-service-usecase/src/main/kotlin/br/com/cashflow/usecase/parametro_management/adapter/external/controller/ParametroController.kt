package br.com.cashflow.usecase.parametro_management.adapter.external.controller

import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroChaveOption
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroCreateRequestDto
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroListResponse
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroResponseDto
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroUpdateRequestDto
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.toResponse
import br.com.cashflow.usecase.parametro_management.port.ParametroManagementInputPort
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
@RequestMapping("/api/v1/parametros")
@PreAuthorize("hasAnyRole('ADMIN','ADMIN_MATRIZ')")
class ParametroController(
    private val inputPort: ParametroManagementInputPort,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) chave: String?,
        @RequestParam(required = false) ativo: Boolean?,
    ): ParametroListResponse {
        val filter =
            ParametroFilterModel(
                chave = chave?.takeIf { it.isNotBlank() },
                ativo = ativo,
            )
        val pageResult = inputPort.findAll(filter, page, size)
        return ParametroListResponse(
            items = pageResult.items.map { it.toResponse() },
            total = pageResult.total,
            page = pageResult.page,
            pageSize = pageResult.pageSize,
        )
    }

    @GetMapping("/chaves")
    fun listChaves(): List<ParametroChaveOption> =
        inputPort.findChavesForDropdown().map { (id, nome) ->
            ParametroChaveOption(id = id, nome = nome)
        }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
    ): ResponseEntity<ParametroResponseDto> {
        val parametro =
            inputPort.findById(id)
                ?: throw ResourceNotFoundException("Parâmetro não encontrado")
        return ResponseEntity.ok(parametro.toResponse())
    }

    @PostMapping
    fun create(
        @Valid @RequestBody request: ParametroCreateRequestDto,
    ): ResponseEntity<ParametroResponseDto> {
        val created = inputPort.create(request)
        val body = created.toResponse()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/v1/parametros/${created.id}")
            .body(body)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ParametroUpdateRequestDto,
    ): ResponseEntity<ParametroResponseDto> {
        val updated = inputPort.update(id, request)
        return ResponseEntity.ok(updated.toResponse())
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID,
    ): ResponseEntity<Unit> {
        inputPort.delete(id)
        return ResponseEntity.noContent().build()
    }
}
