package br.com.cashflow.usecase.maquina_management.adapter.external.dto

import br.com.cashflow.usecase.maquina.model.MaquinaComCongregacao
import br.com.cashflow.usecase.maquina_historico.model.MaquinaHistoricoItemModel

data class MaquinaResponse(
    val id: String,
    val maquinaId: String,
    val congregacaoId: String?,
    val congregacaoNome: String,
    val bancoId: String,
    val bancoNome: String,
    val departamentoId: String?,
    val departamentoNome: String?,
    val ativo: Boolean,
    val version: Long?,
    val createdAt: String,
    val updatedAt: String?,
)

data class MaquinaListResponse(
    val items: List<MaquinaResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)

data class MaquinaHistoricoResponse(
    val id: String,
    val maquinaId: String,
    val congregacaoId: String?,
    val congregacaoNome: String?,
    val departamentoId: String?,
    val departamentoNome: String?,
    val dataInicio: String,
    val dataFim: String?,
)

data class MaquinaOptionResponse(
    val id: String,
    val maquinaId: String,
    val congregacaoNome: String,
    val departamentoNome: String?,
)

fun MaquinaComCongregacao.toResponse(): MaquinaResponse =
    MaquinaResponse(
        id = id.toString(),
        maquinaId = maquinaId,
        congregacaoId = congregacaoId?.toString(),
        congregacaoNome = congregacaoNome,
        bancoId = bancoId?.toString() ?: "",
        bancoNome = bancoNome,
        departamentoId = departamentoId?.toString(),
        departamentoNome = departamentoNome,
        ativo = ativo,
        version = version,
        createdAt = createdAt?.toString() ?: "",
        updatedAt = updatedAt?.toString(),
    )

fun MaquinaHistoricoItemModel.toHistoricoResponse(): MaquinaHistoricoResponse =
    MaquinaHistoricoResponse(
        id = id.toString(),
        maquinaId = maquinaId.toString(),
        congregacaoId = congregacaoId?.toString(),
        congregacaoNome = congregacaoNome,
        departamentoId = departamentoId?.toString(),
        departamentoNome = departamentoNome,
        dataInicio = dataInicio.toString(),
        dataFim = dataFim?.toString(),
    )

fun MaquinaComCongregacao.toOptionResponse(): MaquinaOptionResponse =
    MaquinaOptionResponse(
        id = id.toString(),
        maquinaId = maquinaId,
        congregacaoNome = congregacaoNome,
        departamentoNome = departamentoNome,
    )
