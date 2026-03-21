package br.com.cashflow.tests.usecase.tenant_context.service

import br.com.cashflow.commons.tenant.TenantContext
import br.com.cashflow.commons.tenant.tenantCoroutineContext
import br.com.cashflow.tests.base.postgresql.PostgresqlBaseTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TenantContextCoroutinePropagationIT : PostgresqlBaseTest() {
    @Test
    fun should_QueryTenantTableOnIoDispatcher_When_TenantCoroutineContextComposed() {
        runBlocking(Dispatchers.IO + tenantCoroutineContext()) {
            withContext(Dispatchers.IO) {
                val count =
                    jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM movimento_api",
                        Int::class.java,
                    )
                assertThat(count).isNotNull.isGreaterThanOrEqualTo(0)
            }
        }
    }

    @Test
    fun should_LeaveSchemaUnsetOnIoWorker_When_TenantCoroutineContextOmitted() {
        runBlocking(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                assertThat(TenantContext.getSchema()).isNull()
            }
        }
    }
}
