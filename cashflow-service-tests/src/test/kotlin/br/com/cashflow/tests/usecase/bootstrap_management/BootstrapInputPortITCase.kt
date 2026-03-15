package br.com.cashflow.tests.usecase.bootstrap_management

import br.com.cashflow.commons.exception.ConflictException
import br.com.cashflow.commons.exception.InvalidBootstrapSecretException
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
class BootstrapInputPortITCase : PostgresqlBaseTest() {
    @Autowired
    private lateinit var bootstrapInputPort: BootstrapInputPort

    @Autowired
    private lateinit var authInputPort: AuthInputPort

    private fun validCommand(
        cnpj: String = "11111111000191",
        adminEmail: String = "admin-bootstrap@example.com",
    ) = BootstrapCommand(
        tenant =
            BootstrapTenantData(
                tradeName = "Bootstrap Church",
                companyName = "Bootstrap Church Ltda",
                cnpj = cnpj,
                street = "Rua Test",
                number = "100",
                complement = null,
                neighborhood = "Centro",
                city = "São Paulo",
                state = "SP",
                zipCode = "01234567",
                phone = null,
                email = null,
                active = true,
            ),
        adminUser = BootstrapAdminUserData(nome = "Admin Bootstrap", email = adminEmail),
        firstCongregation =
            BootstrapCongregationData(
                nome = "Sede Bootstrap",
                cnpj = null,
                logradouro = "Rua Test",
                bairro = "Centro",
                numero = "100",
                cidade = "São Paulo",
                uf = "SP",
                cep = "01234567",
                email = null,
                telefone = null,
                ativo = true,
            ),
    )

    @Test
    fun should_ReturnResultWithTenantIdAdminEmailTemporaryPasswordAndCongregationId_When_ValidSecretAndValidCommand() {
        val command = validCommand(cnpj = "11111111000191", adminEmail = "admin-it1@example.com")

        val result = bootstrapInputPort.bootstrap("test-bootstrap-secret", command)

        assertThat(result.tenantId).isNotNull()
        assertThat(result.tenantSchemaName).isEqualTo("tenant_11111111000191")
        assertThat(result.adminEmail).isEqualTo("admin-it1@example.com")
        assertThat(result.temporaryPassword).isNotBlank()
        assertThat(result.congregationId).isNotNull()
    }

    @Test
    fun should_ReturnTokens_When_LoginWithAdminEmailAndTemporaryPasswordAfterBootstrap() {
        val adminEmail = "admin-login@example.com"
        val command = validCommand(cnpj = "22222222000192", adminEmail = adminEmail)

        val result = bootstrapInputPort.bootstrap("test-bootstrap-secret", command)

        val loginResponse = authInputPort.login(result.adminEmail, result.temporaryPassword)

        assertThat(loginResponse.token).isNotBlank()
        assertThat(loginResponse.refreshToken).isNotBlank()
    }

    @Test
    fun should_ThrowInvalidBootstrapSecretException_When_SecretIsBlank() {
        val command = validCommand(cnpj = "33333333000193")

        assertThatThrownBy { bootstrapInputPort.bootstrap("", command) }
            .isInstanceOf(InvalidBootstrapSecretException::class.java)
    }

    @Test
    fun should_ThrowInvalidBootstrapSecretException_When_SecretIsWrong() {
        val command = validCommand(cnpj = "44444444000194")

        assertThatThrownBy { bootstrapInputPort.bootstrap("wrong-secret", command) }
            .isInstanceOf(InvalidBootstrapSecretException::class.java)
    }

    @Test
    fun should_ThrowConflictException_When_TenantCnpjAlreadyExists() {
        val cnpj = "55555555000195"
        val command = validCommand(cnpj = cnpj)
        bootstrapInputPort.bootstrap("test-bootstrap-secret", command)

        assertThatThrownBy { bootstrapInputPort.bootstrap("test-bootstrap-secret", command) }
            .isInstanceOf(ConflictException::class.java)
            .hasMessageContaining("CNPJ")
    }

    @Test
    fun should_ThrowIllegalArgumentException_When_TenantCnpjHasLessThan14Digits() {
        val command = validCommand(cnpj = "123")

        assertThatThrownBy { bootstrapInputPort.bootstrap("test-bootstrap-secret", command) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
