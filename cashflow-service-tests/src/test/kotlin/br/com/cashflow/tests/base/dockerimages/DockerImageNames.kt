package br.com.cashflow.tests.base.dockerimages

import org.testcontainers.utility.DockerImageName

object DockerImageNames {
    val POSTGRES_IMAGE: DockerImageName = DockerImageName.parse("postgres:16-alpine")
}
