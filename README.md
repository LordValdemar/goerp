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

### Venda no PDV

O PDV envia o carrinho inteiro e os pagamentos de uma vez. Preço, código e descrição vêm do cadastro (o PDV não define preço).
Numa única transação: confere caixa, produtos, desconto e pagamentos, numera e baixa o estoque. Se faltar estoque de
qualquer item, nada é gravado (nem o número). Só o dinheiro gera troco.

| Método | Rota | Permissão | Descrição |
|---|---|---|---|
| POST | `/api/v1/vendas` | `CAIXA_OPERAR` | `{itens:[{produtoId ou codigoDeBarras, quantidade, desconto}], descontoNaVenda, pagamentos:[{forma, valor}], clienteId, cpfNaNota}` |
| GET | `/api/v1/vendas/{id}` | `VENDA_LER` ou `CAIXA_OPERAR` | consulta (cupom) |
| POST | `/api/v1/vendas/{id}/cancelar` | `VENDA_CANCELAR` | `{motivo}`; só com o caixa da venda aberto: devolve o estoque e tira o valor do caixa |
| GET | `/api/v1/vendas?caixaId=&data=&situacao=&pagina=&tamanho=` | `VENDA_LER` | pesquisa (data no fuso `GOERP_FUSO_HORARIO`, padrão America/Sao_Paulo) |
| GET | `/api/v1/vendas/resumo?data=&caixaId=` | `VENDA_LER` | quantidade, total, descontos, ticket médio, recebido por forma, cancelamentos |
| GET / PUT | `/api/v1/vendas/configuracao` | `CAIXA_OPERAR` / `CAIXA_GERENCIAR` | `{descontoMaximoPercentual}` (padrão 10%) |

Desconto exige a permissão `VENDA_DESCONTO` e respeita o máximo configurado. Formas: `DINHEIRO`, `CARTAO_DEBITO`, `CARTAO_CREDITO`, `PIX`.

### Pedido de venda (`PEDIDO_LER` / `PEDIDO_GERENCIAR` / `PEDIDO_FATURAR`)

Para cliente cadastrado: **orçamento → confirmado → faturado** (cancelável antes de faturar). O orçamento tem validade e pode ser
alterado (os preços são recopiados do cadastro); confirmado, fica congelado. O faturamento baixa o estoque na mesma transação.

| Método | Rota | Permissão | Descrição |
|---|---|---|---|
| POST | `/api/v1/pedidos-venda` | `PEDIDO_GERENCIAR` | `{clienteId, itens, desconto, validade, observacoes}` cria o orçamento |
| PUT | `/api/v1/pedidos-venda/{id}` | `PEDIDO_GERENCIAR` | altera o orçamento (envie a `versao` lida; 409 se mudou) |
| POST | `/api/v1/pedidos-venda/{id}/confirmar` | `PEDIDO_GERENCIAR` | só dentro da validade |
| POST | `/api/v1/pedidos-venda/{id}/faturar` | `PEDIDO_FATURAR` | baixa o estoque (origem `PEDIDO_DE_VENDA`) |
| POST | `/api/v1/pedidos-venda/{id}/cancelar` | `PEDIDO_GERENCIAR` | `{motivo}`; antes de faturar |
| GET | `/api/v1/pedidos-venda?clienteId=&situacao=` e `/{id}` | `PEDIDO_LER` | consulta |

## Compras

### Pedido de compra (`COMPRA_LER` / `COMPRA_GERENCIAR`)

**Rascunho → enviado → parcialmente recebido → recebido**. Cancelável enquanto nada chegou; com recebimento parcial, o saldo pode
ser encerrado. Só o rascunho é alterado. Sem custo informado, o item usa o custo do cadastro do produto.

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/pedidos-compra` | `{fornecedorId, itens:[{produtoId, quantidade, custoNegociado}], previsaoDeEntrega, observacoes}` |
| PUT | `/api/v1/pedidos-compra/{id}` | altera o rascunho (envie a `versao` lida) |
| POST | `/api/v1/pedidos-compra/{id}/enviar` | envia ao fornecedor (congela os itens) |
| POST | `/api/v1/pedidos-compra/{id}/cancelar` | `{motivo}`; só sem recebimento |
| POST | `/api/v1/pedidos-compra/{id}/encerrar-saldo` | `{motivo}`; o que falta deixa de ser esperado |
| GET | `/api/v1/pedidos-compra?fornecedorId=&situacao=` e `/{id}` | consulta (com `atrasado` e o pendente de cada item) |

### Recebimento (`COMPRA_RECEBER`)

Numa única transação: atualiza o que falta receber no pedido, dá entrada no estoque (origem `COMPRA`; o custo da nota entra no
custo médio) e registra o último custo de compra no cadastro do produto. Não recebe mais do que o pendente.
O estorno devolve o pendente ao pedido e estorna as entradas (recusado se a mercadoria já saiu).

| Método | Rota | Permissão | Descrição |
|---|---|---|---|
| POST | `/api/v1/pedidos-compra/{id}/recebimentos` | `COMPRA_RECEBER` | `{notaFiscal, itens:[{produtoId, quantidade, custoUnitario}]}` (sem custo, vale o negociado) |
| GET | `/api/v1/pedidos-compra/{id}/recebimentos` | `COMPRA_LER` | recebimentos do pedido |
| GET | `/api/v1/recebimentos/{id}` | `COMPRA_LER` | consulta |
| POST | `/api/v1/recebimentos/{id}/estornar` | `COMPRA_RECEBER` | `{motivo}`; uma vez só |

### Consultas de compras (`COMPRA_LER`)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/pedidos-compra/pendentes?fornecedorId=&atrasados=` | pedidos enviados ou parcialmente recebidos, pela previsão de entrega; cada um com o `valorPendente` (o que falta chegar × custo negociado), mais `valorAReceber` e quantos estão `atrasados` |
| GET | `/api/v1/recebimentos?de=&ate=&fornecedorId=&incluirEstornados=` | recebimentos do período (`de` e `ate` obrigatórios, inclusive, até 366 dias), com fornecedor e número do pedido; `total` e `totalPorFornecedor` nunca somam os estornados |

## Financeiro

Todo lançamento e todo pagamento registram a **forma de pagamento**: `DINHEIRO`, `PIX`, `CARTAO_DEBITO`, `CARTAO_CREDITO`,
`BOLETO`, `TRANSFERENCIA` ou `A_PRAZO`. A prazo não movimenta dinheiro na hora: gera um título (conta a receber/pagar), e o
pagamento dele registra a forma usada. O PDV aceita só as quatro formas conferidas no caixa.

### Contas financeiras e categorias (`FINANCEIRO_LER` / `CONTA_FINANCEIRA_GERENCIAR`)

Conta é onde o dinheiro está: `CAIXA` (cofre, tesouraria), `BANCO` ou `CONTA_DE_PAGAMENTO` (maquininha, carteira PIX). Nasce
com saldo zero (o saldo inicial entra como lançamento) e só é inativada com saldo zero. Saldo negativo só se a conta permitir
(ex.: cheque especial). Categorias são o plano de contas simples: `RECEITA` classifica entradas; `DESPESA`, saídas.

| Método | Rota | Descrição |
|---|---|---|
| GET/POST | `/api/v1/contas-financeiras` | `?ativa=`; `{tipo, nome, permiteSaldoNegativo}` |
| GET/PUT/DELETE | `/api/v1/contas-financeiras/{id}` | `PUT {nome, permiteSaldoNegativo, versao}` (409 se mudou); `DELETE` inativa |
| POST | `/api/v1/contas-financeiras/{id}/reativar` | |
| GET | `/api/v1/contas-financeiras/{id}/extrato?forma=&de=&ate=&pagina=&tamanho=` | lançamentos com o saldo após cada um, mais recentes primeiro |
| GET/POST | `/api/v1/categorias-financeiras` | `?tipo=&ativa=`; `{tipo, nome}` |
| GET/PUT/DELETE | `/api/v1/categorias-financeiras/{id}` | `PUT {nome}` (o tipo não muda); `DELETE` inativa |
| POST | `/api/v1/categorias-financeiras/{id}/reativar` | |

### Lançamentos (`LANCAMENTO_REGISTRAR`)

Lançamentos não são alterados nem apagados (trigger no banco); corrige-se por estorno, uma vez só, que mantém a forma e a
origem. Lançamentos simultâneos na mesma conta esperam a vez (trava na conta), então nunca se gasta o que não existe.

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/lancamentos-financeiros` | avulso: `{contaId, sentido: ENTRADA/SAIDA, valor, forma, categoriaId, descricao}` |
| GET | `/api/v1/lancamentos-financeiros/{id}` | `FINANCEIRO_LER` |
| POST | `/api/v1/lancamentos-financeiros/{id}/estornar` | `{motivo}`; avulso, ou transferência (estorna as duas pontas). Os de títulos e vendas são estornados por eles |
| POST | `/api/v1/transferencias-financeiras` | `{contaOrigemId, contaDestinoId, valor, forma (padrão TRANSFERENCIA), descricao}`: as duas pontas, ligadas pelo mesmo número |

### Contas a pagar (`FINANCEIRO_LER` / `TITULO_GERENCIAR` / `TITULO_BAIXAR`)

Cada parcela é uma conta (título). O pagamento tira o valor da conta financeira escolhida, com a **forma de pagamento usada**,
e gera o lançamento na mesma transação: sem saldo, nada é gravado. Pode ser parcial; juros e multa não abatem a dívida,
desconto abate. O estorno do pagamento devolve o valor devido e o dinheiro, juntos. Só se altera ou cancela sem pagamentos.
Lançar contas e pagá-las são permissões separadas.

| Método | Rota | Permissão | Descrição |
|---|---|---|---|
| POST | `/api/v1/contas-a-pagar` | `TITULO_GERENCIAR` | `{fornecedorId?, descricao, categoriaId?, documento?, formaPrevista?, condicao:{valor, primeiroVencimento, parcelas} ou {valor, vencimentos:[...]}}` |
| GET | `/api/v1/contas-a-pagar?fornecedorId=&situacao=&emAberto=&vencimentoDe=&vencimentoAte=&vencidas=` | `FINANCEIRO_LER` | por vencimento; cada uma com `vencido` e `diasEmAtraso` |
| GET | `/api/v1/contas-a-pagar/resumo` | `FINANCEIRO_LER` | em aberto, vencidas e que vencem hoje (quantidade e valor) |
| GET/PUT | `/api/v1/contas-a-pagar/{id}` | `FINANCEIRO_LER` / `TITULO_GERENCIAR` | `PUT {descricao, categoriaId, documento, valor, vencimento, versao}` |
| POST | `/api/v1/contas-a-pagar/{id}/cancelar` | `TITULO_GERENCIAR` | `{motivo}` |
| POST | `/api/v1/contas-a-pagar/{id}/pagamentos` | `TITULO_BAIXAR` | `{contaId, forma, valorPago, juros?, multa?, desconto?}` |
| POST | `/api/v1/contas-a-pagar/{id}/pagamentos/{numero}/estornar` | `TITULO_BAIXAR` | `{motivo}` |

### Contas a receber (`FINANCEIRO_LER` / `TITULO_GERENCIAR` / `TITULO_BAIXAR`)

Mesmo funcionamento das contas a pagar, com **cliente obrigatório** e categoria de receita. O recebimento põe o valor na
conta financeira com a **forma usada pelo cliente** (ex.: um crediário quitado parte em dinheiro, parte por PIX). Estornar
um recebimento exige que o dinheiro ainda esteja na conta. Cada rota só enxerga o seu lado: uma conta a receber pedida em
`/contas-a-pagar` dá 404, e vice-versa.

| Método | Rota | Permissão | Descrição |
|---|---|---|---|
| POST | `/api/v1/contas-a-receber` | `TITULO_GERENCIAR` | `{clienteId, descricao, categoriaId?, documento?, formaPrevista?, condicao:{...}}` (padrão: `A_PRAZO`) |
| GET | `/api/v1/contas-a-receber?clienteId=&situacao=&emAberto=&vencimentoDe=&vencimentoAte=&vencidas=` | `FINANCEIRO_LER` | `vencidas=true`: inadimplência |
| GET | `/api/v1/contas-a-receber/resumo` | `FINANCEIRO_LER` | em aberto, vencidas e que vencem hoje |
| GET/PUT | `/api/v1/contas-a-receber/{id}` | `FINANCEIRO_LER` / `TITULO_GERENCIAR` | |
| POST | `/api/v1/contas-a-receber/{id}/cancelar` | `TITULO_GERENCIAR` | `{motivo}` |
| POST | `/api/v1/contas-a-receber/{id}/recebimentos` | `TITULO_BAIXAR` | `{contaId, forma, valorPago, juros?, multa?, desconto?}` |
| POST | `/api/v1/contas-a-receber/{id}/recebimentos/{numero}/estornar` | `TITULO_BAIXAR` | `{motivo}` |
