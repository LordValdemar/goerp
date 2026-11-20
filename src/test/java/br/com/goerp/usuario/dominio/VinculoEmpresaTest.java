package br.com.goerp.usuario.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class VinculoEmpresaTest {

    @Test
    void novoVinculoNasceAtivo() {
        assertThat(VinculoEmpresa.novo(1L, 2L, 3L).ativo()).isTrue();
    }

    @Test
    void exigeUsuarioEmpresaEPerfil() {
        assertThatThrownBy(() -> VinculoEmpresa.novo(null, 2L, 3L)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> VinculoEmpresa.novo(1L, null, 3L)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> VinculoEmpresa.novo(1L, 2L, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void trocaPerfil() {
        VinculoEmpresa vinculo = VinculoEmpresa.novo(1L, 2L, 3L);

        vinculo.trocarPerfil(4L);

        assertThat(vinculo.perfilId()).isEqualTo(4L);
    }

    @Test
    void inativa() {
        VinculoEmpresa vinculo = VinculoEmpresa.novo(1L, 2L, 3L);

        vinculo.inativar();

        assertThat(vinculo.ativo()).isFalse();
    }
}
