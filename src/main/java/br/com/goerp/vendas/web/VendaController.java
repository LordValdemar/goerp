package br.com.goerp.vendas.web;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.goerp.vendas.aplicacao.BuscarVenda;
import br.com.goerp.vendas.aplicacao.ConfigurarVendas;
import br.com.goerp.vendas.aplicacao.RegistrarVenda;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/vendas")
@RequiredArgsConstructor
public class VendaController {

    private static final String DESCONTO_MAXIMO = "descontoMaximoPercentual";

    record Configuracao(@NotNull BigDecimal descontoMaximoPercentual) {
    }

    private final RegistrarVenda registrarVenda;
    private final BuscarVenda buscarVenda;
    private final ConfigurarVendas configurarVendas;

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
