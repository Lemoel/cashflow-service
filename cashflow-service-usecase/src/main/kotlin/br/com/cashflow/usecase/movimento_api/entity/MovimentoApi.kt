package br.com.cashflow.usecase.movimento_api.entity

import br.com.cashflow.commons.audit.Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "movimento_api")
class MovimentoApi(
    @Id
    var id: UUID? = null,

    @Column(name = "payload")
    val payload: String? = null,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var status: StatusProcessamentoEnum = StatusProcessamentoEnum.RECEBIDO,

    @Column(name = "pagina")
    val pagina: Int = 1,

    @Column(name = "total_elementos")
    val totalElementos: Int = 0,

    @Column(name = "total_paginas")
    val totalPaginas: Int = 1,

    @Column(name = "data_leitura")
    val dataLeitura: LocalDate? = null,
) : Auditable<String>() {
    @PrePersist
    fun onPrePersist() {
        if (id == null) id = UUID.randomUUID()
    }
}
