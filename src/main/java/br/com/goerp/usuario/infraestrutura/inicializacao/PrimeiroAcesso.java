package br.com.goerp.usuario.infraestrutura.inicializacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import br.com.goerp.usuario.aplicacao.CriarAdministradorDaPlataforma;
import lombok.RequiredArgsConstructor;

/** Ao subir a aplicação, garante que existe um administrador da plataforma. */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(PropriedadesDoAdministradorDaPlataforma.class)
public class PrimeiroAcesso implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PrimeiroAcesso.class);
    private static final String NOME_PADRAO = "Administrador da Plataforma";

    private final PropriedadesDoAdministradorDaPlataforma propriedades;
    private final CriarAdministradorDaPlataforma criarAdministrador;

    @Override
    public void run(ApplicationArguments argumentos) {
        if (!propriedades.configuradas()) {
            LOG.info("GOERP_ADMIN_EMAIL/GOERP_ADMIN_SENHA não definidos: administrador da plataforma não será criado agora");
            return;
        }
        String nome = propriedades.nome() == null || propriedades.nome().isBlank() ? NOME_PADRAO : propriedades.nome();
        if (criarAdministrador.executar(nome, propriedades.email(), propriedades.senha())) {
            LOG.warn("Administrador da plataforma criado ({}). Troque a senha no primeiro acesso.", propriedades.email());
        }
    }
}
