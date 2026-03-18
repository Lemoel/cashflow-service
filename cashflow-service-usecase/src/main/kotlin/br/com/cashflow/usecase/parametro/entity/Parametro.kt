package br.com.cashflow.usecase.parametro.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "parametro")
@EntityListeners(AuditingEntityListener::class)
class Parametro(
    @Id
    var id: UUID? = null,

    @Column(name = "chave")
    var chave: String = "",

    @Column(name = "valor_texto")
    var valorTexto: String? = null,

    @Column(name = "valor_inteiro")
    var valorInteiro: Long? = null,

    @Column(name = "valor_decimal")
    var valorDecimal: Double? = null,

    @Column(name = "tipo")
    var tipo: String = "",

    @Column(name = "ativo")
    var ativo: Boolean = true,

    @CreatedBy
    @Column(name = "creation_user_id")
    var creationUserId: String = "",

    @LastModifiedBy
    @Column(name = "mod_user_id")
    var modUserId: String? = null,

    @CreatedDate
    @Column(name = "created_at")
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: Instant? = null,
) {
    @PrePersist
    fun onPrePersist() {
        if (id == null) id = UUID.randomUUID()
    }
}
