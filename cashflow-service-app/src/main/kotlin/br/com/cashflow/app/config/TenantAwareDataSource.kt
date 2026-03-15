package br.com.cashflow.app.config

import br.com.cashflow.commons.tenant.TenantContext
import java.sql.Connection
import javax.sql.DataSource

class TenantAwareDataSource(
    private val delegate: DataSource,
) : DataSource by delegate {
    override fun getConnection(): Connection {
        val connection = delegate.connection
        TenantContext.getSchema()?.let { schema ->
            require(schema.matches(VALID_SCHEMA_NAME_REGEX)) { "Invalid schema name: $schema" }
            connection.createStatement().use { stmt ->
                stmt.execute("SET search_path TO $schema, core, public")
            }
        }
        return connection
    }

    override fun getConnection(
        username: String?,
        password: String?,
    ): Connection {
        val connection = delegate.getConnection(username, password)
        TenantContext.getSchema()?.let { schema ->
            require(schema.matches(VALID_SCHEMA_NAME_REGEX)) { "Invalid schema name: $schema" }
            connection.createStatement().use { stmt ->
                stmt.execute("SET search_path TO $schema, core, public")
            }
        }
        return connection
    }

    companion object {
        private val VALID_SCHEMA_NAME_REGEX = Regex("^[a-zA-Z0-9_]+$")
    }
}
