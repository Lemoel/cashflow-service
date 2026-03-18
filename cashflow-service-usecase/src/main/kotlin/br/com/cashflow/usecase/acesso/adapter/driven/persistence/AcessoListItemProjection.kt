package br.com.cashflow.usecase.acesso.adapter.driven.persistence

import java.time.Instant
import java.util.UUID

interface AcessoListItemProjection {
    fun getEmail(): String

    fun getNome(): String?

    fun getTelefone(): String?

    fun getTipoAcesso(): String

    fun getAtivo(): Boolean

    fun getCreatedDate(): Instant?

    fun getLastModifiedDate(): Instant?

    fun getCongregacaoId(): UUID?

    fun getCongregacaoNome(): String?
}
