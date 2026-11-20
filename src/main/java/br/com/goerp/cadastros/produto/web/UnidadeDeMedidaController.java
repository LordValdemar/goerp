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

import br.com.goerp.cadastros.produto.aplicacao.AlterarSituacaoDaUnidadeDeMedida;
import br.com.goerp.cadastros.produto.aplicacao.AlterarUnidadeDeMedida;
import br.com.goerp.cadastros.produto.aplicacao.BuscarUnidadeDeMedida;
import br.com.goerp.cadastros.produto.aplicacao.CadastrarUnidadeDeMedida;
import br.com.goerp.cadastros.produto.aplicacao.ListarUnidadesDeMedida;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/unidades")
@RequiredArgsConstructor
public class UnidadeDeMedidaController {

    private final ListarUnidadesDeMedida listar;
    private final BuscarUnidadeDeMedida buscar;
    private final CadastrarUnidadeDeMedida cadastrar;
    private final AlterarUnidadeDeMedida alterar;
    private final AlterarSituacaoDaUnidadeDeMedida alterarSituacao;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public List<UnidadeDeMedidaResponse> listar(@RequestParam(required = false) Boolean ativa) {
        return listar.executar(ativa).stream().map(UnidadeDeMedidaResponse::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public UnidadeDeMedidaResponse buscar(@PathVariable Long id) {
        return UnidadeDeMedidaResponse.de(buscar.executar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public ResponseEntity<UnidadeDeMedidaResponse> cadastrar(@Valid @RequestBody UnidadeDeMedidaRequest request) {
        var criada = UnidadeDeMedidaResponse.de(
                cadastrar.executar(request.sigla(), request.descricao(), request.fracionavel()));
        return ResponseEntity.created(URI.create("/api/v1/unidades/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public UnidadeDeMedidaResponse alterar(@PathVariable Long id, @Valid @RequestBody UnidadeDeMedidaRequest request) {
        return UnidadeDeMedidaResponse.de(alterar.executar(id, request.descricao(), request.fracionavel()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        alterarSituacao.executar(id, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public UnidadeDeMedidaResponse reativar(@PathVariable Long id) {
        return UnidadeDeMedidaResponse.de(alterarSituacao.executar(id, true));
    }
}
