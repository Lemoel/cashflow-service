package br.com.cashflow.usecase.parametro_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.parametro.entity.Parametro
import br.com.cashflow.usecase.parametro.model.ParametroFilterModel
import br.com.cashflow.usecase.parametro.model.ParametroPageModel
import br.com.cashflow.usecase.parametro.port.ParametroOutputPort
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.EnumTipoParametro
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroCreateRequestDto
import br.com.cashflow.usecase.parametro_management.adapter.external.dto.ParametroUpdateRequestDto
import br.com.cashflow.usecase.parametro_management.port.ParametroManagementInputPort
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ParametroManagementService(
    private val parametroOutputPort: ParametroOutputPort,
) : ParametroManagementInputPort {
    @Transactional
    override fun create(request: ParametroCreateRequestDto): Parametro {
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

    @Transactional
    override fun update(
        id: UUID,
        request: ParametroUpdateRequestDto,
    ): Parametro {
        val existing =
            parametroOutputPort.findById(id)
                ?: throw ResourceNotFoundException("Parâmetro não encontrado")

        validateChave(request.chave)
        validateValor(request.valor)

        val chaveNormalizada = request.chave.trim().uppercase()

        if (existing.chave != chaveNormalizada &&
            parametroOutputPort.existsByChaveExcludingId(chaveNormalizada, id)
        ) {
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
        filter: ParametroFilterModel?,
        page: Int,
        size: Int,
    ): ParametroPageModel = parametroOutputPort.findWithFilters(filter, page, size)

    override fun findChavesForDropdown(): List<Pair<String, String>> = parametroOutputPort.findAllChaveOrderByChave().map { it to it }

    @Transactional
    override fun delete(id: UUID) {
        if (!parametroOutputPort.existsById(id)) {
            throw ResourceNotFoundException("Parâmetro não encontrado")
        }
        try {
            parametroOutputPort.deleteById(id)
        } catch (error: DataIntegrityViolationException) {
            throw BusinessException(
                "Erro ao excluir o parâmetro. Verifique se não há registros dependentes.",
            )
        }
    }

    private fun validateChave(chave: String) {
        if (chave.isBlank()) {
            throw BusinessException("A chave é obrigatória")
        }
    }

    private fun validateValor(valor: String) {
        if (valor.isBlank()) {
            throw BusinessException("O valor é obrigatório")
        }
    }

    private fun buildNewParametro(
        request: ParametroCreateRequestDto,
        chaveNormalizada: String,
    ): Parametro =
        Parametro(
            chave = chaveNormalizada,
            tipo = request.tipo.toDbTipo(),
            ativo = request.ativo,
        )

    private fun applyValorByTipo(
        entity: Parametro,
        tipo: EnumTipoParametro,
        valor: String,
    ) {
        when (tipo) {
            EnumTipoParametro.TEXTO -> {
                entity.valorTexto = valor
                entity.valorInteiro = null
                entity.valorDecimal = null
            }
            EnumTipoParametro.INTEIRO -> {
                val parsed =
                    valor.trim().toLongOrNull()
                        ?: throw BusinessException("Valor deve ser numérico para o tipo informado")
                entity.valorTexto = null
                entity.valorInteiro = parsed
                entity.valorDecimal = null
            }
            EnumTipoParametro.DECIMAL -> {
                val parsed =
                    valor.trim().replace(',', '.').toBigDecimalOrNull()
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
