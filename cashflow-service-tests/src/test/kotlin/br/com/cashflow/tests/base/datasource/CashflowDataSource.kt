package br.com.cashflow.tests.base.datasource

import br.com.cashflow.tests.base.dockerimages.BaseContainers
import org.testcontainers.containers.PostgreSQLContainer

abstract class CashflowDataSource : AbstractDatasourceBaseTest() {
    companion object {
        val POSTGRES_CONTAINER: PostgreSQLContainer<*> = BaseContainers.postgreSQLContainer()
    }
}
