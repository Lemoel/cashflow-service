package br.com.cashflow.usecase.tenant_management.service

import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.port.TenantFilter
import br.com.cashflow.usecase.tenant.port.TenantOutputPort
import br.com.cashflow.usecase.tenant.port.TenantPage
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantCreateRequest
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantUpdateRequest
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.applyTo
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.toEntity
import br.com.cashflow.usecase.tenant_management.port.TenantManagementInputPort
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private const val CNPJ_DIGITS_LENGTH = 14

@Service
class TenantManagementService(
    private val tenantOutputPort: TenantOutputPort,
) : TenantManagementInputPort {
    override fun create(request: TenantCreateRequest): Tenant {
        val entity = request.toEntity()
        requireCnpjLength(entity.cnpj)
        if (tenantOutputPort.existsByCnpjExcludingId(entity.cnpj, null)) {
            throw ConflictException("CNPJ already registered")
        }
        return tenantOutputPort.save(entity)
    }

    override fun update(
        id: UUID,
        request: TenantUpdateRequest,
    ): Tenant {
        val existing =
            tenantOutputPort.findById(id)
                ?: throw ResourceNotFoundException("Tenant not found: $id")
        request.applyTo(existing)
        requireCnpjLength(existing.cnpj)
        if (tenantOutputPort.existsByCnpjExcludingId(existing.cnpj, id)) {
            throw ConflictException("CNPJ already registered")
        }
        return tenantOutputPort.save(existing)
    }

    override fun findById(id: UUID): Tenant? = tenantOutputPort.findById(id)

    override fun findAll(
        filter: TenantFilter?,
        page: Int,
        size: Int,
    ): TenantPage = tenantOutputPort.findAll(filter, page, size)

    override fun findActiveForList(): List<Tenant> = tenantOutputPort.findActiveOrderByTradeName()

    override fun isCnpjAvailable(
        cnpj: String,
        excludeId: UUID?,
    ): Boolean {
        val normalized = cnpj.filter { it.isDigit() }
        requireCnpjLength(normalized)
        return !tenantOutputPort.existsByCnpjExcludingId(normalized, excludeId)
    }

    @Transactional
    override fun delete(id: UUID) {
        val existing =
            tenantOutputPort.findById(id)
                ?: throw ResourceNotFoundException("Tenant not found: $id")
        try {
            tenantOutputPort.deleteById(id)
        } catch (error: DataIntegrityViolationException) {
            throw ConflictException(
                "Cannot delete tenant: there are dependent congregations or departments",
            )
        }
    }

    private fun requireCnpjLength(cnpj: String) {
        if (cnpj.length != CNPJ_DIGITS_LENGTH) {
            throw IllegalArgumentException("cnpj must contain exactly $CNPJ_DIGITS_LENGTH digits")
        }
    }
}
