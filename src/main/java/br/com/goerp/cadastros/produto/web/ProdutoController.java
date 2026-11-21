package br.com.goerp.cadastros.produto.web;

import java.net.URI;

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

import br.com.goerp.cadastros.produto.aplicacao.AlterarProduto;
import br.com.goerp.cadastros.produto.aplicacao.AlterarSituacaoDoProduto;
import br.com.goerp.cadastros.produto.aplicacao.BuscarProduto;
import br.com.goerp.cadastros.produto.aplicacao.BuscarProdutoPorCodigoDeBarras;
import br.com.goerp.cadastros.produto.aplicacao.CadastrarProduto;
import br.com.goerp.cadastros.produto.aplicacao.PesquisarProdutos;
import br.com.goerp.cadastros.produto.dominio.FiltroDeProdutos;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.web.PaginaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final PesquisarProdutos pesquisarProdutos;
    private final BuscarProduto buscarProduto;
    private final CadastrarProduto cadastrarProduto;
    private final AlterarProduto alterarProduto;
    private final AlterarSituacaoDoProduto alterarSituacao;
    private final BuscarProdutoPorCodigoDeBarras buscarPorCodigoDeBarras;

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public PaginaResponse<ProdutoResponse> pesquisar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(pesquisarProdutos.executar(new FiltroDeProdutos(busca, categoriaId, ativo),
                new Paginacao(pagina, tamanho)), ProdutoResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public ProdutoResponse buscar(@PathVariable Long id) {
        return ProdutoResponse.de(buscarProduto.executar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public ResponseEntity<ProdutoResponse> cadastrar(@Valid @RequestBody ProdutoRequest request) {
        var criado = ProdutoResponse.de(cadastrarProduto.executar(request.codigo(), request.paraDados()));
        return ResponseEntity.created(URI.create("/api/v1/produtos/" + criado.id())).body(criado);
    }

    /** Leitura do código de barras (PDV): só produtos ativos. */
    @GetMapping("/codigo-barras/{codigo}")
    @PreAuthorize("hasAuthority('PRODUTO_LER')")
    public ProdutoResponse buscarPorCodigoDeBarras(@PathVariable String codigo) {
        return ProdutoResponse.de(buscarPorCodigoDeBarras.executar(codigo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public ProdutoResponse alterar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return ProdutoResponse.de(alterarProduto.executar(id, request.versao(), request.paraDados()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        alterarSituacao.executar(id, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('PRODUTO_GERENCIAR')")
    public ProdutoResponse reativar(@PathVariable Long id) {
        return ProdutoResponse.de(alterarSituacao.executar(id, true));
    }
}
