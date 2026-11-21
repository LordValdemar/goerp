package br.com.goerp.financeiro.web;

import static br.com.goerp.financeiro.dominio.NaturezaDoTitulo.A_PAGAR;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.web.PaginaResponse;
import br.com.goerp.financeiro.aplicacao.AlterarTitulo;
import br.com.goerp.financeiro.aplicacao.BaixarTitulo;
import br.com.goerp.financeiro.aplicacao.BuscarTitulo;
import br.com.goerp.financeiro.aplicacao.CancelarTitulo;
import br.com.goerp.financeiro.aplicacao.EstornarBaixa;
import br.com.goerp.financeiro.aplicacao.LancarTitulo;
import br.com.goerp.financeiro.aplicacao.PesquisarTitulos;
import br.com.goerp.financeiro.aplicacao.ResumirTitulos;
import br.com.goerp.financeiro.dominio.DadosDoTitulo;
import br.com.goerp.financeiro.dominio.FiltroDeTitulos;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.OrigemDoTitulo;
import br.com.goerp.financeiro.dominio.SituacaoDoTitulo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/contas-a-pagar")
@RequiredArgsConstructor
public class ContaAPagarController {

    private final LancarTitulo lancar;
    private final AlterarTitulo alterar;
    private final CancelarTitulo cancelar;
    private final BaixarTitulo baixar;
    private final EstornarBaixa estornarBaixa;
    private final BuscarTitulo buscar;
    private final PesquisarTitulos pesquisar;
    private final ResumirTitulos resumir;

    /** @param formaPrevista como se pretende pagar (ex.: BOLETO); padrão A_PRAZO */
    public record Lancamento(Long fornecedorId,
            @NotBlank @Size(max = TituloRequests.TAMANHO_DA_DESCRICAO) String descricao, Long categoriaId,
            @Size(max = TituloRequests.TAMANHO_DO_DOCUMENTO) String documento, FormaDePagamento formaPrevista,
            @NotNull @Valid TituloRequests.Vencimentos condicao) {
    }

    /** Uma conta por parcela; devolve todas. */
    @PostMapping
    @PreAuthorize("hasAuthority('TITULO_GERENCIAR')")
    public ResponseEntity<List<TituloResponse>> lancar(@Valid @RequestBody Lancamento request) {
        var dados = new DadosDoTitulo(A_PAGAR, request.fornecedorId(), request.descricao(), request.categoriaId(),
                request.documento(), request.formaPrevista(), OrigemDoTitulo.MANUAL, null);
        var criadas = lancar.executar(dados, request.condicao().parcelamento()).stream().map(TituloResponse::de).toList();
        return ResponseEntity.created(URI.create("/api/v1/contas-a-pagar/" + criadas.get(0).id())).body(criadas);
    }

    /** Por vencimento, mais antigas primeiro; {@code vencidas=true} só as vencidas até ontem. */
    @GetMapping
    @PreAuthorize("hasAuthority('FINANCEIRO_LER')")
    public PaginaResponse<TituloResponse> pesquisar(@RequestParam(required = false) Long fornecedorId,
            @RequestParam(required = false) SituacaoDoTitulo situacao,
            @RequestParam(required = false) Boolean emAberto,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimentoAte,
            @RequestParam(defaultValue = "false") boolean vencidas,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "" + Paginacao.TAMANHO_PADRAO) int tamanho) {
        var filtro = new FiltroDeTitulos(A_PAGAR, fornecedorId, situacao, emAberto, vencimentoDe, vencimentoAte, null);
        return PaginaResponse.de(pesquisar.executar(filtro, vencidas, new Paginacao(pagina, tamanho)),
                TituloResponse::de);
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasAuthority('FINANCEIRO_LER')")
    public ResumoDeTitulosResponse resumo() {
        return ResumoDeTitulosResponse.de(resumir.executar(A_PAGAR));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FINANCEIRO_LER')")
    public TituloResponse buscar(@PathVariable Long id) {
        return TituloResponse.de(buscar.executar(A_PAGAR, id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TITULO_GERENCIAR')")
    public TituloResponse alterar(@PathVariable Long id, @Valid @RequestBody TituloRequests.Alteracao request) {
        return TituloResponse.de(alterar.executar(A_PAGAR, id, request.versao(), request.descricao(),
                request.categoriaId(), request.documento(), Dinheiro.de(request.valor()), request.vencimento()));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('TITULO_GERENCIAR')")
    public TituloResponse cancelar(@PathVariable Long id, @Valid @RequestBody TituloRequests.Motivo request) {
        return TituloResponse.de(cancelar.executar(A_PAGAR, id, request.motivo()));
    }

    /** Paga (total ou parcial): tira o valor da conta informada, com a forma de pagamento usada. */
    @PostMapping("/{id}/pagamentos")
    @PreAuthorize("hasAuthority('TITULO_BAIXAR')")
    public TituloResponse pagar(@PathVariable Long id, @Valid @RequestBody TituloRequests.Baixa request) {
        return TituloResponse.de(baixar.executar(A_PAGAR, id, request.contaId(), request.forma(),
                Dinheiro.de(request.valorPago()), TituloRequests.Baixa.opcional(request.juros()),
                TituloRequests.Baixa.opcional(request.multa()), TituloRequests.Baixa.opcional(request.desconto())));
    }

    /** Desfaz o pagamento: o valor volta a ser devido e volta para a conta. */
    @PostMapping("/{id}/pagamentos/{numero}/estornar")
    @PreAuthorize("hasAuthority('TITULO_BAIXAR')")
    public TituloResponse estornarPagamento(@PathVariable Long id, @PathVariable int numero,
            @Valid @RequestBody TituloRequests.Motivo request) {
        return TituloResponse.de(estornarBaixa.executar(A_PAGAR, id, numero, request.motivo()));
    }
}
