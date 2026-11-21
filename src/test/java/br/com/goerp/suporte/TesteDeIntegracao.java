package br.com.goerp.suporte;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Base dos testes de integração: sobe a aplicação inteira contra um PostgreSQL real
 * (Testcontainers) e começa cada teste com o banco vazio.
 */
@SpringBootTest(properties = "goerp.seguranca.segredo-jwt=segredo-de-teste-com-mais-de-32-caracteres")
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
public abstract class TesteDeIntegracao {

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected RelogioAjustavel relogio;

    @BeforeEach
    void limparBanco() {
        relogio.voltarParaAgora();
        String tabelas = String.join(", ", jdbc.queryForList("""
                SELECT tablename FROM pg_tables
                WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'
                """, String.class));
        if (!tabelas.isEmpty()) {
            jdbc.execute("TRUNCATE " + tabelas + " RESTART IDENTITY CASCADE");
        }
    }
}
