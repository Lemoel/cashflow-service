package br.com.cashflow.usecase.acesso.entity

import br.com.cashflow.usecase.congregation.entity.Congregation
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "acesso")
class Acesso(
    @Id
    var email: String? = null,

    @Column(name = "password")
    var password: String = "",

    @Column(name = "data")
    var data: Instant? = null,

    @Column(name = "mod_date_time")
    var modDateTime: Instant? = null,

    @Column(name = "nome")
    var nome: String? = null,

    @Column(name = "telefone")
    var telefone: String? = null,

    @Column(name = "ativo")
    var ativo: Boolean = true,

    @Column(name = "tipo_acesso")
    var tipoAcesso: String = PerfilUsuario.USER.name,

    @ManyToMany
    @JoinTable(
        name = "acesso_congregacao",
        joinColumns = [JoinColumn(name = "email")],
        inverseJoinColumns = [JoinColumn(name = "congregacao_id")],
    )
    var congregacoes: MutableSet<Congregation> = mutableSetOf(),
) {
    fun perfil(): PerfilUsuario = PerfilUsuario.entries.find { it.name == tipoAcesso } ?: PerfilUsuario.USER
}
