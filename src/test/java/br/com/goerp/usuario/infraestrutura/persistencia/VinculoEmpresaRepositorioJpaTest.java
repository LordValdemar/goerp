package br.com.goerp.usuario.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.compartilhado.infraestrutura.multiempresa.ContextoDeEmpresa;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.TesteDeIntegracao;
import br.com.goerp.usuario.dominio.SenhaCriptografada;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

/** Testa a persistência em si, sem filtro de empresa (o isolamento tem teste próprio). */
class VinculoEmpresaRepositorioJpaTest extends TesteDeIntegracao {

    @Autowired
    VinculoEmpresaRepositorio vinculos;

    @Autowired
    UsuarioRepositorio usuarios;

    @Autowired
    Cenario cenario;

    Long usuarioId;
    Long empresaA;
    Long empresaB;
    Long perfilA;
    Long perfilB;

    @BeforeEach
    void prepararDados() {
        usuarioId = usuarios.salvar(Usuario.novo("Maria", Email.de("maria@exemplo.com"), new SenhaCriptografada("hash")))
                .id();
        Empresa a = cenario.empresa("11222333000181", "Empresa A");
        Empresa b = cenario.empresa("11444777000161", "Empresa B");
        empresaA = a.id();
        empresaB = b.id();
        perfilA = cenario.administradorDa(a).id();
        perfilB = cenario.administradorDa(b).id();
    }

    @Test
    void mesmoUsuarioPodeTerVinculoComVariasEmpresas() {
        ContextoDeEmpresa.emTodasAsEmpresas(() -> {
            vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaA, perfilA));
            vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaB, perfilB));
        });

        assertThat(ContextoDeEmpresa.emTodasAsEmpresas(() -> vinculos.listarDoUsuario(usuarioId)))
                .extracting(VinculoEmpresa::empresaId)
                .containsExactly(empresaA, empresaB);
    }

    @Test
    void naoPermiteVinculoDuplicado() {
        ContextoDeEmpresa.emTodasAsEmpresas(() -> vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaA, perfilA)));

        assertThatThrownBy(() -> ContextoDeEmpresa.emTodasAsEmpresas(
                () -> vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaA, perfilA))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void inativaVinculo() {
        ContextoDeEmpresa.emTodasAsEmpresas(() -> {
            VinculoEmpresa vinculo = vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaA, perfilA));
            vinculo.inativar();
            vinculos.salvar(vinculo);
        });

        assertThat(ContextoDeEmpresa.emTodasAsEmpresas(() -> vinculos.buscar(usuarioId, empresaA)).orElseThrow().ativo())
                .isFalse();
    }

    @Test
    void perfilPrecisaSerDaMesmaEmpresaDoVinculo() {
        assertThatThrownBy(() -> ContextoDeEmpresa.emTodasAsEmpresas(
                () -> vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaA, perfilB))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void sabeSeHaVinculoAtivoComOPerfil() {
        VinculoEmpresa vinculo = ContextoDeEmpresa.emTodasAsEmpresas(
                () -> vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaA, perfilA)));

        assertThat(ContextoDeEmpresa.emTodasAsEmpresas(() -> vinculos.existeAtivoComPerfil(perfilA))).isTrue();

        vinculo.inativar();
        ContextoDeEmpresa.emTodasAsEmpresas(() -> vinculos.salvar(vinculo));
        assertThat(ContextoDeEmpresa.emTodasAsEmpresas(() -> vinculos.existeAtivoComPerfil(perfilA))).isFalse();
    }
}
