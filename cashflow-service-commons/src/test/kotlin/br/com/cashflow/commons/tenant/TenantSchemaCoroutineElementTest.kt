package br.com.cashflow.commons.tenant

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class TenantSchemaCoroutineElementTest {
    @AfterEach
    fun tearDown() {
        TenantContext.clear()
    }

    @Test
    fun should_ExposeSameSchemaOnNestedIoDispatcher_When_TenantCoroutineContextComposed() {
        TenantContext.setSchema("tenant_x")
        runBlocking(Dispatchers.IO + tenantCoroutineContext()) {
            withContext(Dispatchers.IO) {
                assertThat(TenantContext.getSchema()).isEqualTo("tenant_x")
            }
        }
    }

    @Test
    fun should_ExposeNullSchemaOnWorker_When_ParentSchemaWasClearedBeforeComposition() {
        TenantContext.clear()
        runBlocking(Dispatchers.IO + tenantCoroutineContext()) {
            withContext(Dispatchers.IO) {
                assertThat(TenantContext.getSchema()).isNull()
            }
        }
    }

    @Test
    fun should_PropagateSchemaThroughAsync_When_TenantCoroutineContextComposed() {
        TenantContext.setSchema("tenant_async")
        runBlocking(Dispatchers.IO + tenantCoroutineContext()) {
            val observed =
                async(Dispatchers.IO) {
                    TenantContext.getSchema()
                }.await()
            assertThat(observed).isEqualTo("tenant_async")
        }
    }
}
