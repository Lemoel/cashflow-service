package br.com.cashflow.tests.config

import br.com.cashflow.usecase.tenant_management.port.TenantSchemaProvisionerPort
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import javax.sql.DataSource

@Configuration
class TestTenantConfig {
    companion object {
        const val TEST_SCHEMA_NAME = "tenant_test"
        val TEST_TENANT_ID = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")
    }

    @Bean
    @Primary
    fun tenantSchemaProvisioner(
        @Qualifier("dataSource") dataSource: DataSource,
    ): TenantSchemaProvisionerPort =
        object : TenantSchemaProvisionerPort {
            override fun provision(schemaName: String) {
                dataSource.connection.use { conn ->
                    conn.createStatement().use { stmt ->
                        stmt.execute("CREATE SCHEMA IF NOT EXISTS $schemaName")
                    }
                }
                Flyway
                    .configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/tenant-migration")
                    .schemas(schemaName)
                    .defaultSchema(schemaName)
                    .placeholders(mapOf("tenant_schema" to schemaName))
                    .load()
                    .migrate()
            }

            override fun dropSchema(schemaName: String) {
                dataSource.connection.use { conn ->
                    conn.createStatement().use { stmt ->
                        stmt.execute("DROP SCHEMA IF EXISTS $schemaName CASCADE")
                    }
                }
            }
        }

    @Bean
    @Order(1)
    fun testSchemaBootstrap(
        @Qualifier("dataSource") dataSource: DataSource,
        tenantSchemaProvisioner: TenantSchemaProvisionerPort,
    ): ApplicationRunner =
        ApplicationRunner {
            dataSource.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute(
                        """
                        INSERT INTO core.tenants (
                            id, cnpj, nome_fantasia, razao_social, logradouro, numero,
                            complemento, bairro, cidade, uf, cep, telefone, email, ativo,
                            created_by_id, dti_created_date, last_modified_by_id, dti_last_modified_date, schema_name
                        ) VALUES (
                            '$TEST_TENANT_ID', '12345678000190', 'Test', 'Test Razao',
                            'Rua', '1', NULL, 'Bairro', 'Cidade', 'SP', '01234567',
                            NULL, NULL, TRUE, 'test', CURRENT_TIMESTAMP, 'test', CURRENT_TIMESTAMP, '$TEST_SCHEMA_NAME'
                        ) ON CONFLICT (id) DO NOTHING
                        """.trimIndent(),
                    )
                }
            }
            tenantSchemaProvisioner.provision(TEST_SCHEMA_NAME)
        }
}
