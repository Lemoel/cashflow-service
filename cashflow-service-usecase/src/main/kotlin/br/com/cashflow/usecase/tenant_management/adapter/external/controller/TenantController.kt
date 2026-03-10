package br.com.cashflow.usecase.tenant_management.adapter.external.controller

import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.tenant.model.TenantFilter
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.CnpjUnicoResponse
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantCreateRequest
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantListOption
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantListResponse
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantResponse
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantUpdateRequest
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.toListOption
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.toResponse
import br.com.cashflow.usecase.tenant_management.port.TenantManagementInputPort
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
@RequestMapping("/api/v1/tenants")
@PreAuthorize("hasRole('ADMIN')")
class TenantController(
    private val tenantManagement: TenantManagementInputPort,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) nome: String?,
        @RequestParam(required = false) cnpj: String?,
        @RequestParam(required = false) active: Boolean?,
    ): TenantListResponse {
        val filter = TenantFilter(nome = nome?.takeIf { it.isNotBlank() }, cnpj = cnpj?.takeIf { it.isNotBlank() }, active = active)
        val pageResult = tenantManagement.findAll(filter, page, size)
        return TenantListResponse(
            items = pageResult.items.map { it.toResponse() },
            total = pageResult.total,
            page = pageResult.page,
            pageSize = pageResult.pageSize,
        )
    }

    @GetMapping("/list")
    fun listForDropdown(): List<TenantListOption> = tenantManagement.findActiveForList().map { it.toListOption() }

    @GetMapping("/cnpj-unico")
    fun cnpjUnico(
        @RequestParam cnpj: String,
        @RequestParam(required = false) excludeId: UUID?,
    ): ResponseEntity<CnpjUnicoResponse> {
        val unique = tenantManagement.isCnpjAvailable(cnpj, excludeId)
        return ResponseEntity.ok(CnpjUnicoResponse(unique = unique))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
    ): ResponseEntity<TenantResponse> {
        val tenant =
            tenantManagement.findById(id)
                ?: throw ResourceNotFoundException("Tenant not found: $id")
        return ResponseEntity.ok(tenant.toResponse())
    }

    @PostMapping
    fun create(
        @Valid @RequestBody request: TenantCreateRequest,
    ): ResponseEntity<TenantResponse> {
        val created = tenantManagement.create(request)
        val body = created.toResponse()
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/v1/tenants/${created.id}")
            .body(body)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: TenantUpdateRequest,
    ): ResponseEntity<TenantResponse> {
        val updated = tenantManagement.update(id, request)
        return ResponseEntity.ok(updated.toResponse())
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID,
    ): ResponseEntity<Unit> {
        tenantManagement.delete(id)
        return ResponseEntity.noContent().build()
    }
}
