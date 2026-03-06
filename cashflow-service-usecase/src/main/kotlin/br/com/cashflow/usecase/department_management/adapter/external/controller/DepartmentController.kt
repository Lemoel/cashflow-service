package br.com.cashflow.usecase.department_management.adapter.external.controller

import br.com.cashflow.commons.auth.CurrentUser
import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.department.port.DepartmentFilter
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentCreateRequest
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentListResponse
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentResponse
import br.com.cashflow.usecase.department_management.adapter.external.dto.DepartmentUpdateRequest
import br.com.cashflow.usecase.department_management.adapter.external.dto.toResponse
import br.com.cashflow.usecase.department_management.port.DepartmentManagementInputPort
import br.com.cashflow.usecase.tenant.port.TenantOutputPort
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
@RequestMapping("/api/v1/departamentos")
@PreAuthorize("hasAnyRole('ADMIN','ADMIN_MATRIZ')")
class DepartmentController(
    private val departmentManagement: DepartmentManagementInputPort,
    private val tenantOutputPort: TenantOutputPort,
) {
    @GetMapping
    fun list(
        @AuthenticationPrincipal currentUser: CurrentUser,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) nome: String?,
        @RequestParam(required = false) ativo: Boolean?,
    ): DepartmentListResponse {
        val filter =
            DepartmentFilter(
                tenantId = currentUser.tenantId,
                nome = nome?.takeIf { it.isNotBlank() },
                ativo = ativo,
            )
        val pageResult = departmentManagement.findAll(filter, page, size)
        val tenantNomeMap =
            pageResult.items
                .mapNotNull { it.tenantId }
                .distinct()
                .associateWith { tenantOutputPort.findById(it)?.tradeName }
        return DepartmentListResponse(
            items = pageResult.items.map { it.toResponse(tenantNomeMap[it.tenantId]) },
            total = pageResult.total,
            page = pageResult.page,
            pageSize = pageResult.pageSize,
        )
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
    ): ResponseEntity<DepartmentResponse> {
        val department =
            departmentManagement.findById(id)
                ?: throw ResourceNotFoundException("Departamento não encontrado")
        val tenantNome = department.tenantId?.let { tenantOutputPort.findById(it)?.tradeName }
        return ResponseEntity.ok(department.toResponse(tenantNome))
    }

    @PostMapping
    fun create(
        @AuthenticationPrincipal currentUser: CurrentUser,
        @Valid @RequestBody request: DepartmentCreateRequest,
    ): ResponseEntity<DepartmentResponse> {
        val tenantId =
            currentUser.tenantId
                ?: throw BusinessException("Usuário sem igreja vinculada. Não é possível criar departamento.")
        val created = departmentManagement.create(tenantId, request)
        val tenantNome = created.tenantId?.let { tenantOutputPort.findById(it)?.tradeName }
        val body = created.toResponse(tenantNome)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/v1/departamentos/${created.id}")
            .body(body)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: DepartmentUpdateRequest,
    ): ResponseEntity<DepartmentResponse> {
        val updated = departmentManagement.update(id, request)
        val tenantNome = updated.tenantId?.let { tenantOutputPort.findById(it)?.tradeName }
        return ResponseEntity.ok(updated.toResponse(tenantNome))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID,
    ): ResponseEntity<Unit> {
        departmentManagement.delete(id)
        return ResponseEntity.noContent().build()
    }
}
