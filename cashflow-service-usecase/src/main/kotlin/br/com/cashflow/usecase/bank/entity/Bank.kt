package br.com.cashflow.usecase.bank.entity

import br.com.cashflow.commons.audit.Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "banco")
class Bank(
    @Id
    var id: UUID? = null,

    @Column(name = "nome")
    var nome: String? = null,

    @Column(name = "codigo")
    var codigo: String = "",

    @Column(name = "endereco_completo")
    var enderecoCompleto: String = "",

    @Column(name = "tipo_integracao")
    var tipoIntegracao: String = "",

    @Column(name = "ativo")
    var ativo: Boolean = true,
) : Auditable<String>()
