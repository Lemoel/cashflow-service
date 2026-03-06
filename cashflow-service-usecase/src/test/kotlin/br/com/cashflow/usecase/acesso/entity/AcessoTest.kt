package br.com.cashflow.usecase.acesso.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AcessoTest {
    @Test
    fun `perfil returns ADMIN when tipoAcesso is ADMIN`() {
        val acesso = Acesso(email = "a@b.com", tipoAcesso = PerfilUsuario.ADMIN.name)
        assertThat(acesso.perfil()).isEqualTo(PerfilUsuario.ADMIN)
    }

    @Test
    fun `perfil returns USER when tipoAcesso is USER`() {
        val acesso = Acesso(email = "u@b.com", tipoAcesso = PerfilUsuario.USER.name)
        assertThat(acesso.perfil()).isEqualTo(PerfilUsuario.USER)
    }

    @Test
    fun `perfil returns FISCAL when tipoAcesso is FISCAL`() {
        val acesso = Acesso(email = "f@b.com", tipoAcesso = PerfilUsuario.FISCAL.name)
        assertThat(acesso.perfil()).isEqualTo(PerfilUsuario.FISCAL)
    }

    @Test
    fun `perfil returns GESTOR when tipoAcesso is GESTOR`() {
        val acesso = Acesso(email = "g@b.com", tipoAcesso = PerfilUsuario.GESTOR.name)
        assertThat(acesso.perfil()).isEqualTo(PerfilUsuario.GESTOR)
    }

    @Test
    fun `perfil returns USER when tipoAcesso is unknown or empty`() {
        val acessoUnknown = Acesso(email = "x@b.com", tipoAcesso = "UNKNOWN")
        assertThat(acessoUnknown.perfil()).isEqualTo(PerfilUsuario.USER)

        val acessoEmpty = Acesso(email = "y@b.com", tipoAcesso = "")
        assertThat(acessoEmpty.perfil()).isEqualTo(PerfilUsuario.USER)
    }
}
