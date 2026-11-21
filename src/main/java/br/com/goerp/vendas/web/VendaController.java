package br.com.goerp.vendas.web;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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

import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.web.PaginaResponse;
import br.com.goerp.vendas.aplicacao.BuscarVenda;
import br.com.goerp.vendas.aplicacao.CancelarVenda;
import br.com.goerp.vendas.aplicacao.ConsultarVendas;
import br.com.goerp.vendas.aplicacao.FiltroDeVendas;
import br.com.goerp.vendas.aplicacao.ConfigurarVendas;
import br.com.goerp.vendas.aplicacao.RegistrarVenda;
import br.com.goerp.vendas.dominio.SituacaoDaVenda;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/vendas")
public class VendaController {

    private static final String DESCONTO_MAXIMO = "descontoMaximoPercentual";

    record Configuracao(@NotNull BigDecimal descontoMaximoPercentual) {
    }

    record Cancelamento(@NotBlank @Size(max = 300) String motivo) {
    }

    private final RegistrarVenda registrarVenda;
    private final BuscarVenda buscarVenda;
    private final ConfigurarVendas configurarVendas;
    private final CancelarVenda cancelarVenda;
    private final ConsultarVendas consultarVendas;
    private final ZoneId fusoHorario;

    public VendaController(RegistrarVenda registrarVenda, BuscarVenda buscarVenda, ConfigurarVendas configurarVendas,
            CancelarVenda cancelarVenda, ConsultarVendas consultarVendas,
            @Value("${goerp.fuso-horario}") String fusoHorario) {
        this.registrarVenda = registrarVenda;
        this.buscarVenda = buscarVenda;
        this.configurarVendas = configurarVendas;
        this.cancelarVenda = cancelarVenda;
        this.consultarVendas = consultarVendas;
        this.fusoHorario = ZoneId.of(fusoHorario);
    }

    /** Venda no PDV: exige caixa aberto do operador. */
    @PostMapping
    @PreAuthorize("hasAuthority('CAIXA_OPERAR')")
    public ResponseEntity<VendaResponse> registrar(@Valid @RequestBody VendaRequest request) {
        var venda = VendaResponse.de(registrarVenda.executar(request.paraSolicitacao()));
        return ResponseEntity.created(URI.create("/api/v1/vendas/" + venda.id())).body(venda);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDA_LER') or hasAuthority('CAIXA_OPERAR')")
    public VendaResponse buscar(@PathVariable Long id) {
        return VendaResponse.de(buscarVenda.executar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('VENDA_CANCELAR')")
    public VendaResponse cancelar(@PathVariable Long id, @Valid @RequestBody Cancelamento request) {
        return VendaResponse.de(cancelarVenda.executar(id, request.motivo()));
    }

    /** Vendas de um caixa e/ou de um dia (no fuso da empresa), da mais recente para a mais antiga. */
    @GetMapping
    @PreAuthorize("hasAuthority('VENDA_LER')")
    public PaginaResponse<VendaResponse> pesquisar(
            @RequestParam(required = false) Long caixaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) SituacaoDaVenda situacao,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(consultarVendas.pesquisar(filtro(caixaId, data, situacao),
                new Paginacao(pagina, tamanho)), VendaResponse::de);
    }

    /** Resumo do dia (ou de um caixa): quantidade, total, ticket médio, por forma e cancelamentos. */
    @GetMapping("/resumo")
    @PreAuthorize("hasAuthority('VENDA_LER')")
    public ResumoDeVendasResponse resumo(
            @RequestParam(required = false) Long caixaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResumoDeVendasResponse.de(consultarVendas.resumir(filtro(caixaId, data, null)));
    }

    private FiltroDeVendas filtro(Long caixaId, LocalDate data, SituacaoDaVenda situacao) {
        Instant inicio = data == null ? null : data.atStartOfDay(fusoHorario).toInstant();
        Instant fim = data == null ? null : data.plusDays(1).atStartOfDay(fusoHorario).toInstant();
        return new FiltroDeVendas(caixaId, inicio, fim, situacao);
    }

    @GetMapping("/configuracao")
    @PreAuthorize("hasAuthority('CAIXA_OPERAR') or hasAuthority('CAIXA_GERENCIAR')")
    public Map<String, BigDecimal> configuracao() {
        return Map.of(DESCONTO_MAXIMO, configurarVendas.descontoMaximo());
    }

    @PutMapping("/configuracao")
    @PreAuthorize("hasAuthority('CAIXA_GERENCIAR')")
    public Map<String, BigDecimal> configurar(@Valid @RequestBody Configuracao request) {
        return Map.of(DESCONTO_MAXIMO, configurarVendas.definirDescontoMaximo(request.descontoMaximoPercentual()));
    }
}
