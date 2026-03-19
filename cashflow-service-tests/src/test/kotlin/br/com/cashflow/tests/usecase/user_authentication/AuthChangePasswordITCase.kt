package br.com.cashflow.tests.usecase.user_authentication

import br.com.cashflow.commons.exception.BusinessException
import br.com.cashflow.commons.exception.WrongPasswordException
import br.com.cashflow.commons.tenant.TenantContext
import br.com.cashflow.tests.base.postgresql.PostgresqlBaseTest
import br.com.cashflow.tests.base.postgresql.annotations.SqlSetUp
import br.com.cashflow.tests.base.postgresql.annotations.SqlTearDown
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapAdminUserData
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapCommand
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapCongregationData
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapInputPort
import br.com.cashflow.usecase.bootstrap_management.port.BootstrapTenantData
import br.com.cashflow.usecase.user_authentication.port.AuthInputPort
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

@SqlSetUp(value = ["/db/scripts/tenant/load.sql"])
@SqlTearDown(value = ["/db/scripts/bootstrap/teardown.sql"])
@TestPropertySource(properties = ["app.bootstrap.secret=test-bootstrap-secret"])
class AuthChangePasswordITCase : PostgresqlBaseTest() {
    @Autowired
    private lateinit var bootstrapInputPort: BootstrapInputPort

    @Autowired
    private lateinit var authInputPort: AuthInputPort

    private fun bootstrapCommand(
        cnpj: String,
        adminEmail: String,
    ) = BootstrapCommand(
        tenant =
            BootstrapTenantData(
                tradeName = "Church",
                companyName = "Church Ltda",
                cnpj = cnpj,
                street = "Rua",
                number = "1",
                complement = null,
                neighborhood = "Centro",
                city = "São Paulo",
                state = "SP",
                zipCode = "01234567",
                phone = null,
                email = null,
                active = true,
            ),
        adminUser = BootstrapAdminUserData(nome = "Admin", email = adminEmail),
        firstCongregation =
            BootstrapCongregationData(
                nome = "Sede",
                cnpj = null,
                logradouro = "Rua",
                bairro = "Centro",
                numero = "1",
                cidade = "São Paulo",
                uf = "SP",
                cep = "01234567",
                email = null,
                telefone = null,
                ativo = true,
            ),
    )

    @Test
    fun should_ChangePasswordAndAllowLoginWithNewPassword_When_CurrentPasswordCorrect() {
        val adminEmail = "admin-changepw@example.com"
        val command = bootstrapCommand("66666666000196", adminEmail)

        val result = bootstrapInputPort.bootstrap("test-bootstrap-secret", command)
        val currentPassword = result.temporaryPassword
        val newPassword = "newPassword123"

        TenantContext.setSchema(result.tenantSchemaName)
        authInputPort.changePassword(adminEmail, currentPassword, newPassword)

        val loginResponse = authInputPort.login(adminEmail, newPassword)
        assertThat(loginResponse.token).isNotBlank()
        assertThat(loginResponse.usuario.email).isEqualTo(adminEmail)
    }

    @Test
    fun should_ThrowWrongPasswordException_When_CurrentPasswordIncorrect() {
        val adminEmail = "admin-wrongpw@example.com"
        val command = bootstrapCommand("77777777000197", adminEmail)

        val result = bootstrapInputPort.bootstrap("test-bootstrap-secret", command)

        TenantContext.setSchema(result.tenantSchemaName)
        assertThatThrownBy {
            authInputPort.changePassword(adminEmail, "wrong-current-password", "newPass123")
        }.isInstanceOf(WrongPasswordException::class.java)
            .hasMessageContaining("Senha atual incorreta")
    }

    @Test
    fun should_ThrowBusinessException_When_NewPasswordEqualsCurrentPassword() {
        val adminEmail = "admin-samepw@example.com"
        val command = bootstrapCommand("88888888000198", adminEmail)

        val result = bootstrapInputPort.bootstrap("test-bootstrap-secret", command)
        val currentPassword = result.temporaryPassword

        TenantContext.setSchema(result.tenantSchemaName)
        assertThatThrownBy {
            authInputPort.changePassword(adminEmail, currentPassword, currentPassword)
        }.isInstanceOf(BusinessException::class.java)
            .hasMessageContaining("A nova senha deve ser diferente da atual")
    }
}
