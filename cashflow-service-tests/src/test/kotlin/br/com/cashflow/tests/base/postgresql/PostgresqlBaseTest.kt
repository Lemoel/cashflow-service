package br.com.cashflow.tests.base.postgresql

import br.com.cashflow.app.CashflowApplication
import br.com.cashflow.tests.base.datasource.CashflowDataSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(classes = [CashflowApplication::class])
abstract class PostgresqlBaseTest : CashflowDataSource() {
    companion object {
        init {
            CashflowDataSource.POSTGRES_CONTAINER.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun setupProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", CashflowDataSource.POSTGRES_CONTAINER::getJdbcUrl)
            registry.add(
                "spring.datasource.username",
                CashflowDataSource.POSTGRES_CONTAINER::getUsername,
            )
            registry.add(
                "spring.datasource.password",
                CashflowDataSource.POSTGRES_CONTAINER::getPassword,
            )
            registry.add("spring.flyway.enabled") { true }
        }
    }
}
