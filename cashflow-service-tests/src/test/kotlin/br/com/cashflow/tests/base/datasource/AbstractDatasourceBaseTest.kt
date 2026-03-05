package br.com.cashflow.tests.base.datasource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

abstract class AbstractDatasourceBaseTest {
    @Autowired
    protected lateinit var jdbcTemplate: JdbcTemplate
}
