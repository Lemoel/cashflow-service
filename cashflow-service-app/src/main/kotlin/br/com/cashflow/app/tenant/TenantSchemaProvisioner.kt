package br.com.cashflow.app.tenant

import br.com.cashflow.usecase.tenant_management.port.TenantSchemaProvisionerPort
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
@Profile("!test")
class TenantSchemaProvisioner(
    @Qualifier("dataSource") private val dataSource: DataSource,
) : TenantSchemaProvisionerPort {
    override fun provision(schemaName: String) {
        require(schemaName.matches(VALID_SCHEMA_NAME_REGEX)) { "Invalid schema name: $schemaName" }
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("CREATE SCHEMA IF NOT EXISTS $schemaName")
            }
        }
        val flyway =
            Flyway
                .configure()
                .dataSource(dataSource)
                .locations("classpath:db/tenant-migration")
                .schemas(schemaName)
                .load()
        flyway.migrate()
    }

    companion object {
        private val VALID_SCHEMA_NAME_REGEX = Regex("^[a-zA-Z0-9_]+$")
    }
}
