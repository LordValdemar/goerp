package br.com.goerp.cadastros.produto.web;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.goerp.cadastros.produto.aplicacao.AlterarSituacaoDaCategoria;
import br.com.goerp.cadastros.produto.aplicacao.BuscarCategoria;
import br.com.goerp.cadastros.produto.aplicacao.CadastrarCategoria;
import br.com.goerp.cadastros.produto.aplicacao.ListarCategorias;
import br.com.goerp.cadastros.produto.aplicacao.RenomearCategoria;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final ListarCategorias listar;
    private final BuscarCategoria buscar;
    private final CadastrarCategoria cadastrar;
    private final RenomearCategoria renomear;
    private final AlterarSituacaoDaCategoria alterarSituacao;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public List<CategoriaResponse> listar(@RequestParam(required = false) Boolean ativa) {
        return listar.executar(ativa).stream().map(CategoriaResponse::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public CategoriaResponse buscar(@PathVariable Long id) {
        return CategoriaResponse.de(buscar.executar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public ResponseEntity<CategoriaResponse> cadastrar(@Valid @RequestBody CategoriaRequest request) {
        var criada = CategoriaResponse.de(cadastrar.executar(request.nome()));
        return ResponseEntity.created(URI.create("/api/v1/categorias/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public CategoriaResponse renomear(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return CategoriaResponse.de(renomear.executar(id, request.nome()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        alterarSituacao.executar(id, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public CategoriaResponse reativar(@PathVariable Long id) {
        return CategoriaResponse.de(alterarSituacao.executar(id, true));
    }
}
