package br.com.cashflow.usecase.parametro_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilter
import br.com.cashflow.usecase.parametro.model.ParametroPage
import br.com.cashflow.usecase.parametro.port.ParametroOutputPort
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroCreateRequest
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroUpdateRequest
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.TipoParametro
import br.com.cashflow.usecase.parametro_management.port.ParametroManagementInputPort
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ParametroManagementService(
    private val parametroOutputPort: ParametroOutputPort,
) : ParametroManagementInputPort {
    override fun create(request: ParametroCreateRequest): Parametro {
        validateChave(request.chave)
        validateValor(request.valor)
        val chaveNormalizada = request.chave.trim().uppercase()
        if (parametroOutputPort.existsByChave(chaveNormalizada)) {
            throw ConflictException("Já existe um parâmetro com esta chave")
        }
        val entity = buildNewParametro(request, chaveNormalizada)
        applyValorByTipo(entity, request.tipo, request.valor)
        return parametroOutputPort.save(entity)
    }

    override fun update(
        id: UUID,
        request: ParametroUpdateRequest,
    ): Parametro {
        val existing =
            parametroOutputPort.findById(id)
                ?: throw ResourceNotFoundException("Parâmetro não encontrado")
        validateChave(request.chave)
        validateValor(request.valor)
        val chaveNormalizada = request.chave.trim().uppercase()
        if (existing.chave != chaveNormalizada && parametroOutputPort.existsByChaveExcludingId(chaveNormalizada, id)) {
            throw ConflictException("Já existe um parâmetro com esta chave")
        }
        existing.chave = chaveNormalizada
        existing.tipo = request.tipo.toDbTipo()
        existing.ativo = request.ativo
        clearAllValueFields(existing)
        applyValorByTipo(existing, request.tipo, request.valor)
        return parametroOutputPort.save(existing)
    }

    override fun findById(id: UUID): Parametro? = parametroOutputPort.findById(id)

    override fun findAll(
        filter: ParametroFilter?,
        page: Int,
        size: Int,
    ): ParametroPage = parametroOutputPort.findWithFilters(filter, page, size)

    override fun findChavesForDropdown(): List<Pair<String, String>> = parametroOutputPort.findAllOrderByChave().map { it.chave to it.chave }

    @Transactional
    override fun delete(id: UUID) {
        val existing =
            parametroOutputPort.findById(id)
                ?: throw ResourceNotFoundException("Parâmetro não encontrado")
        try {
            parametroOutputPort.deleteById(id)
        } catch (error: DataIntegrityViolationException) {
            throw BusinessException(
                "Erro ao excluir o parâmetro. Verifique se não há registros dependentes.",
            )
        }
    }

    private fun validateChave(chave: String) {
        if (!chave.isNotBlank()) {
            throw BusinessException("A chave é obrigatória")
        }
    }

    private fun validateValor(valor: String) {
        if (!valor.isNotBlank()) {
            throw BusinessException("O valor é obrigatório")
        }
    }

    private fun buildNewParametro(
        request: ParametroCreateRequest,
        chaveNormalizada: String,
    ): Parametro =
        Parametro(
            chave = chaveNormalizada,
            tipo = request.tipo.toDbTipo(),
            ativo = request.ativo,
        )

    private fun applyValorByTipo(
        entity: Parametro,
        tipo: TipoParametro,
        valor: String,
    ) {
        when (tipo) {
            TipoParametro.TEXTO -> {
                entity.valorTexto = valor
                entity.valorInteiro = null
                entity.valorDecimal = null
            }
            TipoParametro.INTEIRO -> {
                val parsed =
                    valor.trim().toLongOrNull()
                        ?: throw BusinessException("Valor deve ser numérico para o tipo informado")
                entity.valorTexto = null
                entity.valorInteiro = parsed
                entity.valorDecimal = null
            }
            TipoParametro.DECIMAL -> {
                val parsed =
                    valor.trim().replace(',', '.').toDoubleOrNull()
                        ?: throw BusinessException("Valor deve ser numérico para o tipo informado")
                entity.valorTexto = null
                entity.valorInteiro = null
                entity.valorDecimal = parsed
            }
        }
    }

    private fun clearAllValueFields(entity: Parametro) {
        entity.valorTexto = null
        entity.valorInteiro = null
        entity.valorDecimal = null
    }
}
