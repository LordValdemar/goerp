package br.com.goerp.usuario.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.goerp.usuario.aplicacao.gestao.BuscarUsuarioDaEmpresa;
import br.com.goerp.usuario.aplicacao.gestao.CadastrarUsuarioNaEmpresa;
import br.com.goerp.usuario.aplicacao.gestao.InativarUsuarioNaEmpresa;
import br.com.goerp.usuario.aplicacao.gestao.ListarUsuariosDaEmpresa;
import br.com.goerp.usuario.aplicacao.gestao.ReativarUsuarioNaEmpresa;
import br.com.goerp.usuario.aplicacao.gestao.ResultadoDoCadastro;
import br.com.goerp.usuario.aplicacao.gestao.TrocarPerfilDoUsuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Usuários da empresa atual. O id é o do usuário; o acesso gerenciado é o vínculo com esta empresa. */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private static final String USUARIO_JA_EXISTIA = "X-Usuario-Ja-Existia";

    private final ListarUsuariosDaEmpresa listarUsuarios;
    private final BuscarUsuarioDaEmpresa buscarUsuario;
    private final CadastrarUsuarioNaEmpresa cadastrarUsuario;
    private final TrocarPerfilDoUsuario trocarPerfil;
    private final InativarUsuarioNaEmpresa inativarUsuario;
    private final ReativarUsuarioNaEmpresa reativarUsuario;

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_LER')")
    public List<UsuarioDaEmpresaResponse> listar() {
        return listarUsuarios.executar().stream().map(UsuarioDaEmpresaResponse::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_LER')")
    public UsuarioDaEmpresaResponse buscar(@PathVariable Long id) {
        return UsuarioDaEmpresaResponse.de(buscarUsuario.executar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public ResponseEntity<UsuarioDaEmpresaResponse> cadastrar(@Valid @RequestBody NovoUsuarioRequest request) {
        ResultadoDoCadastro resultado = cadastrarUsuario.executar(request.paraDados());
        UsuarioDaEmpresaResponse corpo = UsuarioDaEmpresaResponse.de(resultado.usuarioDaEmpresa());
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + corpo.id()))
                .header(USUARIO_JA_EXISTIA, String.valueOf(resultado.usuarioJaExistia()))
                .body(corpo);
    }

    @PutMapping("/{id}/perfil")
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public UsuarioDaEmpresaResponse trocarPerfil(@PathVariable Long id, @Valid @RequestBody TrocaDePerfilRequest request) {
        return UsuarioDaEmpresaResponse.de(trocarPerfil.executar(id, request.perfilId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        inativarUsuario.executar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public UsuarioDaEmpresaResponse reativar(@PathVariable Long id) {
        return UsuarioDaEmpresaResponse.de(reativarUsuario.executar(id));
    }
}
