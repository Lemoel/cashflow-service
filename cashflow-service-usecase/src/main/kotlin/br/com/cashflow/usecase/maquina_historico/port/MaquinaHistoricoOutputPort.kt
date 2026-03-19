package br.com.cashflow.usecase.maquina_historico.port

import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItemModel
import java.util.UUID

interface MaquinaHistoricoOutputPort {
    fun listarPorMaquinaId(maquinaId: UUID): List<MaquinaHistoricoItemModel>

    fun deletarPorMaquinaId(maquinaId: UUID)

    fun fecharPeriodoAtual(maquinaId: UUID)

    fun inserirPeriodo(
        maquinaId: UUID,
        congregacaoId: UUID?,
        departamentoId: UUID?,
    )
}
