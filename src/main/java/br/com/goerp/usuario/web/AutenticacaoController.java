package br.com.goerp.usuario.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.goerp.usuario.aplicacao.autenticacao.AutenticarUsuario;
import br.com.goerp.usuario.aplicacao.autenticacao.EncerrarSessao;
import br.com.goerp.usuario.aplicacao.autenticacao.RenovarAcesso;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.Autenticado;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.Recusado;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.SelecaoDeEmpresaNecessaria;
import br.com.goerp.usuario.aplicacao.autenticacao.SolicitacaoDeLogin;
import br.com.goerp.usuario.aplicacao.gestao.TrocarPropriaSenha;
import br.com.goerp.usuario.aplicacao.gestao.TrocarPropriaSenha.ResultadoDaTrocaDeSenha;
import br.com.goerp.usuario.dominio.SenhaAtualIncorretaException;
import br.com.goerp.usuario.web.AutenticacaoResponses.SelecaoDeEmpresa;
import br.com.goerp.usuario.web.AutenticacaoResponses.Tokens;
import br.com.goerp.usuario.web.AutenticacaoResponses.UsuarioAtual;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/autenticacao")
@RequiredArgsConstructor
public class AutenticacaoController {

    private static final String CLAIM_EMPRESA = "empresa";
    private static final String CLAIM_NOME = "nome";
    private static final String CLAIM_PERMISSOES = "permissoes";

    private final AutenticarUsuario autenticarUsuario;
    private final RenovarAcesso renovarAcesso;
    private final EncerrarSessao encerrarSessao;
    private final TrocarPropriaSenha trocarPropriaSenha;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return responder(autenticarUsuario.executar(
                new SolicitacaoDeLogin(request.email(), request.senha(), request.empresaId())));
    }

    @PostMapping("/renovar")
    public ResponseEntity<?> renovar(@Valid @RequestBody TokenDeRenovacaoRequest request) {
        return responder(renovarAcesso.executar(request.tokenDeRenovacao()));
    }

    @PostMapping("/sair")
    public ResponseEntity<Void> sair(@Valid @RequestBody TokenDeRenovacaoRequest request) {
        encerrarSessao.executar(request.tokenDeRenovacao());
        return ResponseEntity.noContent().build();
    }

    /** Quem sou eu: dados do token atual. Útil para o front-end e para conferir o login. */
    @GetMapping("/eu")
    public UsuarioAtual eu(@AuthenticationPrincipal Jwt token) {
        Number empresa = token.getClaim(CLAIM_EMPRESA);
        List<String> permissoes = token.getClaimAsStringList(CLAIM_PERMISSOES);
        return new UsuarioAtual(Long.valueOf(token.getSubject()), token.getClaimAsString(CLAIM_NOME),
                empresa == null ? null : empresa.longValue(), permissoes == null ? List.of() : permissoes);
    }

    /** Troca da própria senha: basta estar autenticado. Depois dela, é preciso fazer login de novo. */
    @PostMapping("/senha")
    public ResponseEntity<Void> trocarSenha(@Valid @RequestBody TrocaDeSenhaRequest request) {
        var resultado = trocarPropriaSenha.executar(request.senhaAtual(), request.novaSenha());
        if (resultado == ResultadoDaTrocaDeSenha.SENHA_ATUAL_INCORRETA) {
            throw new SenhaAtualIncorretaException();
        }
        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<?> responder(ResultadoDaAutenticacao resultado) {
        return switch (resultado) {
            case Autenticado autenticado -> ResponseEntity.ok(Tokens.de(autenticado));
            case SelecaoDeEmpresaNecessaria selecao -> ResponseEntity.ok(SelecaoDeEmpresa.de(selecao.empresas()));
            case Recusado recusado -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(recusa(recusado));
        };
    }

    private static ProblemDetail recusa(Recusado recusado) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, recusado.motivo().mensagem());
        problema.setTitle("Autenticação recusada");
        problema.setProperty("motivo", recusado.motivo().name());
        return problema;
    }
}
