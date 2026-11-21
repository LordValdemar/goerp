package br.com.goerp.usuario.infraestrutura.inicializacao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import br.com.goerp.usuario.aplicacao.CriarAdministradorDaPlataforma;

class PrimeiroAcessoTest {

    CriarAdministradorDaPlataforma criar = mock(CriarAdministradorDaPlataforma.class);

    @Test
    void criaComOsDadosDasVariaveisDeAmbiente() {
        new PrimeiroAcesso(new PropriedadesDoAdministradorDaPlataforma(null, "admin@goerp.local", "senhaForte1"), criar)
                .run(null);

        verify(criar).executar("Administrador da Plataforma", "admin@goerp.local", "senhaForte1");
    }

    @Test
    void semVariaveisNaoFazNada() {
        new PrimeiroAcesso(new PropriedadesDoAdministradorDaPlataforma(null, "", null), criar).run(null);

        verify(criar, never()).executar(any(), any(), any());
    }
}
