package br.com.cashflow.tests.base.postgresql.annotations

import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.jdbc.Sql

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
annotation class SqlSetUp(
    @get:AliasFor(annotation = Sql::class)
    val value: Array<String> = [],
)
