# Cashflow Service
.PHONY: run build test quality format lint coverage coverage-check pre-push check-editorconfig clean docker-build docker-push docker-run native-build native-run setup-hooks

# Desenvolvimento
run:
	./gradlew :cashflow-service-app:bootRun

build:
	./gradlew clean build

clean:
	./gradlew clean

# Qualidade
quality:
	./gradlew ktlintCheck test jacocoTestReport

coverage-check:
	./gradlew jacocoTestCoverageVerification

# format e lint usam .editorconfig (ktlint); format corrige, lint verifica
pre-push: check-editorconfig format lint build coverage-check

check-editorconfig:
	@test -f .editorconfig || (echo "Erro: .editorconfig nao encontrado." && exit 1)

format:
	./gradlew ktlintFormat

lint:
	./gradlew ktlintCheck

# Testes
test:
	./gradlew test

test-unit:
	./gradlew :cashflow-service-usecase:test

test-integration:
	./gradlew :cashflow-service-tests:test

coverage:
	./gradlew jacocoTestReport
	@echo "Relatorio em: cashflow-service-usecase/build/reports/jacoco/test/html/index.html"

# Docker (Jib)
docker-build:
	./gradlew :cashflow-service-app:jibDockerBuild

docker-push:
	./gradlew :cashflow-service-app:jib

docker-run:
	docker run -p 8080:8080 --env-file .env cashflow-service:latest

# Native Image (GraalVM)
native-build:
	./gradlew :cashflow-service-app:nativeCompile

native-run:
	./cashflow-service-app/build/native/nativeCompile/cashflow-service-app

# Git hooks
setup-hooks:
	./gradlew addKtlintCheckGitPreCommitHook
	@echo "Pre-commit hook instalado com sucesso"
