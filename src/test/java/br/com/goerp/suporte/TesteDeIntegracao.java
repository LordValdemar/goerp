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
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
public abstract class TesteDeIntegracao {

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void limparBanco() {
        String tabelas = String.join(", ", jdbc.queryForList("""
                SELECT tablename FROM pg_tables
                WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'
                """, String.class));
        if (!tabelas.isEmpty()) {
            jdbc.execute("TRUNCATE " + tabelas + " RESTART IDENTITY CASCADE");
        }
    }
}
