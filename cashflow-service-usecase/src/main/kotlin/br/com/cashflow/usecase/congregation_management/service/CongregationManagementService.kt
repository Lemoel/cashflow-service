package br.com.cashflow.usecase.congregation_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.commons.util.CnpjValidator
import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.model.CongregationFilter
import br.com.cashflow.usecase.congregation.model.CongregationPage
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationCreateRequest
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.CongregationUpdateRequest
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.applyTo
import br.com.cashflow.usecase.congregation_management.adapter.external.dto.toEntity
import br.com.cashflow.usecase.congregation_management.port.CongregationManagementInputPort
import br.com.cashflow.usecase.tenant.port.TenantOutputPort
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private const val CNPJ_DIGITS_LENGTH = 14

@Service
class CongregationManagementService(
    private val congregationOutputPort: CongregationOutputPort,
    private val tenantOutputPort: TenantOutputPort,
) : CongregationManagementInputPort {
    override fun create(request: CongregationCreateRequest): Congregation {
        if (tenantOutputPort.findById(request.tenantId) == null) {
            throw BusinessException("Usuário não possui congregação vinculada!")
        }
        val entity = request.toEntity()
        validateRequiredFields(entity)
        val cnpjDigits = entity.cnpj
        if (!cnpjDigits.isNullOrBlank()) {
            validateCnpjFormat(cnpjDigits)
            if (congregationOutputPort.existsByCnpjExcludingId(cnpjDigits, null)) {
                throw ConflictException("Já existe uma congregação com este CNPJ")
            }
        }
        return congregationOutputPort.save(entity)
    }

    override fun update(
        id: UUID,
        request: CongregationUpdateRequest,
    ): Congregation {
        val existing =
            congregationOutputPort.findById(id)
                ?: throw ResourceNotFoundException("Congregação não encontrada")
        request.applyTo(existing)
        validateRequiredFields(existing)
        val cnpjDigits = existing.cnpj
        if (!cnpjDigits.isNullOrBlank()) {
            validateCnpjFormat(cnpjDigits)
            if (congregationOutputPort.existsByCnpjExcludingId(cnpjDigits, id)) {
                throw ConflictException("Já existe uma congregação com este CNPJ")
            }
        }
        return congregationOutputPort.save(existing)
    }

    override fun findById(id: UUID): Congregation? = congregationOutputPort.findById(id)

    override fun findAll(
        filter: CongregationFilter?,
        page: Int,
        size: Int,
    ): CongregationPage = congregationOutputPort.findAll(filter, page, size)

    override fun findListForDropdown(): List<Pair<UUID, String>> = congregationOutputPort.findAllOrderByNome().map { it.id!! to it.nome }

    override fun findSetoriais(): List<Pair<UUID, String>> = congregationOutputPort.findSetoriais().map { it.id!! to it.nome }

    @Transactional
    override fun delete(id: UUID) {
        if (congregationOutputPort.findById(id) == null) {
            throw ResourceNotFoundException("Congregação não encontrada")
        }
        try {
            congregationOutputPort.deleteById(id)
        } catch (error: DataIntegrityViolationException) {
            throw ConflictException(
                "Erro ao excluir a congregação. Verifique se não há registros dependentes.",
            )
        }
    }

    override fun isCnpjAvailable(
        cnpj: String,
        excludeId: UUID?,
    ): Boolean {
        val digits = CnpjValidator.clean(cnpj)
        if (digits.isBlank()) return true
        if (digits.length != CNPJ_DIGITS_LENGTH) {
            throw IllegalArgumentException("CNPJ deve ter exatamente 14 dígitos numéricos")
        }
        if (!CnpjValidator.isValid(digits)) {
            throw IllegalArgumentException("CNPJ inválido: dígitos verificadores não conferem")
        }
        return !congregationOutputPort.existsByCnpjExcludingId(digits, excludeId)
    }

    private fun validateRequiredFields(c: Congregation) {
        if (c.nome.isBlank()) throw BusinessException("O nome é obrigatório")
        if (c.logradouro.isBlank()) throw BusinessException("O logradouro é obrigatório")
        if (c.bairro.isBlank()) throw BusinessException("O bairro é obrigatório")
        if (c.numero.isBlank()) throw BusinessException("O número é obrigatório")
        if (c.cidade.isBlank()) throw BusinessException("A cidade é obrigatória")
        if (c.uf.isBlank()) throw BusinessException("A UF é obrigatória")
        if (c.cep.isBlank()) throw BusinessException("O CEP é obrigatório")
    }

    private fun validateCnpjFormat(cnpj: String) {
        if (cnpj.length != CNPJ_DIGITS_LENGTH) {
            throw BusinessException("CNPJ deve ter exatamente 14 dígitos numéricos")
        }
        if (!CnpjValidator.isValid(cnpj)) {
            throw BusinessException("CNPJ inválido: dígitos verificadores não conferem")
        }
    }
}
