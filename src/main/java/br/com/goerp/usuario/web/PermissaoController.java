package br.com.goerp.usuario.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.goerp.usuario.dominio.Permissao;

/** Catálogo de permissões existentes, para montar a tela de perfis. */
@RestController
@RequestMapping("/api/v1/permissoes")
public class PermissaoController {

    public record PermissaoResponse(String codigo, String descricao) {
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERFIL_LER')")
    public List<PermissaoResponse> listar() {
        return Arrays.stream(Permissao.values())
                .map(permissao -> new PermissaoResponse(permissao.name(), permissao.descricao()))
                .toList();
    }
}
