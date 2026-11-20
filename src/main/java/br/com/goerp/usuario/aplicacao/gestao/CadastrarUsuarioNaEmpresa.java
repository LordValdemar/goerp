package br.com.goerp.usuario.aplicacao.gestao;

import java.util.Optional;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.usuario.aplicacao.CodificadorDeSenha;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.Senha;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioJaVinculadoException;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

/**
 * Dá acesso à empresa atual. Se o e-mail ainda não existe, cria o usuário; se já existe
 * (ex.: contador que atende outras empresas), apenas cria o vínculo, sem mexer nos dados dele.
 */
@CasoDeUso
public class CadastrarUsuarioNaEmpresa {

    private final UsuarioRepositorio usuarios;
    private final VinculoEmpresaRepositorio vinculos;
    private final UsuariosDaEmpresa usuariosDaEmpresa;
    private final CodificadorDeSenha codificadorDeSenha;
    private final EmpresaAtual empresaAtual;

    public CadastrarUsuarioNaEmpresa(UsuarioRepositorio usuarios, VinculoEmpresaRepositorio vinculos,
            UsuariosDaEmpresa usuariosDaEmpresa, CodificadorDeSenha codificadorDeSenha, EmpresaAtual empresaAtual) {
        this.usuarios = usuarios;
        this.vinculos = vinculos;
        this.usuariosDaEmpresa = usuariosDaEmpresa;
        this.codificadorDeSenha = codificadorDeSenha;
        this.empresaAtual = empresaAtual;
    }

    public ResultadoDoCadastro executar(DadosDoNovoUsuario dados) {
        Long empresaId = empresaAtual.id();
        Perfil perfil = usuariosDaEmpresa.perfilAtivo(dados.perfilId());
        Optional<Usuario> existente = usuarios.buscarPorEmail(Email.de(dados.email()));
        Usuario usuario = existente.orElseGet(() -> criarUsuario(dados));

        if (existente.isPresent() && vinculos.buscar(usuario.id(), empresaId).isPresent()) {
            throw new UsuarioJaVinculadoException();
        }
        VinculoEmpresa vinculo = vinculos.salvar(VinculoEmpresa.novo(usuario.id(), empresaId, perfil.id()));
        return new ResultadoDoCadastro(new UsuarioDaEmpresa(usuario, vinculo, perfil), existente.isPresent());
    }

    private Usuario criarUsuario(DadosDoNovoUsuario dados) {
        Senha senha = new Senha(dados.senha());
        return usuarios.salvar(Usuario.novo(dados.nome(), Email.de(dados.email()), codificadorDeSenha.criptografar(senha)));
    }
}
