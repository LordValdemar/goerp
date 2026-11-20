package br.com.goerp.usuario.aplicacao.gestao;

import java.util.Optional;

import br.com.goerp.compartilhado.aplicacao.AcessoEntreEmpresas;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.usuario.aplicacao.CodificadorDeSenha;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.PerfilRepositorio;
import br.com.goerp.usuario.dominio.Senha;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

/**
 * Prepara uma empresa recém-cadastrada: cria o perfil Administrador e dá acesso ao primeiro
 * administrador (novo usuário, ou um já existente, que mantém a própria senha).
 *
 * <p>Acessa todas as empresas porque quem chama é o administrador da plataforma, que não está
 * dentro de nenhuma empresa; a empresa de destino é informada explicitamente.
 */
@CasoDeUso
@AcessoEntreEmpresas
public class ConfigurarAcessoInicialDaEmpresa {

    private final PerfilRepositorio perfis;
    private final UsuarioRepositorio usuarios;
    private final VinculoEmpresaRepositorio vinculos;
    private final CodificadorDeSenha codificadorDeSenha;

    public ConfigurarAcessoInicialDaEmpresa(PerfilRepositorio perfis, UsuarioRepositorio usuarios,
            VinculoEmpresaRepositorio vinculos, CodificadorDeSenha codificadorDeSenha) {
        this.perfis = perfis;
        this.usuarios = usuarios;
        this.vinculos = vinculos;
        this.codificadorDeSenha = codificadorDeSenha;
    }

    public AcessoInicial executar(Long empresaId, DadosDoAdministrador dados) {
        Perfil administrador = perfis.salvar(Perfil.administradorDa(empresaId));
        Optional<Usuario> existente = usuarios.buscarPorEmail(Email.de(dados.email()));
        Usuario usuario = existente.orElseGet(() -> usuarios.salvar(Usuario.novo(dados.nome(), Email.de(dados.email()),
                codificadorDeSenha.criptografar(new Senha(dados.senha())))));
        vinculos.salvar(VinculoEmpresa.novo(usuario.id(), empresaId, administrador.id()));
        return new AcessoInicial(usuario, existente.isPresent());
    }
}
