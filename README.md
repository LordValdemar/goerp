# GoERP

ERP para pequenas e médias empresas: cadastros, estoque, vendas/PDV, compras, financeiro e fiscal.

**Stack:** Java 21 · Spring Boot 3 · PostgreSQL 16 · Flyway · Docker

## Pré-requisitos
- Java 21
- Docker e Docker Compose

## Rodando

Tudo via Docker (banco + aplicação):

```bash
cp .env.example .env   # e defina JWT_SECRET
docker compose up -d --build
```

Ou só o banco no Docker e a aplicação local:

```bash
docker compose up -d db
JWT_SECRET=uma-chave-de-pelo-menos-32-caracteres ./mvnw spring-boot:run
```

A API sobe em http://localhost:8080. Health check: http://localhost:8080/actuator/health

## Testes

```bash
./mvnw test
```

Os testes de integração usam Testcontainers, então o Docker precisa estar rodando.

## Autenticação

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/autenticacao/login` | `{email, senha, empresaId?}` → token de acesso (15 min) + token de renovação (7 dias). Se o usuário tem várias empresas e não informou `empresaId`, devolve a lista para escolher. |
| POST | `/api/v1/autenticacao/renovar` | `{tokenDeRenovacao}` → novo par de tokens (o antigo deixa de valer) |
| POST | `/api/v1/autenticacao/sair` | `{tokenDeRenovacao}` → encerra a sessão |
| GET | `/api/v1/autenticacao/eu` | dados do usuário do token atual |
| POST | `/api/v1/autenticacao/senha` | `{senhaAtual, novaSenha}` → troca a própria senha (encerra as sessões) |

Demais rotas exigem o cabeçalho `Authorization: Bearer <tokenDeAcesso>`.

## Usuários e perfis da empresa

| Método | Rota | Permissão | Descrição |
|---|---|---|---|
| GET | `/api/v1/usuarios` | `USUARIO_LER` | usuários com acesso à empresa |
| GET | `/api/v1/usuarios/{id}` | `USUARIO_LER` | um usuário da empresa |
| POST | `/api/v1/usuarios` | `USUARIO_GERENCIAR` | `{nome, email, senha, perfilId}`; se o e-mail já existe, só dá acesso (cabeçalho `X-Usuario-Ja-Existia: true`) |
| PUT | `/api/v1/usuarios/{id}/perfil` | `USUARIO_GERENCIAR` | `{perfilId}` troca o perfil |
| DELETE | `/api/v1/usuarios/{id}` | `USUARIO_GERENCIAR` | inativa o acesso à empresa e encerra as sessões |
| POST | `/api/v1/usuarios/{id}/reativar` | `USUARIO_GERENCIAR` | devolve o acesso |
| GET | `/api/v1/perfis`, `/api/v1/perfis/{id}` | `PERFIL_LER` | perfis da empresa |
| POST/PUT/DELETE | `/api/v1/perfis[/{id}]` | `PERFIL_GERENCIAR` | cria, altera, inativa perfil |
| GET | `/api/v1/permissoes` | `PERFIL_LER` | catálogo de permissões |
