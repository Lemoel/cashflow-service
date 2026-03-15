package br.com.cashflow.usecase.movimento_api.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Table("movimento_api")
class MovimentoApi(
    @Id
    var id: UUID? = null,

    @Column("payload")
    val payload: String? = null,

    @Column("status")
    var status: StatusProcessamentoEnum = StatusProcessamentoEnum.RECEBIDO,

    @Column("pagina")
    val pagina: Int = 1,

    @Column("total_elementos")
    val totalElementos: Int = 0,

    @Column("total_paginas")
    val totalPaginas: Int = 1,

    @Column("data_leitura")
    val dataLeitura: LocalDate? = null,

    @Column("created_at")
    var createdAt: Instant? = null,

    @Column("updated_at")
    var updatedAt: Instant? = null,

    @Column("creation_user_id")
    val creationUserId: String = "",

    @Column("mod_user_id")
    val modUserId: String? = null,
)
