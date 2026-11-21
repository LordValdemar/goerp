package br.com.goerp.usuario.aplicacao.gestao;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.PerfilInativoException;
import br.com.goerp.usuario.dominio.PerfilRepositorio;
import br.com.goerp.usuario.dominio.UltimoAdministradorException;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

/** Consultas e verificações sobre os usuários da empresa atual, compartilhadas pelos casos de uso. */
@ServicoDeAplicacao
public class UsuariosDaEmpresa {

    private static final String RECURSO = "Usuário";

    private final UsuarioRepositorio usuarios;
    private final VinculoEmpresaRepositorio vinculos;
    private final PerfilRepositorio perfis;
    private final EmpresaAtual empresaAtual;

    public UsuariosDaEmpresa(UsuarioRepositorio usuarios, VinculoEmpresaRepositorio vinculos,
            PerfilRepositorio perfis, EmpresaAtual empresaAtual) {
        this.usuarios = usuarios;
        this.vinculos = vinculos;
        this.perfis = perfis;
        this.empresaAtual = empresaAtual;
    }

    public List<UsuarioDaEmpresa> listar() {
        List<VinculoEmpresa> daEmpresa = vinculos.listarDaEmpresa();
        Map<Long, Usuario> usuariosPorId = usuarios.buscarPorIds(daEmpresa.stream().map(VinculoEmpresa::usuarioId).toList())
                .stream().collect(Collectors.toMap(Usuario::id, Function.identity()));
        Map<Long, Perfil> perfisPorId = perfis.listar().stream()
                .collect(Collectors.toMap(Perfil::id, Function.identity()));
        return daEmpresa.stream()
                .map(vinculo -> new UsuarioDaEmpresa(usuariosPorId.get(vinculo.usuarioId()), vinculo,
                        perfisPorId.get(vinculo.perfilId())))
                .toList();
    }

    /** O usuário só "existe" para a empresa se tiver vínculo com ela (ativo ou não). */
    public UsuarioDaEmpresa buscar(Long usuarioId) {
        VinculoEmpresa vinculo = vinculos.buscar(usuarioId, empresaAtual.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException(RECURSO, usuarioId));
        Usuario usuario = usuarios.buscarPorId(usuarioId).orElseThrow();
        return new UsuarioDaEmpresa(usuario, vinculo, perfis.buscarPorId(vinculo.perfilId()).orElseThrow());
    }

    public Perfil perfilAtivo(Long perfilId) {
        Perfil perfil = perfis.buscarPorId(perfilId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", perfilId));
        if (!perfil.ativo()) {
            throw new PerfilInativoException();
        }
        return perfil;
    }

    /** Impede que a empresa fique sem nenhum administrador ativo ao tirar este usuário da função. */
    public void garantirQueRestaOutroAdministrador(UsuarioDaEmpresa atual) {
        boolean ehAdministradorAtivo = atual.vinculo().ativo() && atual.perfil().administrador();
        if (ehAdministradorAtivo && vinculos.contarAtivosComPerfil(atual.perfil().id()) <= 1) {
            throw new UltimoAdministradorException();
        }
    }
}
