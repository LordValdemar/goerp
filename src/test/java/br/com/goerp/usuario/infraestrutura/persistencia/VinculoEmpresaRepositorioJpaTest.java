package br.com.goerp.usuario.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.goerp.compartilhado.dominio.Cnpj;
import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;
import br.com.goerp.suporte.TesteDeIntegracao;
import br.com.goerp.usuario.dominio.SenhaCriptografada;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

class VinculoEmpresaRepositorioJpaTest extends TesteDeIntegracao {

    @Autowired
    VinculoEmpresaRepositorio vinculos;

    @Autowired
    UsuarioRepositorio usuarios;

    @Autowired
    EmpresaRepositorio empresas;

    Long usuarioId;
    Long empresaA;
    Long empresaB;

    @BeforeEach
    void prepararDados() {
        usuarioId = usuarios.salvar(Usuario.novo("Maria", Email.de("maria@exemplo.com"), new SenhaCriptografada("hash")))
                .id();
        empresaA = empresas.salvar(Empresa.nova(Cnpj.de("11222333000181"), "Empresa A", null)).id();
        empresaB = empresas.salvar(Empresa.nova(Cnpj.de("11444777000161"), "Empresa B", null)).id();
    }

    @Test
    void mesmoUsuarioPodeTerVinculoComVariasEmpresas() {
        vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaA));
        vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaB));

        assertThat(vinculos.listarDoUsuario(usuarioId))
                .extracting(VinculoEmpresa::empresaId)
                .containsExactly(empresaA, empresaB);
    }

    @Test
    void naoPermiteVinculoDuplicado() {
        vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaA));

        assertThatThrownBy(() -> vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaA)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void inativaVinculo() {
        VinculoEmpresa vinculo = vinculos.salvar(VinculoEmpresa.novo(usuarioId, empresaA));
        vinculo.inativar();
        vinculos.salvar(vinculo);

        assertThat(vinculos.buscar(usuarioId, empresaA).orElseThrow().ativo()).isFalse();
    }
}
