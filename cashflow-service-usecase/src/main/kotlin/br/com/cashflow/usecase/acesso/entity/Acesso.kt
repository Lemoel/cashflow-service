package br.com.cashflow.usecase.acesso.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table(value = "acesso", schema = "eventos")
class Acesso(
    @Id
    var email: String? = null,

    @Column("password")
    var password: String = "",

    @Column("data")
    var data: Instant? = null,

    @Column("mod_date_time")
    var modDateTime: Instant? = null,

    @Column("nome")
    var nome: String? = null,

    @Column("telefone")
    var telefone: String? = null,

    @Column("ativo")
    var ativo: Boolean = true,

    @Column("tipo_acesso")
    var tipoAcesso: String = PerfilUsuario.USER.name,

) {
    fun perfil(): PerfilUsuario = PerfilUsuario.entries.find { it.name == tipoAcesso } ?: PerfilUsuario.USER
}
