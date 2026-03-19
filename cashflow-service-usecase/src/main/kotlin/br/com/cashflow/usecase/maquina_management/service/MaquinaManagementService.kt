package br.com.cashflow.usecase.maquina_management.service

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.ResourceNotFoundException
import br.com.cashflow.usecase.bank.entity.Bank
import br.com.cashflow.usecase.bank.port.BankOutputPort
import br.com.cashflow.usecase.congregation.entity.Congregation
import br.com.cashflow.usecase.congregation.port.CongregationOutputPort
import br.com.cashflow.usecase.department.entity.Department
import br.com.cashflow.usecase.department.port.DepartmentOutputPort
import br.com.cashflow.usecase.maquina.entity.Maquina
import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import br.com.cashflow.usecase.maquina.model.MaquinaPage
import br.com.cashflow.usecase.maquina.port.MaquinaOutputPort
import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItemModel
import br.com.cashflow.usecase.maquina_historico.port.MaquinaHistoricoOutputPort
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaCreateRequestDto
import br.com.cashflow.usecase.maquina_management.adapter.external.dto.MaquinaUpdateRequestDto
import br.com.cashflow.usecase.maquina_management.port.MaquinaManagementInputPort
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.util.Locale
import java.util.UUID

@Service
@Transactional(readOnly = true)
class MaquinaManagementService(
    private val maquinaOutputPort: MaquinaOutputPort,
    private val maquinaHistoricoOutputPort: MaquinaHistoricoOutputPort,
    private val congregationOutputPort: CongregationOutputPort,
    private val departmentOutputPort: DepartmentOutputPort,
    private val bankOutputPort: BankOutputPort,
) : MaquinaManagementInputPort {
    @Transactional
    override fun create(request: MaquinaCreateRequestDto): MaquinaComCongregacao {
        validarMaquinaIdObrigatorio(request.maquinaId)
        val refs = validateAndLoadReferences(request.congregacaoId, request.bancoId, request.departamentoId)
        val numeroSerieNormalizado = request.maquinaId.trim().uppercase(Locale.ROOT)

        if (maquinaOutputPort.existsByNumeroSerieLeitor(numeroSerieNormalizado)) {
            throw ConflictException("Já existe uma máquina com este ID")
        }

        val entity =
            Maquina(
                numeroSerieLeitor = numeroSerieNormalizado,
                congregacaoId = request.congregacaoId,
                bancoId = request.bancoId,
                departamentoId = request.departamentoId,
                ativo = request.ativo,
            )

        val saved = maquinaOutputPort.save(entity)

        maquinaHistoricoOutputPort.inserirPeriodo(
            saved.id!!,
            saved.congregacaoId,
            saved.departamentoId,
        )

        return buildMaquinaComCongregacao(saved, refs.congregation, refs.bank, refs.department)
    }

    @Transactional
    override fun update(
        id: UUID,
        request: MaquinaUpdateRequestDto,
    ): MaquinaComCongregacao {
        val existing =
            maquinaOutputPort.findById(id)
                ?: throw ResourceNotFoundException("Máquina não encontrada")
        val refs = validateAndLoadReferences(request.congregacaoId, request.bancoId, request.departamentoId)
        val congregacaoMudou = existing.congregacaoId != request.congregacaoId
        val departamentoMudou = existing.departamentoId != request.departamentoId
        if (congregacaoMudou || departamentoMudou) {
            maquinaHistoricoOutputPort.fecharPeriodoAtual(id)
        }
        existing.congregacaoId = request.congregacaoId
        existing.bancoId = request.bancoId
        existing.departamentoId = request.departamentoId
        existing.ativo = request.ativo
        val saved = maquinaOutputPort.save(existing)
        if (congregacaoMudou || departamentoMudou) {
            maquinaHistoricoOutputPort.inserirPeriodo(
                saved.id!!,
                saved.congregacaoId,
                saved.departamentoId,
            )
        }
        return buildMaquinaComCongregacao(saved, refs.congregation, refs.bank, refs.department)
    }

    override fun findById(id: UUID): MaquinaComCongregacao? = maquinaOutputPort.findByIdWithDetalhes(id)

    override fun search(
        maquinaId: String?,
        congregacao: String?,
        banco: String?,
        departamentoId: UUID?,
        page: Int,
        size: Int,
    ): MaquinaPage =
        maquinaOutputPort.findWithFiltersComDetalhes(
            maquinaId?.trim()?.takeIf { it.isNotBlank() },
            congregacao?.trim()?.takeIf { it.isNotBlank() },
            banco?.trim()?.takeIf { it.isNotBlank() },
            departamentoId,
            page,
            size,
        )

    override fun listForOptions(
        tenantId: UUID?,
        congregacaoId: UUID?,
        numeroSerie: String?,
        page: Int,
        size: Int,
    ): MaquinaPage =
        maquinaOutputPort.findParaSelecaoHistorico(
            tenantId,
            congregacaoId,
            numeroSerie?.trim()?.takeIf { it.isNotBlank() },
            page,
            size,
        )

    override fun listOrSearch(
        maquinaId: String?,
        congregacao: String?,
        banco: String?,
        departamentoId: UUID?,
        tenantId: UUID?,
        congregacaoId: UUID?,
        numeroSerie: String?,
        page: Int,
        size: Int,
    ): MaquinaPage {
        val hasSearchFilters =
            !maquinaId.isNullOrBlank() ||
                !congregacao.isNullOrBlank() ||
                !banco.isNullOrBlank() ||
                departamentoId != null
        return if (hasSearchFilters) {
            search(maquinaId, congregacao, banco, departamentoId, page, size)
        } else {
            listForOptions(tenantId, congregacaoId, numeroSerie, page, size)
        }
    }

    override fun listHistoricoByMaquinaId(maquinaId: UUID): List<MaquinaHistoricoItemModel> = maquinaHistoricoOutputPort.listarPorMaquinaId(maquinaId)

    @Transactional
    override fun delete(id: UUID) {
        if (maquinaOutputPort.findById(id) == null) {
            throw ResourceNotFoundException("Máquina não encontrada")
        }
        maquinaHistoricoOutputPort.deletarPorMaquinaId(id)
        try {
            maquinaOutputPort.deleteById(id)
        } catch (error: DataIntegrityViolationException) {
            throw ConflictException(
                "Erro ao excluir a máquina. Verifique se não há registros dependentes.",
            )
        }
    }

    private fun validarMaquinaIdObrigatorio(maquinaId: String?) {
        if (maquinaId.isNullOrBlank()) {
            throw BusinessException("O ID da máquina é obrigatório")
        }
    }

    private data class MaquinaReferences(
        val congregation: Congregation,
        val bank: Bank,
        val department: Department?,
    )

    private fun validateAndLoadReferences(
        congregacaoId: UUID?,
        bancoId: UUID?,
        departamentoId: UUID?,
    ): MaquinaReferences {
        if (congregacaoId == null) {
            throw BusinessException("É necessário selecionar uma congregação")
        }
        if (bancoId == null) {
            throw BusinessException("É necessário selecionar um banco")
        }
        val congregation =
            congregationOutputPort.findById(congregacaoId)
                ?: throw BusinessException("Congregação da máquina não encontrada")
        val bank =
            bankOutputPort.findById(bancoId)
                ?: throw BusinessException("Banco não encontrado")
        val department =
            departamentoId?.let { id ->
                departmentOutputPort.findById(id)
                    ?: throw BusinessException("Departamento não encontrado")
            }
        department?.let {
            if (it.tenantId != congregation.tenantId) {
                throw BusinessException(
                    "O departamento deve pertencer ao mesmo tenant da congregação da máquina",
                )
            }
        }
        return MaquinaReferences(congregation, bank, department)
    }

    private fun buildMaquinaComCongregacao(
        maquina: Maquina,
        congregation: Congregation,
        bank: Bank,
        department: Department?,
    ): MaquinaComCongregacao =
        MaquinaComCongregacao(
            id = maquina.id!!,
            maquinaId = maquina.numeroSerieLeitor ?: "",
            congregacaoId = maquina.congregacaoId,
            congregacaoNome = congregation.nome.ifBlank { "Não informada" },
            bancoId = maquina.bancoId,
            bancoNome = (bank.nome?.takeIf { it.isNotBlank() }) ?: "Não informado",
            departamentoId = maquina.departamentoId,
            departamentoNome = department?.nome,
            ativo = maquina.ativo,
            version = maquina.version,
            createdAt = maquina.createdDate?.atZone(ZoneId.systemDefault())?.toInstant(),
            updatedAt = maquina.lastModifiedDate?.atZone(ZoneId.systemDefault())?.toInstant(),
        )
}
