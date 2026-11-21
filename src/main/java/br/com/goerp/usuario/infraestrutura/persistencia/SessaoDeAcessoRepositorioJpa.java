package br.com.goerp.usuario.infraestrutura.persistencia;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.usuario.dominio.SessaoDeAcesso;
import br.com.goerp.usuario.dominio.SessaoDeAcessoRepositorio;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class SessaoDeAcessoRepositorioJpa implements SessaoDeAcessoRepositorio {

    private final SessaoDeAcessoJpaRepository jpa;

    @Override
    public SessaoDeAcesso salvar(SessaoDeAcesso sessao) {
        SessaoDeAcessoJpa registro = sessao.id() == null ? new SessaoDeAcessoJpa() : buscarRegistro(sessao.id());
        registro.setUsuarioId(sessao.usuarioId());
        registro.setEmpresaId(sessao.empresaId());
        registro.setHashDoToken(sessao.hashDoToken());
        registro.setExpiraEm(sessao.expiraEm());
        registro.setRevogadaEm(sessao.revogadaEm());
        return paraDominio(jpa.save(registro));
    }

    @Override
    public Optional<SessaoDeAcesso> buscarPorHashDoToken(String hashDoToken) {
        return jpa.findByHashDoToken(hashDoToken).map(SessaoDeAcessoRepositorioJpa::paraDominio);
    }

    @Override
    public void revogarTodasDoUsuario(Long usuarioId, Instant agora) {
        jpa.revogarTodasDoUsuario(usuarioId, agora);
    }

    @Override
    public void revogarTodasDoUsuarioNaEmpresa(Long usuarioId, Long empresaId, Instant agora) {
        jpa.revogarTodasDoUsuarioNaEmpresa(usuarioId, empresaId, agora);
    }

    private SessaoDeAcessoJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Sessão", id));
    }

    private static SessaoDeAcesso paraDominio(SessaoDeAcessoJpa registro) {
        return SessaoDeAcesso.reconstituir(registro.getId(), registro.getUsuarioId(), registro.getEmpresaId(),
                registro.getHashDoToken(), registro.getExpiraEm(), registro.getRevogadaEm());
    }
}
