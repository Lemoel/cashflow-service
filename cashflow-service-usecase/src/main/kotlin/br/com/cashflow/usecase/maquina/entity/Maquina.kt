package br.com.cashflow.usecase.maquina.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "maquina")
@EntityListeners(AuditingEntityListener::class)
class Maquina(
    @Id
    var id: UUID? = null,

    @Column(name = "numero_serie_leitor")
    var numeroSerieLeitor: String? = null,

    @Column(name = "congregacao_id")
    var congregacaoId: UUID? = null,

    @Column(name = "banco_id")
    var bancoId: UUID? = null,

    @Column(name = "departamento_id")
    var departamentoId: UUID? = null,

    @Column(name = "ativo")
    var ativo: Boolean = true,

    @Version
    @Column(name = "version")
    var version: Long? = null,

    @Column(name = "created_at")
    var createdAt: Instant? = null,

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
