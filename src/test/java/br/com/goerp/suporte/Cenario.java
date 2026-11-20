package br.com.goerp.suporte;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import br.com.goerp.compartilhado.dominio.Cnpj;
import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.compartilhado.infraestrutura.multiempresa.ContextoDeEmpresa;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;
import br.com.goerp.usuario.aplicacao.CodificadorDeSenha;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.PerfilRepositorio;
import br.com.goerp.usuario.dominio.Permissao;
import br.com.goerp.usuario.dominio.Senha;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;
import lombok.RequiredArgsConstructor;

/** Monta dados de teste direto pelos repositórios, sem filtro de empresa. */
@Component
@RequiredArgsConstructor
public class Cenario {

    public static final String SENHA = "segredo123";

    private final EmpresaRepositorio empresas;
    private final UsuarioRepositorio usuarios;
    private final VinculoEmpresaRepositorio vinculos;
    private final PerfilRepositorio perfis;
    private final CodificadorDeSenha codificador;

    /** Cria a empresa já com o perfil Administrador, como acontecerá no cadastro real. */
    public Empresa empresa(String cnpj, String razaoSocial) {
        Empresa empresa = empresas.salvar(Empresa.nova(Cnpj.de(cnpj), razaoSocial, null));
        ContextoDeEmpresa.emTodasAsEmpresas(() -> perfis.salvar(Perfil.administradorDa(empresa.id())));
        return empresa;
    }

    public Perfil perfil(Empresa empresa, String nome, Permissao primeira, Permissao... demais) {
        return ContextoDeEmpresa.emTodasAsEmpresas(
                () -> perfis.salvar(Perfil.novo(empresa.id(), nome, EnumSet.of(primeira, demais))));
    }

    public Perfil administradorDa(Empresa empresa) {
        return ContextoDeEmpresa.emTodasAsEmpresas(() -> perfis.listar().stream()
                .filter(perfil -> perfil.administrador() && perfil.empresaId().equals(empresa.id()))
                .findFirst()
                .orElseThrow());
    }

    public Usuario usuario(String email) {
        return usuarios.salvar(Usuario.novo("Usuário " + email, Email.de(email), codificador.criptografar(Senha.de(SENHA))));
    }

    public Usuario administradorDaPlataforma(String email) {
        return usuarios.salvar(Usuario.novoAdministradorDaPlataforma("Admin", Email.de(email),
                codificador.criptografar(Senha.de(SENHA))));
    }

    /** Vincula com o perfil Administrador da empresa. */
    public VinculoEmpresa vincular(Usuario usuario, Empresa empresa) {
        return vincular(usuario, empresa, administradorDa(empresa));
    }

    public VinculoEmpresa vincular(Usuario usuario, Empresa empresa, Perfil perfil) {
        return ContextoDeEmpresa.emTodasAsEmpresas(
                () -> vinculos.salvar(VinculoEmpresa.novo(usuario.id(), empresa.id(), perfil.id())));
    }
}
