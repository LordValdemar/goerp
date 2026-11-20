# GoERP

ERP para pequenas e médias empresas: cadastros, estoque, vendas/PDV, compras, financeiro e fiscal.

**Stack:** Java 21 · Spring Boot 3 · PostgreSQL 16 · Flyway · Docker

## Pré-requisitos
- Java 21
- Docker e Docker Compose

## Rodando

Tudo via Docker (banco + aplicação):

```bash
cp .env.example .env
docker compose up -d --build
```

Ou só o banco no Docker e a aplicação local:

```bash
docker compose up -d db
./mvnw spring-boot:run
```

A API sobe em http://localhost:8080. Health check: http://localhost:8080/actuator/health

## Testes

```bash
./mvnw test
```

Os testes de integração usam Testcontainers, então o Docker precisa estar rodando.

## Endpoints disponíveis

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/clientes` | Lista paginada (`?page=0&size=20&sort=nome`) |
| GET | `/api/v1/clientes/{id}` | Busca por id |
| POST | `/api/v1/clientes` | Cria cliente |
| PUT | `/api/v1/clientes/{id}` | Atualiza cliente |
| DELETE | `/api/v1/clientes/{id}` | Inativa cliente |
