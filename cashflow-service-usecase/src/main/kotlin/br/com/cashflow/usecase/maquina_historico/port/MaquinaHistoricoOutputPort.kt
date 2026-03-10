package br.com.cashflow.usecase.maquina_historico.port

import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItem
import java.util.UUID

interface MaquinaHistoricoOutputPort {
    fun listarPorMaquinaId(maquinaId: UUID): List<MaquinaHistoricoItem>

    fun fecharPeriodoAtual(maquinaId: UUID)

    fun inserirPeriodo(
        maquinaId: UUID,
        congregacaoId: UUID?,
        departamentoId: UUID?,
    )
}
