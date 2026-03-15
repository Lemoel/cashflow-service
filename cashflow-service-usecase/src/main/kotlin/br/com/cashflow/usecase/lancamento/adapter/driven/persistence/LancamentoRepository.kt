package br.com.cashflow.usecase.lancamento.adapter.driven.persistence

import br.com.cashflow.usecase.lancamento.entity.Lancamento
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.UUID

interface LancamentoRepository : CrudRepository<Lancamento, UUID> {
    /**
     * O que o fluxo precisa
     * Inserir o lançamento só se ainda não existir uma linha com o mesmo (codigo_transacao, tipo_evento, parcela).
     * Se já existir: não fazer nada e não dar erro (idempotência).
     */
    @Modifying
    @Query(
        """
        INSERT INTO lancamento (
            id, nsu, tid, codigo_transacao, parcela, tipo_evento, meio_captura, valor_parcela,
            meio_pagamento, estabelecimento, pagamento_prazo, taxa_intermediacao,
            numero_serie_leitor, valor_total_transacao, data_inicial_transacao, hora_inicial_transacao,
            data_prevista_pagamento, valor_liquido_transacao, valor_original_transacao,
            maquina_id, congregacao_id, departamento_id, creation_user_id, created_at
        ) VALUES (
            gen_random_uuid(),
            :#{#lancamento.nsu},
            :#{#lancamento.tid},
            :#{#lancamento.codigoTransacao},
            :#{#lancamento.parcela},
            :#{#lancamento.tipoEvento.name},
            :#{#lancamento.meioCaptura.name},
            :#{#lancamento.valorParcela},
            :#{#lancamento.meioPagamento.name},
            :#{#lancamento.estabelecimento},
            :#{#lancamento.pagamentoPrazo},
            :#{#lancamento.taxaIntermediacao},
            :#{#lancamento.numeroSerieLeitor},
            :#{#lancamento.valorTotalTransacao},
            :#{#lancamento.dataInicialTransacao},
            :#{#lancamento.horaInicialTransacao},
            :#{#lancamento.dataPrevistaPagamento},
            :#{#lancamento.valorLiquidoTransacao},
            :#{#lancamento.valorOriginalTransacao},
            :#{#lancamento.maquinaId},
            :#{#lancamento.congregacaoId},
            :#{#lancamento.departamentoId},
            :#{#lancamento.creationUserId},
            NOW()
        )
        ON CONFLICT (codigo_transacao, tipo_evento, parcela) DO NOTHING
        """,
    )
    fun insertIgnorandoDuplicata(
        @Param("lancamento") lancamento: Lancamento,
    )
}
