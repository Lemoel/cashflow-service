package br.com.cashflow.usecase.bank.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("banco")
class Bank(
    @Id
    var id: UUID? = null,

    @Column("nome")
    var nome: String? = null,

    @Column("codigo")
    var codigo: String = "",

    @Column("endereco_completo")
    var enderecoCompleto: String = "",

    @Column("tipo_integracao")
    var tipoIntegracao: String = "",

    @Column("ativo")
    var ativo: Boolean = true,
)
