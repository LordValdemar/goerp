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

import br.com.goerp.usuario.aplicacao.perfil.AlterarPerfil;
import br.com.goerp.usuario.aplicacao.perfil.BuscarPerfil;
import br.com.goerp.usuario.aplicacao.perfil.CriarPerfil;
import br.com.goerp.usuario.aplicacao.perfil.InativarPerfil;
import br.com.goerp.usuario.aplicacao.perfil.ListarPerfis;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/perfis")
@RequiredArgsConstructor
public class PerfilController {

    private final ListarPerfis listarPerfis;
    private final BuscarPerfil buscarPerfil;
    private final CriarPerfil criarPerfil;
    private final AlterarPerfil alterarPerfil;
    private final InativarPerfil inativarPerfil;

    @GetMapping
    @PreAuthorize("hasAuthority('PERFIL_LER')")
    public List<PerfilResponse> listar() {
        return listarPerfis.executar().stream().map(PerfilResponse::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFIL_LER')")
    public PerfilResponse buscar(@PathVariable Long id) {
        return PerfilResponse.de(buscarPerfil.executar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERFIL_GERENCIAR')")
    public ResponseEntity<PerfilResponse> criar(@Valid @RequestBody PerfilRequest request) {
        PerfilResponse criado = PerfilResponse.de(criarPerfil.executar(request.paraDados()));
        return ResponseEntity.created(URI.create("/api/v1/perfis/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFIL_GERENCIAR')")
    public PerfilResponse alterar(@PathVariable Long id, @Valid @RequestBody PerfilRequest request) {
        return PerfilResponse.de(alterarPerfil.executar(id, request.paraDados()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFIL_GERENCIAR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        inativarPerfil.executar(id);
        return ResponseEntity.noContent().build();
    }
}
