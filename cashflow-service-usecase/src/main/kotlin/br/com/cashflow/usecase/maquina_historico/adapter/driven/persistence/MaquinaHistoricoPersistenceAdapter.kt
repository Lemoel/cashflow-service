package br.com.cashflow.usecase.maquina_historico.adapter.driven.persistence

import br.com.cashflow.usecase.maquina_historico.entity.MaquinaHistorico
import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItemModel
import br.com.cashflow.usecase.maquina_historico.port.MaquinaHistoricoOutputPort
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class MaquinaHistoricoPersistenceAdapter(
    private val maquinaHistoricoRepository: MaquinaHistoricoRepository,
) : MaquinaHistoricoOutputPort {
    override fun listarPorMaquinaId(maquinaId: UUID): List<MaquinaHistoricoItemModel> =
        maquinaHistoricoRepository
            .findByMaquinaIdOrderByDataInicioDesc(maquinaId)
            .map { it.toItem() }

    override fun deletarPorMaquinaId(maquinaId: UUID) {
        maquinaHistoricoRepository.deleteByMaquinaId(maquinaId)
    }

    override fun fecharPeriodoAtual(maquinaId: UUID) {
        maquinaHistoricoRepository.fecharPeriodoAtual(maquinaId)
    }

    override fun inserirPeriodo(
        maquinaId: UUID,
        congregacaoId: UUID?,
        departamentoId: UUID?,
    ) {
        val entity =
            MaquinaHistorico(
                maquinaId = maquinaId,
                congregacaoId = congregacaoId,
                departamentoId = departamentoId,
                dataInicio = Instant.now(),
                dataFim = null,
            )
        maquinaHistoricoRepository.save(entity)
    }
}

private fun MaquinaHistoricoItemRow.toItem(): MaquinaHistoricoItemModel =
    MaquinaHistoricoItemModel(
        id = id,
        maquinaId = maquinaId,
        congregacaoId = congregacaoId,
        congregacaoNome = congregacaoNome,
        departamentoId = departamentoId,
        departamentoNome = departamentoNome,
        dataInicio = dataInicio,
        dataFim = dataFim,
    )
