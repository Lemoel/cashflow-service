package br.com.cashflow.app.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class DataSourceConfig {
    @Bean("dataSource")
    fun rawDataSource(properties: DataSourceProperties): DataSource = properties.initializeDataSourceBuilder().build()

    @Bean
    @Primary
    fun tenantAwareDataSource(
        @Qualifier("dataSource") target: DataSource,
    ): DataSource = TenantAwareDataSource(target)
}
