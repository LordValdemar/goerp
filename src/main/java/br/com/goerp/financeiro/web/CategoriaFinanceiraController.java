package br.com.goerp.financeiro.web;

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

import br.com.goerp.financeiro.aplicacao.AlterarSituacaoDaCategoriaFinanceira;
import br.com.goerp.financeiro.aplicacao.BuscarCategoriaFinanceira;
import br.com.goerp.financeiro.aplicacao.CadastrarCategoriaFinanceira;
import br.com.goerp.financeiro.aplicacao.ListarCategoriasFinanceiras;
import br.com.goerp.financeiro.aplicacao.RenomearCategoriaFinanceira;
import br.com.goerp.financeiro.dominio.TipoDeCategoria;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categorias-financeiras")
@RequiredArgsConstructor
public class CategoriaFinanceiraController {

    private final ListarCategoriasFinanceiras listar;
    private final BuscarCategoriaFinanceira buscar;
    private final CadastrarCategoriaFinanceira cadastrar;
    private final RenomearCategoriaFinanceira renomear;
    private final AlterarSituacaoDaCategoriaFinanceira alterarSituacao;

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCEIRO_LER')")
    public List<CategoriaFinanceiraResponse> listar(@RequestParam(required = false) TipoDeCategoria tipo,
            @RequestParam(required = false) Boolean ativa) {
        return listar.executar(tipo, ativa).stream().map(CategoriaFinanceiraResponse::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCEIRO_LER')")
    public CategoriaFinanceiraResponse buscar(@PathVariable Long id) {
        return CategoriaFinanceiraResponse.de(buscar.executar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONTA_FINANCEIRA_GERENCIAR')")
    public ResponseEntity<CategoriaFinanceiraResponse> cadastrar(
            @Valid @RequestBody CategoriaFinanceiraRequests.Cadastro request) {
        var criada = CategoriaFinanceiraResponse.de(cadastrar.executar(request.tipo(), request.nome()));
        return ResponseEntity.created(URI.create("/api/v1/categorias-financeiras/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTA_FINANCEIRA_GERENCIAR')")
    public CategoriaFinanceiraResponse renomear(@PathVariable Long id,
            @Valid @RequestBody CategoriaFinanceiraRequests.Renomeacao request) {
        return CategoriaFinanceiraResponse.de(renomear.executar(id, request.nome()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTA_FINANCEIRA_GERENCIAR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        alterarSituacao.executar(id, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('CONTA_FINANCEIRA_GERENCIAR')")
    public CategoriaFinanceiraResponse reativar(@PathVariable Long id) {
        return CategoriaFinanceiraResponse.de(alterarSituacao.executar(id, true));
    }
}
