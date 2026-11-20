package br.com.goerp.cadastros.fornecedor.web;

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

import br.com.goerp.cadastros.fornecedor.aplicacao.AlterarFornecedor;
import br.com.goerp.cadastros.fornecedor.aplicacao.BuscarFornecedor;
import br.com.goerp.cadastros.fornecedor.aplicacao.CadastrarFornecedor;
import br.com.goerp.cadastros.fornecedor.aplicacao.InativarFornecedor;
import br.com.goerp.cadastros.fornecedor.aplicacao.PesquisarFornecedores;
import br.com.goerp.cadastros.fornecedor.aplicacao.ReativarFornecedor;
import br.com.goerp.cadastros.fornecedor.dominio.FiltroDeFornecedores;
import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.web.PaginaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final PesquisarFornecedores pesquisarFornecedores;
    private final BuscarFornecedor buscarFornecedor;
    private final CadastrarFornecedor cadastrarFornecedor;
    private final AlterarFornecedor alterarFornecedor;
    private final InativarFornecedor inativarFornecedor;
    private final ReativarFornecedor reativarFornecedor;

    @GetMapping
    @PreAuthorize("hasAuthority('FORNECEDOR_LER')")
    public PaginaResponse<FornecedorResponse> pesquisar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(
                pesquisarFornecedores.executar(new FiltroDeFornecedores(busca, ativo), new Paginacao(pagina, tamanho)),
                FornecedorResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FORNECEDOR_LER')")
    public FornecedorResponse buscar(@PathVariable Long id) {
        return FornecedorResponse.de(buscarFornecedor.executar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('FORNECEDOR_GERENCIAR')")
    public ResponseEntity<FornecedorResponse> cadastrar(@Valid @RequestBody FornecedorRequest request) {
        var criado = FornecedorResponse.de(
                cadastrarFornecedor.executar(Documento.de(request.documento()), request.paraDados()));
        return ResponseEntity.created(URI.create("/api/v1/fornecedores/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FORNECEDOR_GERENCIAR')")
    public FornecedorResponse alterar(@PathVariable Long id, @Valid @RequestBody FornecedorRequest request) {
        return FornecedorResponse.de(alterarFornecedor.executar(id, request.versao(), request.paraDados()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FORNECEDOR_GERENCIAR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        inativarFornecedor.executar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('FORNECEDOR_GERENCIAR')")
    public FornecedorResponse reativar(@PathVariable Long id) {
        return FornecedorResponse.de(reativarFornecedor.executar(id));
    }
}
