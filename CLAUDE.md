# GoERP

Sistema ERP para pequenas e médias empresas.

## Módulos
- **cadastros** – clientes, fornecedores, produtos, categorias, unidades de medida
- **estoque** – saldos, movimentações (entrada/saída/ajuste), inventário
- **vendas** – pedidos de venda e PDV (frente de caixa: abertura/fechamento de caixa, venda rápida, formas de pagamento)
- **compras** – pedidos de compra e recebimento de mercadorias (gera entrada no estoque e conta a pagar)
- **financeiro** – contas a pagar/receber, caixa e bancos, fluxo de caixa
- **fiscal** – emissão de NF-e/NFC-e, regras de tributação (ICMS, IPI, PIS, COFINS), NCM/CFOP

## Stack
- Java 21
- Spring Boot 3 (Web, Data JPA, Validation, Security)
- Maven (usar sempre o wrapper `./mvnw`)
- PostgreSQL 16; migrations com Flyway
- Testes: JUnit 5, Mockito, Spring Boot Test, Testcontainers (PostgreSQL)
- Lombok apenas para `@Getter`, `@Setter`, `@RequiredArgsConstructor` e `@Builder`

## Comandos
- Compilar: `./mvnw clean compile`
- Rodar: `./mvnw spring-boot:run`
- Testar tudo: `./mvnw test`
- Testar uma classe: `./mvnw test -Dtest=NomeDaClasseTest`
- Build completo (com testes): `./mvnw clean verify`

## Arquitetura
Pacote base: `br.com.goerp`. Organização por módulo (package-by-feature):

```
src/main/java/br/com/goerp/
├── GoErpApplication.java
├── shared/            # exceções, handler global de erros, config, utilitários
├── cadastros/
│   └── cliente/
│       ├── Cliente.java               # entidade JPA
│       ├── ClienteRepository.java     # Spring Data
│       ├── ClienteService.java        # regras de negócio
│       ├── ClienteController.java     # REST
│       └── dto/                       # records de request/response
├── estoque/
├── vendas/            # inclui pdv/
├── compras/
├── financeiro/
└── fiscal/
src/main/resources/
├── application.yml
└── db/migration/      # scripts Flyway: V1__descricao.sql, V2__...
```

- Controller → Service → Repository. Controllers não acessam repositories diretamente.
- Controllers recebem e devolvem DTOs (Java `record`), nunca entidades JPA.
- Um módulo só acessa outro pela camada de service, nunca pelo repository alheio.
- Fluxos entre módulos: venda/PDV → baixa no estoque + conta a receber + documento fiscal;
  compra recebida → entrada no estoque + conta a pagar. Movimentações de estoque e lançamentos
  financeiros são gerados pelos services de `estoque` e `financeiro`, nunca gravados direto por outros módulos.
- Saldo de estoque é sempre derivado de movimentações; nunca alterar saldo sem registrar a movimentação.

## Convenções
- Nomes de classes, métodos e variáveis em português, alinhados ao domínio (`Cliente`, `emitirNota`).
- Injeção de dependência por construtor (`@RequiredArgsConstructor` + campos `final`); nunca `@Autowired` em campo.
- Valores monetários sempre `BigDecimal` com escala 2; nunca `double`/`float`.
- Datas com `java.time` (`LocalDate`, `OffsetDateTime`).
- Validação de entrada com Bean Validation (`@NotBlank`, `@Valid`) nos DTOs.
- Erros de negócio lançam exceções de `shared/exception`; o `@RestControllerAdvice` global converte em respostas JSON padronizadas (RFC 7807 / `ProblemDetail`).
- `@Transactional` na camada de service, não no controller.
- Endpoints REST em `/api/v1/<recurso>` no plural (`/api/v1/clientes`).
- Commits no formato Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`).

## Testes
- Service: teste unitário com Mockito.
- Controller: `@WebMvcTest`.
- Repository e fluxos completos: `@SpringBootTest` + Testcontainers.
- Toda funcionalidade nova ou correção de bug vem com teste.
- Rodar `./mvnw test` antes de commitar; não commitar com testes falhando.

## Regras
- Nunca editar uma migration Flyway já commitada; criar uma nova versão.
- `spring.jpa.hibernate.ddl-auto` fica em `validate`; o schema é responsabilidade do Flyway.
- Nunca commitar credenciais; usar variáveis de ambiente (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
- Não adicionar dependências novas no `pom.xml` sem necessidade clara.
