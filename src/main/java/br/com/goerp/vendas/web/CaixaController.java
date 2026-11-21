package br.com.goerp.vendas.web;

import java.math.BigDecimal;
import java.net.URI;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.vendas.aplicacao.AbrirCaixa;
import br.com.goerp.vendas.aplicacao.ConsultarCaixa;
import br.com.goerp.vendas.aplicacao.FecharCaixa;
import br.com.goerp.vendas.aplicacao.MovimentarCaixa;
import br.com.goerp.vendas.dominio.FormaDePagamento;
import br.com.goerp.vendas.dominio.SituacaoDoCaixa;
import br.com.goerp.vendas.dominio.TipoDeMovimentoDeCaixa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/caixas")
@RequiredArgsConstructor
public class CaixaController {

    record Abertura(@NotNull BigDecimal trocoInicial) {
    }

    record Movimento(@NotNull BigDecimal valor, @Size(max = 300) String motivo) {
    }

    /** Quanto o operador contou em cada forma; forma não informada conta como zero. */
    record Fechamento(@NotNull Map<FormaDePagamento, BigDecimal> contado) {
    }

    private final AbrirCaixa abrirCaixa;
    private final ConsultarCaixa consultarCaixa;
    private final MovimentarCaixa movimentarCaixa;
    private final FecharCaixa fecharCaixa;

    @PostMapping
    @PreAuthorize("hasAuthority('CAIXA_OPERAR')")
    public ResponseEntity<CaixaResponse> abrir(@Valid @RequestBody Abertura request) {
        var aberto = CaixaResponse.de(abrirCaixa.executar(Dinheiro.de(request.trocoInicial())));
        return ResponseEntity.created(URI.create("/api/v1/caixas/" + aberto.id())).body(aberto);
    }

    /** O caixa aberto do usuário logado (404 se não houver). */
    @GetMapping("/meu")
    @PreAuthorize("hasAuthority('CAIXA_OPERAR')")
    public CaixaResponse meu() {
        return consultarCaixa.meuCaixaAberto().map(CaixaResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Caixa aberto do usuário", "atual"));
    }

    /** Operador vê o próprio caixa; o de outro só com CAIXA_GERENCIAR. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CAIXA_OPERAR') or hasAuthority('CAIXA_GERENCIAR')")
    public CaixaResponse buscar(@PathVariable Long id) {
        return CaixaResponse.de(consultarCaixa.porId(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CAIXA_GERENCIAR')")
    public List<CaixaResponse> listar(@RequestParam(required = false) SituacaoDoCaixa situacao) {
        return consultarCaixa.todos(situacao).stream().map(CaixaResponse::de).toList();
    }

    @PostMapping("/{id}/suprimentos")
    @PreAuthorize("hasAuthority('CAIXA_GERENCIAR')")
    public CaixaResponse suprimento(@PathVariable Long id, @Valid @RequestBody Movimento request) {
        return CaixaResponse.de(movimentarCaixa.executar(id, TipoDeMovimentoDeCaixa.SUPRIMENTO,
                Dinheiro.de(request.valor()), request.motivo()));
    }

    @PostMapping("/{id}/sangrias")
    @PreAuthorize("hasAuthority('CAIXA_GERENCIAR')")
    public CaixaResponse sangria(@PathVariable Long id, @Valid @RequestBody Movimento request) {
        return CaixaResponse.de(movimentarCaixa.executar(id, TipoDeMovimentoDeCaixa.SANGRIA,
                Dinheiro.de(request.valor()), request.motivo()));
    }

    /** O operador fecha o próprio caixa; o de outro só com CAIXA_GERENCIAR. */
    @PostMapping("/{id}/fechar")
    @PreAuthorize("hasAuthority('CAIXA_OPERAR') or hasAuthority('CAIXA_GERENCIAR')")
    public CaixaResponse fechar(@PathVariable Long id, @Valid @RequestBody Fechamento request) {
        Map<FormaDePagamento, Dinheiro> contado = new EnumMap<>(FormaDePagamento.class);
        request.contado().forEach((forma, valor) -> contado.put(forma, Dinheiro.de(valor)));
        return CaixaResponse.de(fecharCaixa.executar(id, contado));
    }
}
