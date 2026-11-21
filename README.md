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

## Primeiro acesso

1. Defina no `.env` as variáveis `GOERP_ADMIN_EMAIL` e `GOERP_ADMIN_SENHA` (e opcionalmente `GOERP_ADMIN_NOME`).
2. Na primeira inicialização é criado o **administrador da plataforma** (se ainda não existir; nas seguintes, nada muda).
3. Entre com ele (`POST /api/v1/autenticacao/login`) e **troque a senha** (`POST /api/v1/autenticacao/senha`).
4. Cadastre as empresas clientes em `POST /api/v1/empresas`, informando o primeiro administrador de cada uma.
   A empresa já nasce com o perfil Administrador e esse usuário com acesso total a ela.

O administrador da plataforma gerencia empresas, mas **não enxerga os dados internos** delas.

## Backup

O serviço `backup` do `docker-compose` faz um `pg_dump` compactado **todo dia às 3h** (configurável em `BACKUP_AGENDA`)
e mantém os últimos **7 dias, 4 semanas e 6 meses** em `./backups` (`last/`, `daily/`, `weekly/`, `monthly/`).

```bash
scripts/backup-agora.sh                                   # backup imediato
scripts/restaurar-backup.sh backups/daily/goerp-AAAAMMDD.sql.gz   # restaura (substitui os dados atuais)
scripts/testar-backup.sh                                  # prova o ciclo backup → desastre → restauração
```

> **Importante:** copie a pasta `backups/` para fora do servidor (outro disco, nuvem). Backup guardado
> só no próprio servidor não protege contra a perda do servidor. Rode `scripts/testar-backup.sh`
> periodicamente: backup que nunca foi restaurado não é backup.

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

## Empresas (administrador da plataforma)

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/empresas` | `{cnpj, razaoSocial, nomeFantasia, administrador:{nome, email, senha}}` cria a empresa pronta para uso |
| GET | `/api/v1/empresas`, `/api/v1/empresas/{id}` | consulta |
| PUT | `/api/v1/empresas/{id}` | `{razaoSocial, nomeFantasia}` (o CNPJ não muda) |
| DELETE | `/api/v1/empresas/{id}` | inativa: ninguém entra nem renova sessão nela |
| POST | `/api/v1/empresas/{id}/reativar` | reativa |

Todas exigem a permissão `PLATAFORMA_ADMINISTRAR`.

## Cadastros

### Clientes (`CLIENTE_LER` / `CLIENTE_GERENCIAR`)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/clientes?busca=&ativo=&pagina=0&tamanho=20` | pesquisa paginada (nome, nome fantasia ou começo do CPF/CNPJ) |
| GET | `/api/v1/clientes/{id}` | consulta |
| POST | `/api/v1/clientes` | cadastra (o documento não muda depois) |
| PUT | `/api/v1/clientes/{id}` | altera; envie a `versao` lida (se outra pessoa alterou antes, responde **409**) |
| DELETE | `/api/v1/clientes/{id}` | inativa |
| POST | `/api/v1/clientes/{id}/reativar` | reativa |

### Fornecedores (`FORNECEDOR_LER` / `FORNECEDOR_GERENCIAR`)

Mesmas rotas e regras dos clientes, em `/api/v1/fornecedores`. A pesquisa também procura pela pessoa de contato.

### Unidades de medida e categorias (`PRODUTO_LER` / `PRODUTO_GERENCIAR`)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/unidades?ativa=` | unidades da empresa (toda empresa começa com UN, KG, G, L, ML, M, CX, PC, PCT, DZ) |
| POST | `/api/v1/unidades` | `{sigla, descricao, fracionavel}` (sigla de 1 a 6 letras/números; não muda depois) |
| PUT | `/api/v1/unidades/{id}` | `{descricao, fracionavel}` |
| DELETE / POST `.../reativar` | `/api/v1/unidades/{id}` | inativa / reativa |
| GET / POST / PUT / DELETE | `/api/v1/categorias[/{id}]` | categorias da empresa (nome único, sem diferenciar maiúsculas) |

### Produtos (`PRODUTO_LER` / `PRODUTO_GERENCIAR`)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/produtos?busca=&categoriaId=&ativo=&pagina=&tamanho=` | pesquisa por descrição, código interno ou começo do código de barras |
| GET | `/api/v1/produtos/{id}` | consulta (com unidade e categoria) |
| POST | `/api/v1/produtos` | cadastra: código interno (não muda), descrição (até 120), código de barras (GTIN), unidade, categoria, preço de venda (2 casas), custo (4 casas), NCM, CEST, origem, controle de estoque e estoque mínimo |
| PUT | `/api/v1/produtos/{id}` | altera; envie a `versao` lida (**409** se outra pessoa alterou antes). Toda mudança de preço fica no histórico |
| DELETE / POST `.../reativar` | `/api/v1/produtos/{id}` | inativa / reativa |
| GET | `/api/v1/produtos/codigo-barras/{gtin}` | leitura do código de barras (PDV): só produtos ativos |

## Estoque

O saldo só muda por movimentações, que **nunca são alteradas nem apagadas** (o banco recusa); erros se corrigem com estorno.
Custo médio ponderado. Movimentações do mesmo produto ao mesmo tempo esperam uma pela outra (saldo travado por produto).

| Método | Rota | Permissão | Descrição |
|---|---|---|---|
| POST | `/api/v1/estoque/entradas` | `ESTOQUE_MOVIMENTAR` | `{produtoId, quantidade, custoUnitario, documentoDeOrigem, observacao}` |
| POST | `/api/v1/estoque/saidas` | `ESTOQUE_MOVIMENTAR` | `{produtoId, quantidade, motivo}` (consumo, doação...) |
| POST | `/api/v1/estoque/ajustes` | `ESTOQUE_AJUSTAR` | `{produtoId, sentido: ENTRADA/SAIDA, quantidade, motivo}` |
| POST | `/api/v1/estoque/movimentacoes/{id}/estorno` | `ESTOQUE_AJUSTAR` | `{motivo}`; cada movimentação só pode ser estornada uma vez |
| GET | `/api/v1/estoque/saldos?busca=&aRepor=&pagina=&tamanho=` | `ESTOQUE_LER` | posição: saldo, mínimo, custo médio, valor em estoque; `aRepor=true` só o que está no mínimo ou abaixo |
| GET | `/api/v1/estoque/saldos/{produtoId}` | `ESTOQUE_LER` | saldo e custo médio |
| GET | `/api/v1/estoque/produtos/{produtoId}/extrato` | `ESTOQUE_LER` | extrato (kardex) paginado, do mais recente ao mais antigo, com o saldo após cada movimentação |
| GET | `/api/v1/estoque/movimentacoes/{id}` | `ESTOQUE_LER` | uma movimentação |
| GET | `/api/v1/estoque/conferencia` | `ESTOQUE_AJUSTAR` | recalcula os saldos pelas movimentações e aponta divergências (deve vir `consistente: true`) |
| GET / PUT | `/api/v1/estoque/configuracao` | `ESTOQUE_LER` / `ESTOQUE_AJUSTAR` | `{permiteEstoqueNegativo}` (padrão: não) |

### Inventário

Contagem física: só os produtos **contados** são ajustados (serve para inventário geral ou parcial). Ao finalizar, o saldo de cada
produto contado vira a quantidade contada, com ajustes de origem `INVENTARIO`, tudo numa única transação. Um inventário aberto por vez.

| Método | Rota | Permissão | Descrição |
|---|---|---|---|
| POST | `/api/v1/estoque/inventarios` | `ESTOQUE_AJUSTAR` | `{descricao}` abre |
| PUT | `/api/v1/estoque/inventarios/{id}/contagens/{produtoId}` | `ESTOQUE_MOVIMENTAR` ou `ESTOQUE_AJUSTAR` | `{quantidade}` registra ou corrige a contagem |
| POST | `/api/v1/estoque/inventarios/{id}/finalizar` | `ESTOQUE_AJUSTAR` | ajusta o estoque e devolve os ajustes |
| POST | `/api/v1/estoque/inventarios/{id}/cancelar` | `ESTOQUE_AJUSTAR` | descarta sem mexer no estoque |
| GET | `/api/v1/estoque/inventarios[/{id}]` | `ESTOQUE_LER` | consulta |

## Vendas e PDV

### Caixa (`CAIXA_OPERAR` / `CAIXA_GERENCIAR`)

Cada operador abre o próprio caixa (um aberto por vez). No fechamento informa quanto contou em cada forma de pagamento;
o sistema registra sobra ou falta. O operador só vê e fecha o próprio caixa; o gerente, todos.

| Método | Rota | Permissão | Descrição |
|---|---|---|---|
| POST | `/api/v1/caixas` | `CAIXA_OPERAR` | `{trocoInicial}` abre o caixa do usuário |
| GET | `/api/v1/caixas/meu` | `CAIXA_OPERAR` | caixa aberto do usuário, com o esperado por forma |
| GET | `/api/v1/caixas/{id}` | `CAIXA_OPERAR` (próprio) / `CAIXA_GERENCIAR` | consulta |
| GET | `/api/v1/caixas?situacao=` | `CAIXA_GERENCIAR` | todos os caixas |
| POST | `/api/v1/caixas/{id}/suprimentos` | `CAIXA_GERENCIAR` | `{valor, motivo}` reforço de troco |
| POST | `/api/v1/caixas/{id}/sangrias` | `CAIXA_GERENCIAR` | `{valor, motivo}` retirada (até o dinheiro disponível) |
| POST | `/api/v1/caixas/{id}/fechar` | `CAIXA_OPERAR` (próprio) / `CAIXA_GERENCIAR` | `{contado: {DINHEIRO: 250.00, PIX: 45.50, ...}}` |
