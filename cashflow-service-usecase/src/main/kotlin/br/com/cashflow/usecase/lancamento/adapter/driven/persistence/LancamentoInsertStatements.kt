package br.com.cashflow.usecase.lancamento.adapter.driven.persistence

internal object LancamentoInsertStatements {
    const val LANCAMENTO_INSERT_COLUMNS =
        "id, nsu, tid, codigo_transacao, parcela, tipo_evento, meio_captura, valor_parcela, " +
            "meio_pagamento, estabelecimento, pagamento_prazo, taxa_intermediacao, " +
            "numero_serie_leitor, valor_total_transacao, data_inicial_transacao, hora_inicial_transacao, " +
            "data_prevista_pagamento, valor_liquido_transacao, valor_original_transacao, " +
            "maquina_id, congregacao_id, departamento_id, " +
            "created_by_id, dti_created_date, last_modified_by_id, dti_last_modified_date"

    const val LANCAMENTO_ON_CONFLICT_IGNORE_DUPLICATES =
        "ON CONFLICT (codigo_transacao, tipo_evento, parcela) DO NOTHING"

    const val INSERT_ONE_IGNORING_DUPLICATES_NATIVE =
        "INSERT INTO lancamento (" + LANCAMENTO_INSERT_COLUMNS + ") VALUES (" +
            "gen_random_uuid(), " +
            ":nsu, :tid, :codigoTransacao, :parcela, :tipoEvento, :meioCaptura, :valorParcela, " +
            ":meioPagamento, :estabelecimento, :pagamentoPrazo, :taxaIntermediacao, " +
            ":numeroSerieLeitor, :valorTotalTransacao, :dataInicialTransacao, :horaInicialTransacao, " +
            ":dataPrevistaPagamento, :valorLiquidoTransacao, :valorOriginalTransacao, " +
            ":maquinaId, :congregacaoId, :departamentoId, " +
            ":createdBy, CURRENT_TIMESTAMP, :lastModifiedBy, CURRENT_TIMESTAMP" +
            ") " + LANCAMENTO_ON_CONFLICT_IGNORE_DUPLICATES
}
