package br.com.cashflow.usecase.parametro.entity

import br.com.cashflow.commons.audit.Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "parametro")
class Parametro(
    @Id
    var id: UUID? = null,

    @Column(name = "chave")
    var chave: String = "",

    @Column(name = "valor_texto")
    var valorTexto: String? = null,

    @Column(name = "valor_inteiro")
    var valorInteiro: Long? = null,

    @Column(name = "valor_decimal", precision = 15, scale = 4)
    var valorDecimal: BigDecimal? = null,

    @Column(name = "tipo")
    var tipo: String = "",

    @Column(name = "ativo")
    var ativo: Boolean = true,
) : Auditable<String>() {
    @PrePersist
    fun onPrePersist() {
        if (id == null) id = UUID.randomUUID()
    }
}
