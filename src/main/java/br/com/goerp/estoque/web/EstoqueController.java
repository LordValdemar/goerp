package br.com.goerp.estoque.web;

import java.net.URI;
import java.util.List;
import java.util.Map;

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

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.web.PaginaResponse;
import br.com.goerp.estoque.aplicacao.AjustarEstoque;
import br.com.goerp.estoque.aplicacao.BuscarMovimentacao;
import br.com.goerp.estoque.aplicacao.ConferirSaldos;
import br.com.goerp.estoque.aplicacao.ConsultarExtrato;
import br.com.goerp.estoque.aplicacao.ConsultarPosicaoDeEstoque;
import br.com.goerp.estoque.aplicacao.DivergenciaDeSaldo;
import br.com.goerp.estoque.aplicacao.FiltroDePosicoes;
import br.com.goerp.estoque.aplicacao.ConfigurarEstoque;
import br.com.goerp.estoque.aplicacao.ConsultarConfiguracaoDeEstoque;
import br.com.goerp.estoque.aplicacao.ConsultarSaldo;
import br.com.goerp.estoque.aplicacao.EstornarMovimentacao;
import br.com.goerp.estoque.aplicacao.RegistrarEntrada;
import br.com.goerp.estoque.aplicacao.RegistrarSaida;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private static final String CONFIGURACAO = "permiteEstoqueNegativo";

    private final RegistrarEntrada registrarEntrada;
    private final RegistrarSaida registrarSaida;
    private final AjustarEstoque ajustarEstoque;
    private final EstornarMovimentacao estornarMovimentacao;
    private final ConsultarSaldo consultarSaldo;
    private final ConfigurarEstoque configurarEstoque;
    private final ConsultarConfiguracaoDeEstoque consultarConfiguracao;
    private final ConsultarPosicaoDeEstoque consultarPosicao;
    private final ConsultarExtrato consultarExtrato;
    private final BuscarMovimentacao buscarMovimentacao;
    private final ConferirSaldos conferirSaldos;

    @PostMapping("/entradas")
    @PreAuthorize("hasAuthority('ESTOQUE_MOVIMENTAR')")
    public ResponseEntity<MovimentacaoResponse> entrada(@Valid @RequestBody EstoqueRequests.Entrada request) {
        return criada(registrarEntrada.executar(request.produtoId(), new Quantidade(request.quantidade()),
                request.custoUnitario() == null ? null : new CustoUnitario(request.custoUnitario()),
                OrigemDaMovimentacao.MANUAL, request.documentoDeOrigem(), request.observacao()));
    }

    @PostMapping("/saidas")
    @PreAuthorize("hasAuthority('ESTOQUE_MOVIMENTAR')")
    public ResponseEntity<MovimentacaoResponse> saida(@Valid @RequestBody EstoqueRequests.Saida request) {
        return criada(registrarSaida.executar(request.produtoId(), new Quantidade(request.quantidade()),
                OrigemDaMovimentacao.MANUAL, null, request.motivo()));
    }

    @PostMapping("/ajustes")
    @PreAuthorize("hasAuthority('ESTOQUE_AJUSTAR')")
    public ResponseEntity<MovimentacaoResponse> ajuste(@Valid @RequestBody EstoqueRequests.Ajuste request) {
        return criada(ajustarEstoque.executar(request.produtoId(), request.sentido(),
                new Quantidade(request.quantidade()), request.motivo()));
    }

    @PostMapping("/movimentacoes/{id}/estorno")
    @PreAuthorize("hasAuthority('ESTOQUE_AJUSTAR')")
    public ResponseEntity<MovimentacaoResponse> estorno(@PathVariable Long id,
            @Valid @RequestBody EstoqueRequests.Estorno request) {
        return criada(estornarMovimentacao.executar(id, request.motivo()));
    }

    @GetMapping("/saldos/{produtoId}")
    @PreAuthorize("hasAuthority('ESTOQUE_LER')")
    public SaldoResponse saldo(@PathVariable Long produtoId) {
        return SaldoResponse.de(consultarSaldo.executar(produtoId));
    }

    /** Posição de estoque; {@code aRepor=true} lista só o que está no mínimo ou abaixo. */
    @GetMapping("/saldos")
    @PreAuthorize("hasAuthority('ESTOQUE_LER')")
    public PaginaResponse<PosicaoResponse> posicoes(
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "false") boolean aRepor,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(consultarPosicao.executar(new FiltroDePosicoes(busca, aRepor),
                new Paginacao(pagina, tamanho)), PosicaoResponse::de);
    }

    @GetMapping("/produtos/{produtoId}/extrato")
    @PreAuthorize("hasAuthority('ESTOQUE_LER')")
    public PaginaResponse<MovimentacaoResponse> extrato(@PathVariable Long produtoId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(consultarExtrato.executar(produtoId, new Paginacao(pagina, tamanho)),
                MovimentacaoResponse::de);
    }

    @GetMapping("/movimentacoes/{id}")
    @PreAuthorize("hasAuthority('ESTOQUE_LER')")
    public MovimentacaoResponse movimentacao(@PathVariable Long id) {
        return MovimentacaoResponse.de(buscarMovimentacao.executar(id));
    }

    /** Recalcula os saldos pelas movimentações; {@code consistente=false} indica alteração por fora do sistema. */
    @GetMapping("/conferencia")
    @PreAuthorize("hasAuthority('ESTOQUE_AJUSTAR')")
    public Map<String, Object> conferencia() {
        List<DivergenciaDeSaldo> divergencias = conferirSaldos.executar();
        return Map.of("consistente", divergencias.isEmpty(), "divergencias", divergencias);
    }

    @GetMapping("/configuracao")
    @PreAuthorize("hasAuthority('ESTOQUE_LER')")
    public Map<String, Boolean> configuracao() {
        return Map.of(CONFIGURACAO, consultarConfiguracao.permiteEstoqueNegativo());
    }

    @PutMapping("/configuracao")
    @PreAuthorize("hasAuthority('ESTOQUE_AJUSTAR')")
    public Map<String, Boolean> configurar(@RequestBody EstoqueRequests.Configuracao request) {
        return Map.of(CONFIGURACAO, configurarEstoque.executar(request.permiteEstoqueNegativo()));
    }

    private static ResponseEntity<MovimentacaoResponse> criada(Movimentacao movimentacao) {
        return ResponseEntity.created(URI.create("/api/v1/estoque/movimentacoes/" + movimentacao.id()))
                .body(MovimentacaoResponse.de(movimentacao));
    }
}
