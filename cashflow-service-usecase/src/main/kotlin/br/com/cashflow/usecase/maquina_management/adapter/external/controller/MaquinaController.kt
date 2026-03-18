package br.com.cashflow.usecase.maquina_management.adapter.external.controller

import br.com.cashflow.commons.auth.CurrentUser
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaCreateRequestDto
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaHistoricoResponse
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaListResponse
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaResponse
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaUpdateRequestDto
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.toHistoricoResponse
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.toResponse
import br.com.cashflow.usecase.maquina_management.port.MaquinaManagementInputPort
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
@RequestMapping("/api/v1/maquinas")
@PreAuthorize("hasAnyRole('ADMIN','ADMIN_MATRIZ')")
class MaquinaController(
    private val maquinaManagement: MaquinaManagementInputPort,
) {
    @GetMapping
    fun search(
        @AuthenticationPrincipal currentUser: CurrentUser?,
        @RequestParam(required = false) maquinaId: String?,
        @RequestParam(required = false) congregacao: String?,
        @RequestParam(required = false) banco: String?,
        @RequestParam(required = false) departamentoId: UUID?,
        @RequestParam(required = false) tenantId: UUID?,
        @RequestParam(required = false) congregacaoId: UUID?,
        @RequestParam(required = false) numeroSerie: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): MaquinaListResponse {
        val tenantEfetivo = currentUser?.tenantId ?: tenantId
        val pageResult =
            maquinaManagement.listOrSearch(
                maquinaId,
                congregacao,
                banco,
                departamentoId,
                tenantEfetivo,
                congregacaoId,
                numeroSerie,
                page,
                size,
            )
        return MaquinaListResponse(
            items = pageResult.items.map { it.toResponse() },
            total = pageResult.total,
            page = pageResult.page,
            pageSize = pageResult.pageSize,
        )
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
    ): ResponseEntity<MaquinaResponse> {
        val maquina =
            maquinaManagement.findById(id)
                ?: throw ResourceNotFoundException("Máquina não encontrada")
        return ResponseEntity.ok(maquina.toResponse())
    }

    @PostMapping
    fun create(
        @Valid @RequestBody request: MaquinaCreateRequestDto,
    ): ResponseEntity<MaquinaResponse> {
        val created = maquinaManagement.create(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/v1/maquinas/${created.id}")
            .body(created.toResponse())
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: MaquinaUpdateRequestDto,
    ): ResponseEntity<MaquinaResponse> {
        val updated = maquinaManagement.update(id, request)
        return ResponseEntity.ok(updated.toResponse())
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID,
    ): ResponseEntity<Unit> {
        maquinaManagement.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/historico")
    fun getHistorico(
        @PathVariable id: UUID,
    ): ResponseEntity<List<MaquinaHistoricoResponse>> {
        val list = maquinaManagement.listHistoricoByMaquinaId(id)
        return ResponseEntity.ok(list.map { it.toHistoricoResponse() })
    }
}
