# make pre-push obrigatório

## Regra

Ao final de **qualquer** implementação no repositório, é **obrigatório** executar `make pre-push` e garantir que ele conclua com sucesso antes de considerar a tarefa concluída.

## Quando aplicar

- Ao final de uma **nova funcionalidade** (feature)
- Ao final de uma **correção de bug** (bugfix)
- Ao final de uma **refatoração** que altere código ou testes
- Ao final de alterações em **migrations**, **configuração** ou **testes**
- Em resumo: ao final de qualquer mudança que seja commitada

## O que o make pre-push faz

O target `pre-push` do Makefile executa, em ordem:

1. **check-editorconfig** – verifica existência de `.editorconfig`
2. **format** – `./gradlew ktlintFormat` (formatação Kotlin)
3. **lint** – `./gradlew ktlintCheck` (verificação ktlint)
4. **build** – `./gradlew clean build` (compilação e testes de todos os módulos)
5. **coverage-check** – `./gradlew jacocoTestCoverageVerification` (verificação de cobertura)

Se qualquer etapa falhar, o `make pre-push` falha e a implementação não deve ser considerada finalizada até que seja corrigida e o comando passe.

## Referência

- Makefile na raiz do repositório: target `pre-push`
- AGENTS.md: checklist ao finalizar implementação
