package br.com.goerp.financeiro.web;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.web.PaginaResponse;
import br.com.goerp.financeiro.aplicacao.AlterarContaFinanceira;
import br.com.goerp.financeiro.aplicacao.AlterarSituacaoDaContaFinanceira;
import br.com.goerp.financeiro.aplicacao.BuscarContaFinanceira;
import br.com.goerp.financeiro.aplicacao.CadastrarContaFinanceira;
import br.com.goerp.financeiro.aplicacao.ConsultarExtratoFinanceiro;
import br.com.goerp.financeiro.aplicacao.ListarContasFinanceiras;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/contas-financeiras")
@RequiredArgsConstructor
public class ContaFinanceiraController {

    private final ListarContasFinanceiras listar;
    private final BuscarContaFinanceira buscar;
    private final CadastrarContaFinanceira cadastrar;
    private final AlterarContaFinanceira alterar;
    private final AlterarSituacaoDaContaFinanceira alterarSituacao;
    private final ConsultarExtratoFinanceiro consultarExtrato;

    @GetMapping
    @PreAuthorize("hasAuthority('FINANCEIRO_LER')")
    public List<ContaFinanceiraResponse> listar(@RequestParam(required = false) Boolean ativa) {
        return listar.executar(ativa).stream().map(ContaFinanceiraResponse::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCEIRO_LER')")
    public ContaFinanceiraResponse buscar(@PathVariable Long id) {
        return ContaFinanceiraResponse.de(buscar.executar(id));
    }

    /** A conta nasce com saldo zero; o saldo inicial entra como lançamento, para aparecer no extrato. */
    @PostMapping
    @PreAuthorize("hasAuthority('CONTA_FINANCEIRA_GERENCIAR')")
    public ResponseEntity<ContaFinanceiraResponse> cadastrar(
            @Valid @RequestBody ContaFinanceiraRequests.Cadastro request) {
        var criada = ContaFinanceiraResponse.de(cadastrar.executar(request.tipo(), request.nome(),
                request.permiteSaldoNegativo()));
        return ResponseEntity.created(URI.create("/api/v1/contas-financeiras/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTA_FINANCEIRA_GERENCIAR')")
    public ContaFinanceiraResponse alterar(@PathVariable Long id,
            @Valid @RequestBody ContaFinanceiraRequests.Alteracao request) {
        return ContaFinanceiraResponse.de(alterar.executar(id, request.versao(), request.nome(),
                request.permiteSaldoNegativo()));
    }

    /** Só com saldo zero. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTA_FINANCEIRA_GERENCIAR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        alterarSituacao.executar(id, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('CONTA_FINANCEIRA_GERENCIAR')")
    public ContaFinanceiraResponse reativar(@PathVariable Long id) {
        return ContaFinanceiraResponse.de(alterarSituacao.executar(id, true));
    }

    /** Mais recentes primeiro; {@code forma}, {@code de} e {@code ate} (inclusive) são opcionais. */
    @GetMapping("/{id}/extrato")
    @PreAuthorize("hasAuthority('FINANCEIRO_LER')")
    public PaginaResponse<LancamentoResponse> extrato(@PathVariable Long id,
            @RequestParam(required = false) FormaDePagamento forma,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + Paginacao.TAMANHO_PADRAO) int tamanho) {
        return PaginaResponse.de(consultarExtrato.executar(id, forma, de, ate, new Paginacao(pagina, tamanho)),
                LancamentoResponse::de);
    }
}
