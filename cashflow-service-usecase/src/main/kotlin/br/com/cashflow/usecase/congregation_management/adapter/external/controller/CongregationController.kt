package br.com.cashflow.usecase.congregation_management.adapter.external.controller

import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.congregation.port.CongregationFilter
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CnpjUnicoCongregationResponse
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationCreateRequest
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationListOption
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationListResponse
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationResponse
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationUpdateRequest
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.toResponse
import br.com.cashflow.usecase.congregation_management.port.CongregationManagementInputPort
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
@RequestMapping("/api/v1/congregacoes")
@PreAuthorize("hasAnyRole('ADMIN','ADMIN_MATRIZ')")
class CongregationController(
    private val congregationManagement: CongregationManagementInputPort,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) nome: String?,
        @RequestParam(required = false) cnpj: String?,
        @RequestParam(required = false) ativo: Boolean?,
    ): CongregationListResponse {
        val filter =
            CongregationFilter(
                nome = nome?.takeIf { it.isNotBlank() },
                cnpj = cnpj?.takeIf { it.isNotBlank() },
                ativo = ativo,
            )
        val pageResult = congregationManagement.findAll(filter, page, size)
        return CongregationListResponse(
            items = pageResult.items.map { it.toResponse() },
            total = pageResult.total,
            page = pageResult.page,
            pageSize = pageResult.pageSize,
        )
    }

    @GetMapping("/list")
    fun listForDropdown(): List<CongregationListOption> =
        congregationManagement.findListForDropdown().map { (id, nome) ->
            CongregationListOption(id = id.toString(), nome = nome)
        }

    @GetMapping("/setoriais")
    fun listSetoriais(): List<CongregationListOption> =
        congregationManagement.findSetoriais().map { (id, nome) ->
            CongregationListOption(id = id.toString(), nome = nome)
        }

    @GetMapping("/cnpj-unico")
    fun cnpjUnico(
        @RequestParam cnpj: String,
        @RequestParam(required = false) excludeId: UUID?,
    ): ResponseEntity<CnpjUnicoCongregationResponse> {
        val unique = congregationManagement.isCnpjAvailable(cnpj, excludeId)
        return ResponseEntity.ok(CnpjUnicoCongregationResponse(unique = unique))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
    ): ResponseEntity<CongregationResponse> {
        val congregation =
            congregationManagement.findById(id)
                ?: throw ResourceNotFoundException("Congregação não encontrada")
        return ResponseEntity.ok(congregation.toResponse())
    }

    @PostMapping
    fun create(
        @Valid @RequestBody request: CongregationCreateRequest,
    ): ResponseEntity<CongregationResponse> {
        val created = congregationManagement.create(request)
        val body = created.toResponse()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/v1/congregacoes/${created.id}")
            .body(body)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CongregationUpdateRequest,
    ): ResponseEntity<CongregationResponse> {
        val updated = congregationManagement.update(id, request)
        return ResponseEntity.ok(updated.toResponse())
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID,
    ): ResponseEntity<Unit> {
        congregationManagement.delete(id)
        return ResponseEntity.noContent().build()
    }
}
