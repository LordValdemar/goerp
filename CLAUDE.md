# GoERP

Sistema ERP para pequenas e médias empresas.

## Módulos
- **cadastros** – clientes, fornecedores, produtos, categorias, unidades de medida
- **estoque** – saldos, movimentações (entrada/saída/ajuste), inventário
- **vendas** – pedidos de venda e PDV (frente de caixa: abertura/fechamento de caixa, venda rápida, formas de pagamento)
- **compras** – pedidos de compra e recebimento de mercadorias (gera entrada no estoque e conta a pagar)
- **financeiro** – contas a pagar/receber, caixa e bancos, fluxo de caixa
- **fiscal** – emissão de NF-e/NFC-e, regras de tributação (ICMS, IPI, PIS, COFINS), NCM/CFOP

## Ordem de implementação
1. Login, usuários, empresas e permissões
2. Cadastros (clientes, fornecedores, produtos)
3. Estoque
4. Vendas / PDV
5. Compras
6. Financeiro (contas a pagar e receber, caixa)
7. Fiscal
8. Relatórios e dashboards

Não começar um módulo antes do anterior estar concluído e testado.

## Forma de trabalho
- **Planejar antes de codar:** para cada funcionalidade nova, apresentar o plano primeiro e só implementar após aprovação explícita.
- **Tarefas pequenas:** uma funcionalidade por vez, com um commit a cada passo que funciona (`./mvnw verify` verde).
- **Testes sempre:** toda regra de negócio tem teste automatizado.
- **Explicar as alterações:** ao terminar cada passo, resumir o que mudou e por quê, para revisão.

## Requisitos obrigatórios do ERP
- **Multiempresa (desde o início):** banco compartilhado com coluna `empresa_id` em toda tabela de negócio.
  - Na entidade JPA, o campo `empresa_id` leva `@TenantId`: o Hibernate filtra toda consulta e preenche a empresa ao gravar, a partir do token do usuário (`ResolvedorDeEmpresa`).
  - **Falha fechada:** sem empresa no token (ninguém logado, administrador da plataforma), nenhum dado de empresa é visível.
  - Nunca filtrar empresa manualmente nas consultas, e nunca aceitar `empresa_id` vindo do cliente da API.
  - Ver dados de todas as empresas só com `@AcessoEntreEmpresas` no caso de uso (ex.: login). Usar com parcimônia e justificar no Javadoc.
  - O `ArquiteturaTest` falha se uma coluna `empresa_id` não tiver `@TenantId` (exceções justificadas na lista `SEM_ISOLAMENTO_POR_EMPRESA`).
  - Todo módulo tem teste provando que a empresa A não vê nem altera dados da empresa B (modelo: `IsolamentoEntreEmpresasTest`; use `suporte/Autenticacao.comoUsuarioDaEmpresa`).
- **Dinheiro:** sempre `BigDecimal` (`NUMERIC` no banco); nunca `double`/`float`.
- **Transações:** operações que tocam várias tabelas/módulos (ex.: venda → estoque → financeiro) rodam em uma única transação; ou tudo grava, ou nada grava.
- **Auditoria completa:**
  - toda entidade tem `criado_em`, `criado_por`, `atualizado_em`, `atualizado_por` (Spring Data Auditing);
  - entidades de negócio são `@Audited` (Hibernate Envers): cada alteração gera uma versão com usuário, empresa e data/hora;
  - nunca apagar registros de negócio; inativar.
- **Transação:** garantida pela anotação `@CasoDeUso` (cada caso de uso é uma transação).
- **Permissões por usuário:** todo endpoint exige autenticação e uma permissão específica (`@PreAuthorize("hasAuthority('XXX')")`); não existe endpoint de negócio público.
  - Permissões ficam no enum `usuario.dominio.Permissao` (cada módulo acrescenta as suas, ex.: `PRODUTO_LER`, `PRODUTO_GERENCIAR`).
  - Cada empresa monta seus perfis; o vínculo usuário-empresa aponta para um perfil; o token carrega as permissões do perfil (mudanças valem na próxima renovação, até 15 min).
  - O perfil Administrador de cada empresa tem sempre todas as permissões e não pode ser alterado nem inativado.
  - O `ArquiteturaTest` falha se um endpoint não tiver `@PreAuthorize` ou citar permissão inexistente.
- **Backups automáticos** do PostgreSQL: serviço `backup` no `docker-compose` (diário, mantém 7 dias / 4 semanas / 6 meses em `./backups`); a pasta deve ser copiada para fora do servidor.
- **Fiscal:** integrar com uma API de emissão especializada (NF-e/NFC-e) em vez de implementar do zero; regras tributárias validadas com um contador.

## Stack
- Java 21
- Spring Boot 3 (Web, Data JPA, Validation, Security)
- Autenticação com JWT; Hibernate Envers para histórico de auditoria
- Maven (usar sempre o wrapper `./mvnw`)
- PostgreSQL 16; migrations com Flyway
- Testes: JUnit 5, Mockito, Spring Boot Test, Testcontainers (PostgreSQL)
- Docker + Docker Compose para subir o ambiente (PostgreSQL + aplicação)
- ArchUnit para verificar a arquitetura
- Lombok apenas na infraestrutura e na web (`@Getter`, `@Setter`, `@RequiredArgsConstructor`)

## Comandos
- Compilar: `./mvnw clean compile`
- Rodar: `./mvnw spring-boot:run`
- Testar tudo: `./mvnw test`
- Testar uma classe: `./mvnw test -Dtest=NomeDaClasseTest`
- Build completo (com testes): `./mvnw clean verify`
- Subir só o banco (para rodar a app pela IDE/`spring-boot:run`): `docker compose up -d db`
- Subir tudo (banco + app): `docker compose up -d --build`
- Derrubar e apagar os dados: `docker compose down -v`
- Os testes usam Testcontainers: o Docker precisa estar rodando para `./mvnw test`.
- Backup imediato: `scripts/backup-agora.sh`; restaurar: `scripts/restaurar-backup.sh <arquivo.sql.gz>`
- Testar backup e restauração de ponta a ponta: `scripts/testar-backup.sh` (rodar ao mexer no banco ou no serviço de backup)

## Arquitetura (Clean Architecture)
Pacote base: `br.com.goerp`. Cada módulo é dividido em quatro camadas:

```
src/main/java/br/com/goerp/
├── GoErpApplication.java
├── compartilhado/                # o que é comum a todos os módulos (mesmas camadas)
│   ├── dominio/                  # objetos de valor (Documento/Cpf/Cnpj, Endereco, Dinheiro, Quantidade, Gtin, Ncm...), Pagina/Paginacao, exceções
│   ├── aplicacao/                # @CasoDeUso, @ServicoDeAplicacao, portas UsuarioAtual/EmpresaAtual
│   ├── infraestrutura/           # auditoria (Envers), segurança, transação, conversores JPA
│   └── web/                      # TratadorGlobalDeErros
├── plataforma/                   # coordena empresa + usuario (cadastro de empresas pelo admin da plataforma)
└── <modulo>/                     # empresa, usuario (inclui autenticação), cadastros, estoque, ...
    ├── dominio/                  # entidades e objetos de valor PUROS + interfaces de repositório
    ├── aplicacao/                # casos de uso (uma classe por caso) + portas para serviços externos
    ├── infraestrutura/           # adaptadores: persistencia/ (XxxJpa, XxxJpaRepository, XxxRepositorioJpa), JWT, BCrypt...
    └── web/                      # controllers REST + records de request/response
src/main/resources/db/migration/  # scripts Flyway: V1__descricao.sql, V2__...
```

**Regra de dependência** (verificada pelo `ArquiteturaTest` com ArchUnit — o build falha se for violada):
- `dominio` → só `java.*` e outros `dominio`. Sem Spring, JPA, Jakarta ou Lombok.
- `aplicacao` → só `java.*`, `dominio` e `aplicacao`. Sem Spring/JPA/HTTP.
- `web` → `aplicacao` e `dominio`. Nunca chama repositório direto.
- `infraestrutura` → implementa as interfaces do domínio/aplicação. Ninguém depende dela.

**Como as peças se ligam:**
- Entidade de domínio (`Empresa`) é separada da entidade JPA (`EmpresaJpa`). O adaptador (`EmpresaRepositorioJpa`) converte entre as duas. Anotações de banco (`@Entity`, `@Audited`, `@TenantId`) só existem na infraestrutura.
- Entidades JPA estendem `EntidadeJpaAuditavel` (criado/atualizado em/por) e são `@Audited`.
- Classes JPA e Spring Data são package-private dentro de `infraestrutura/persistencia`.
- Casos de uso são anotados com `@CasoDeUso` (anotação nossa, sem Spring). A infraestrutura registra como bean e **executa cada chamada em uma transação** — não usar `@Transactional` em casos de uso, controllers ou domínio.
- Um módulo usa outro chamando seus casos de uso, nunca o repositório alheio. Sem dependência circular entre módulos: quando dois módulos precisam ser coordenados, o caso de uso coordenador fica num módulo acima deles (ex.: `plataforma`).

**Fluxos entre módulos:**
- venda/PDV → baixa no estoque + conta a receber + documento fiscal;
- compra recebida → entrada no estoque + conta a pagar.
- Movimentações de estoque e lançamentos financeiros são gerados pelos casos de uso de `estoque` e `financeiro`, nunca gravados direto por outros módulos.
- Saldo de estoque é sempre derivado de movimentações; nunca alterar saldo sem registrar a movimentação.

## Clean Code
- Nomes que revelam intenção, em português do domínio (`Empresa.inativar()`, `AutenticarUsuario`).
- Métodos pequenos, que fazem uma coisa só; poucos parâmetros.
- **Entidades ricas:** regras dentro da entidade (`empresa.inativar()`), sem setters públicos no domínio. Criação por fábricas nomeadas (`Empresa.nova(...)`, `Empresa.reconstituir(...)`).
- **Objetos de valor** para conceitos do negócio (`Cnpj`, `Email`, `Senha`, `Dinheiro`): sempre válidos desde a criação, imutáveis (`record`).
- **Um caso de uso por classe**, com nome de ação e um método público (`executar`).
- Exceções de domínio específicas (`CnpjInvalidoException`) estendendo `ExcecaoDeDominio`; o `TratadorGlobalDeErros` converte em JSON (RFC 7807): `ExcecaoDeDominio` → 422, `RecursoNaoEncontradoException` → 404, validação → 400.
- Comentários explicam o *porquê*, nunca o *quê*. Sem código comentado.
- Sem números/strings mágicos: constantes com nome.

## Convenções
- Injeção de dependência sempre por construtor, campos `final`; nunca `@Autowired` em campo.
- Lombok **só** em `infraestrutura` e `web`. Domínio e aplicação são Java puro.
- Valores monetários sempre com o objeto de valor `Dinheiro` (`BigDecimal`, 2 casas; recusa valores digitados com mais casas; cálculos arredondam meio-para-o-par). Quantidades com `Quantidade` (3 casas). Nunca `double`/`float`.
- Listagens paginadas devolvem `Pagina<T>` (compartilhado/dominio), nunca `Page` do Spring; tamanho máximo 100.
- Entidades editáveis por várias pessoas têm controle de versão: a API recebe a `versao` lida e devolve **409** (`ConflitoDeEdicaoException`) se o registro mudou nesse meio-tempo.
- Datas com `java.time`; obter a hora atual sempre via `Clock` injetado (testável; avança em microssegundos, a precisão do PostgreSQL).
- Resultados esperados de uma operação (ex.: login recusado) são retornados como valor (`sealed interface`), não como exceção — assim a transação confirma o que precisa ficar registrado.
- Validação de formato da entrada HTTP com Bean Validation nos records de `web`; regras de negócio ficam no domínio.
- Endpoints REST em `/api/v1/<recurso>` no plural (`/api/v1/empresas`).
- Commits no formato Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`).

## Testes
- **Domínio:** testes unitários puros (sem Spring, sem mocks), um por regra de negócio.
- **Casos de uso:** testes unitários com Mockito para as portas (repositórios, serviços externos).
- **Adaptadores, API e fluxos completos:** estender `suporte/TesteDeIntegracao` (`@SpringBootTest` + Testcontainers; banco limpo antes de cada teste). Dados de teste com `suporte/Cenario`; relógio controlável com `relogio.avancar(...)`.
- **Arquitetura:** `arquitetura/ArquiteturaTest` (ArchUnit).
- Toda funcionalidade nova ou correção de bug vem com teste.
- Rodar `./mvnw verify` antes de commitar; não commitar com testes falhando.

## Regras
- Nunca editar uma migration Flyway já commitada; criar uma nova versão.
- `spring.jpa.hibernate.ddl-auto` fica em `validate`; o schema é responsabilidade do Flyway.
- Nunca commitar credenciais; usar variáveis de ambiente (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `GOERP_ADMIN_*`).
- Não adicionar dependências novas no `pom.xml` sem necessidade clara.
