package br.com.cashflow.app.tenant

import br.com.cashflow.usecase.tenant.port.TenantOutputPort
import jakarta.annotation.PostConstruct
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
@Profile("!test")
class TenantFlywayMigrator(
    @Qualifier("dataSource") private val dataSource: DataSource,
    private val tenantOutputPort: TenantOutputPort,
) {
    @PostConstruct
    fun migrateAllTenantSchemas() {
        tenantOutputPort.findAllSchemaNames().forEach { schemaName ->
            Flyway
                .configure()
                .dataSource(dataSource)
                .locations("classpath:db/tenant-migration")
                .schemas(schemaName)
                .load()
                .migrate()
        }
    }
}
