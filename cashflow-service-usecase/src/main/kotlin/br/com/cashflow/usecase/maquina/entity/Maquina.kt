package br.com.cashflow.usecase.maquina.entity

import br.com.cashflow.commons.audit.Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.util.UUID

@Entity
@Table(name = "maquina")
class Maquina(

    @Id
    var id: UUID? = null,

    @Column(name = "numero_serie_leitor")
    var numeroSerieLeitor: String? = null,

    @Column(name = "congregacao_id")
    var congregacaoId: UUID? = null,

    @Column(name = "banco_id", nullable = false)
    var bancoId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000"),

    @Column(name = "departamento_id")
    var departamentoId: UUID? = null,

    @Column(name = "ativo")
    var ativo: Boolean = true,

    @Version
    @Column(name = "version")
    var version: Long? = null,
) : Auditable<String>() {
    @PrePersist
    fun onPrePersist() {
        if (id == null) id = UUID.randomUUID()
    }
}
