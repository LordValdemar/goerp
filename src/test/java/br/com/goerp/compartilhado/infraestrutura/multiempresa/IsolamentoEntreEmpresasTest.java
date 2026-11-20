package br.com.goerp.compartilhado.infraestrutura.multiempresa;

import static br.com.goerp.suporte.Autenticacao.comoAdministradorDaPlataforma;
import static br.com.goerp.suporte.Autenticacao.comoUsuarioDaEmpresa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.TesteDeIntegracao;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

/**
 * Prova que cada empresa enxerga só os próprios dados, usando o caminho real:
 * a empresa vem do token do usuário autenticado e o Hibernate filtra pelo {@code @TenantId}.
 */
class IsolamentoEntreEmpresasTest extends TesteDeIntegracao {

    @Autowired
    Cenario cenario;

    @Autowired
    VinculoEmpresaRepositorio vinculos;

    Empresa empresaA;
    Empresa empresaB;
    Usuario contador;
    VinculoEmpresa vinculoNaB;

    @BeforeEach
    void prepararDuasEmpresasComOMesmoUsuario() {
        empresaA = cenario.empresa("11222333000181", "Empresa A LTDA");
        empresaB = cenario.empresa("11444777000161", "Empresa B LTDA");
        contador = cenario.usuario("contador@exemplo.com");
        cenario.vincular(contador, empresaA);
        vinculoNaB = cenario.vincular(contador, empresaB);
    }

    @Test
    void empresaAListaSoOsProprios() {
        var vistos = comoUsuarioDaEmpresa(empresaA.id(), () -> vinculos.listarDoUsuario(contador.id()));

        assertThat(vistos).extracting(VinculoEmpresa::empresaId).containsExactly(empresaA.id());
    }

    @Test
    void empresaANaoEncontraRegistroDaEmpresaB() {
        var buscado = comoUsuarioDaEmpresa(empresaA.id(), () -> vinculos.buscar(contador.id(), empresaB.id()));

        assertThat(buscado).isEmpty();
    }

    @Test
    void empresaANaoAlteraRegistroDaEmpresaBNemSabendoOId() {
        vinculoNaB.inativar();

        assertThatThrownBy(() -> comoUsuarioDaEmpresa(empresaA.id(), () -> vinculos.salvar(vinculoNaB)))
                .hasMessageContaining("não encontrado");
        assertThat(ContextoDeEmpresa.emTodasAsEmpresas(() -> vinculos.buscar(contador.id(), empresaB.id()))
                .orElseThrow().ativo()).isTrue();
    }

    @Test
    void empresaANaoCriaRegistroEmNomeDaEmpresaB() {
        Usuario outro = cenario.usuario("outro@exemplo.com");

        assertThatThrownBy(() -> comoUsuarioDaEmpresa(empresaA.id(),
                () -> vinculos.salvar(VinculoEmpresa.novo(outro.id(), empresaB.id()))))
                .isInstanceOf(RuntimeException.class);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM usuario_empresa WHERE usuario_id = ?", Integer.class,
                outro.id())).isZero();
    }

    @Test
    void registroCriadoNaEmpresaARecebeAEmpresaAutomaticamente() {
        Usuario novo = cenario.usuario("novo@exemplo.com");

        comoUsuarioDaEmpresa(empresaA.id(), () -> vinculos.salvar(VinculoEmpresa.novo(novo.id(), empresaA.id())));

        assertThat(jdbc.queryForObject("SELECT empresa_id FROM usuario_empresa WHERE usuario_id = ?", Long.class,
                novo.id())).isEqualTo(empresaA.id());
    }

    @Test
    void semUsuarioAutenticadoNaoVeNada() {
        assertThat(vinculos.listarDoUsuario(contador.id())).isEmpty();
    }

    @Test
    void administradorDaPlataformaNaoVeDadosDeEmpresaPorPadrao() {
        assertThat(comoAdministradorDaPlataforma(() -> vinculos.listarDoUsuario(contador.id()))).isEmpty();
    }

    @Test
    void escopoExplicitoDeTodasAsEmpresasVeTudoEDepoisVoltaAoNormal() {
        assertThat(ContextoDeEmpresa.emTodasAsEmpresas(() -> vinculos.listarDoUsuario(contador.id()))).hasSize(2);

        assertThat(vinculos.listarDoUsuario(contador.id())).isEmpty();
    }

    @Test
    void historicoDeAuditoriaRegistraAEmpresa() {
        Usuario novo = cenario.usuario("novo@exemplo.com");

        comoUsuarioDaEmpresa(empresaA.id(), () -> vinculos.salvar(VinculoEmpresa.novo(novo.id(), empresaA.id())));

        assertThat(jdbc.queryForObject("""
                SELECT r.empresa_id FROM usuario_empresa_aud a JOIN revisao r ON r.id = a.rev
                WHERE a.usuario_id = ?""", Long.class, novo.id())).isEqualTo(empresaA.id());
    }
}
