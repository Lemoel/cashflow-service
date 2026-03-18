package br.com.cashflow.usecase.movimento_api.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "movimento_api")
@EntityListeners(AuditingEntityListener::class)
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

    @CreatedDate
    @Column(name = "created_at")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: Instant? = null,

    @CreatedBy
    @Column(name = "creation_user_id")
    var creationUserId: String = "",

    @LastModifiedBy
    @Column(name = "mod_user_id")
    var modUserId: String? = null,
) {
    @PrePersist
    fun onPrePersist() {
        if (id == null) id = UUID.randomUUID()
    }
}
