package br.com.cashflow.tests.base.dockerimages

import org.testcontainers.containers.PostgreSQLContainer

object BaseContainers {
    fun postgreSQLContainer(): PostgreSQLContainer<*> =
        PostgreSQLContainer(DockerImageNames.POSTGRES_IMAGE)
            .withDatabaseName("cashflow_service_test")
            .withUsername("test")
            .withPassword("test")
}
