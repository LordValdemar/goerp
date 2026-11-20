package br.com.goerp.usuario.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class VinculoEmpresaTest {

    @Test
    void novoVinculoNasceAtivo() {
        assertThat(VinculoEmpresa.novo(1L, 2L).ativo()).isTrue();
    }

    @Test
    void exigeUsuarioEEmpresa() {
        assertThatThrownBy(() -> VinculoEmpresa.novo(null, 2L)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> VinculoEmpresa.novo(1L, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void inativa() {
        VinculoEmpresa vinculo = VinculoEmpresa.novo(1L, 2L);

        vinculo.inativar();

        assertThat(vinculo.ativo()).isFalse();
    }
}
