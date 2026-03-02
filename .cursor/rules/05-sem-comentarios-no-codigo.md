# Sem Comentários no Código

## Regra

**Nenhum comentário deve ser adicionado ao código-fonte**, nem no backend nem no frontend. O código deve ser autoexplicativo.

- **Backend (Kotlin):** Não usar `//`, `/* */` ou `/** */` para comentários em código de produção.
- **Frontend (TypeScript/React):** Não usar `//`, `/* */` ou `/** */` para comentários em código de produção.

## Exceções

- **Documentação de API (OpenAPI/Swagger):** Anotações como `@Operation`, `@ApiResponse` ou equivalentes para documentar endpoints são permitidas quando exigidas pelo framework.
- **Arquivos de configuração:** Comentários em `application.yml`, `.env.example`, `tailwind.config`, etc., quando necessários para orientar configuração.

## Motivação

O código deve ser autoexplicativo. Nomes claros de variáveis, funções e classes eliminam a necessidade de comentários. Comentários desatualizam e podem induzir a erros.
