package br.com.goerp.usuario.dominio;

import static br.com.goerp.usuario.dominio.Permissao.PERFIL_LER;
import static br.com.goerp.usuario.dominio.Permissao.USUARIO_LER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;

class PerfilTest {

    private static final Long EMPRESA = 1L;

    @Test
    void novoPerfilConcedeSoAsPermissoesEscolhidas() {
        Perfil caixa = Perfil.novo(EMPRESA, "Caixa", Set.of(USUARIO_LER));

        assertThat(caixa.concede(USUARIO_LER)).isTrue();
        assertThat(caixa.concede(PERFIL_LER)).isFalse();
        assertThat(caixa.administrador()).isFalse();
    }

    @Test
    void exigeNomeEAoMenosUmaPermissao() {
        assertThatThrownBy(() -> Perfil.novo(EMPRESA, " ", Set.of(USUARIO_LER)))
                .isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> Perfil.novo(EMPRESA, "Caixa", Set.of()))
                .isInstanceOf(PerfilSemPermissaoException.class);
    }

    @Test
    void administradorTemTodasAsPermissoes() {
        Perfil administrador = Perfil.administradorDa(EMPRESA);

        assertThat(administrador.permissoes()).isEqualTo(EnumSet.allOf(Permissao.class));
        assertThat(administrador.nome()).isEqualTo(Perfil.NOME_DO_ADMINISTRADOR);
    }

    @Test
    void administradorGanhaPermissoesCriadasDepoisDele() {
        Perfil gravadoComMenosPermissoes = Perfil.reconstituir(5L, EMPRESA, "Administrador", Set.of(USUARIO_LER),
                true, true);

        assertThat(gravadoComMenosPermissoes.permissoes()).isEqualTo(EnumSet.allOf(Permissao.class));
    }

    @Test
    void administradorNaoPodeSerAlteradoNemInativado() {
        Perfil administrador = Perfil.administradorDa(EMPRESA);

        assertThatThrownBy(() -> administrador.alterar("Outro", Set.of(USUARIO_LER)))
                .isInstanceOf(PerfilDoSistemaException.class);
        assertThatThrownBy(administrador::inativar).isInstanceOf(PerfilDoSistemaException.class);
    }

    @Test
    void alteraNomeEPermissoes() {
        Perfil perfil = Perfil.novo(EMPRESA, "Caixa", Set.of(USUARIO_LER));

        perfil.alterar("Gerente", Set.of(USUARIO_LER, PERFIL_LER));

        assertThat(perfil.nome()).isEqualTo("Gerente");
        assertThat(perfil.permissoes()).containsExactlyInAnyOrder(USUARIO_LER, PERFIL_LER);
    }

    @Test
    void perfilInativoNaoConcedeNada() {
        Perfil perfil = Perfil.novo(EMPRESA, "Caixa", Set.of(USUARIO_LER));

        perfil.inativar();

        assertThat(perfil.concede(USUARIO_LER)).isFalse();
    }

    @Test
    void permissoesNaoPodemSerAlteradasPorFora() {
        Perfil perfil = Perfil.novo(EMPRESA, "Caixa", Set.of(USUARIO_LER));

        assertThatThrownBy(() -> perfil.permissoes().add(PERFIL_LER)).isInstanceOf(UnsupportedOperationException.class);
    }
}
