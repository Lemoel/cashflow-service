package br.com.cashflow.usecase.tenant_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.tenant.entity.Tenant
import br.com.cashflow.usecase.tenant.model.TenantFilter
import br.com.cashflow.usecase.tenant.model.TenantIdName
import br.com.cashflow.usecase.tenant.model.TenantPage
import br.com.cashflow.usecase.tenant.port.TenantOutputPort
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantCreateRequestDto
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.TenantUpdateRequestDto
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.applyTo
import br.com.cashflow.usecase.tenant_management.adapter.external.dto.toEntity
import br.com.cashflow.usecase.tenant_management.port.TenantManagementInputPort
import br.com.cashflow.usecase.tenant_management.port.TenantSchemaProvisionerPort
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.UUID

private const val CNPJ_DIGITS_LENGTH = 14

@Service
class TenantManagementService(
    private val tenantOutputPort: TenantOutputPort,
    private val tenantSchemaProvisioner: TenantSchemaProvisionerPort,
) : TenantManagementInputPort {
    @Transactional
    override fun create(request: TenantCreateRequestDto): Tenant {
        val entity = request.toEntity()
        entity.schemaName = "tenant_${entity.cnpj}"
        requireCnpjLength(entity.cnpj)
        if (tenantOutputPort.existsByCnpjExcludingId(entity.cnpj, null)) {
            throw ConflictException("CNPJ already registered")
        }
        val saved = tenantOutputPort.save(entity)
        val schemaNameToProvision = saved.schemaName
        val tenantIdToCompensate = saved.id
        require(schemaNameToProvision.isNotBlank()) { "Tenant schema name is blank" }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        runProvision(schemaNameToProvision, tenantIdToCompensate)
                    }
                },
            )
        } else {
            runProvision(schemaNameToProvision, tenantIdToCompensate)
        }
        return saved
    }

    private fun runProvision(
        schemaName: String,
        tenantIdToCompensate: UUID?,
    ) {
        try {
            tenantSchemaProvisioner.provision(schemaName)
        } catch (e: Exception) {
            log.error("Falha ao provisionar schema do tenant: $schemaName", e)
            tenantIdToCompensate?.let { tenantOutputPort.deleteById(it) }
            throw BusinessException(
                "Falha ao provisionar schema do tenant: ${e.message ?: e.javaClass.simpleName}",
            )
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(TenantManagementService::class.java)
    }

    @Transactional
    override fun update(
        id: UUID,
        request: TenantUpdateRequestDto,
    ): Tenant {
        val existing =
            tenantOutputPort.findById(id)
                ?: throw ResourceNotFoundException("Tenant not found: $id")
        request.applyTo(existing)
        return tenantOutputPort.save(existing)
    }

    @Transactional(readOnly = true)
    override fun findById(id: UUID): Tenant? = tenantOutputPort.findById(id)

    @Transactional(readOnly = true)
    override fun findAll(
        filter: TenantFilter?,
        page: Int,
        size: Int,
    ): TenantPage = tenantOutputPort.findAll(filter, page, size)

    @Transactional(readOnly = true)
    override fun findActiveForList(): List<TenantIdName> = tenantOutputPort.findActiveOrderByTradeName()

    @Transactional(readOnly = true)
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
        val schemaNameToDrop = existing.schemaName
        try {
            tenantOutputPort.deleteById(id)
        } catch (error: DataIntegrityViolationException) {
            throw ConflictException(
                "Cannot delete tenant: there are dependent congregations or departments",
            )
        }
        if (schemaNameToDrop.isNotBlank() && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        runCatching { tenantSchemaProvisioner.dropSchema(schemaNameToDrop) }
                            .onFailure { log.warn("Falha ao remover schema do tenant: $schemaNameToDrop", it) }
                    }
                },
            )
        }
    }

    private fun requireCnpjLength(cnpj: String) {
        if (cnpj.length != CNPJ_DIGITS_LENGTH) {
            throw IllegalArgumentException("cnpj must contain exactly $CNPJ_DIGITS_LENGTH digits")
        }
    }
}
