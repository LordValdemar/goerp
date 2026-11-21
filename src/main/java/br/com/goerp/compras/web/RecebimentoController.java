package br.com.goerp.compras.web;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compras.aplicacao.ConsultarRecebimentos;
import br.com.goerp.compras.aplicacao.EstornarRecebimento;
import br.com.goerp.compras.aplicacao.ItemInformadoNoRecebimento;
import br.com.goerp.compras.aplicacao.RegistrarRecebimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecebimentoController {

    /** @param custoUnitario o custo da nota; sem ele, vale o custo negociado no pedido */
    record Item(@NotNull Long produtoId, @NotNull BigDecimal quantidade, BigDecimal custoUnitario) {
    }

    record NovoRecebimento(@NotBlank @Size(max = 60) String notaFiscal, @NotEmpty List<@Valid Item> itens) {
    }

    record Estorno(@NotBlank @Size(max = 300) String motivo) {
    }

    private final RegistrarRecebimento registrarRecebimento;
    private final EstornarRecebimento estornarRecebimento;
    private final ConsultarRecebimentos consultar;

    @PostMapping("/pedidos-compra/{pedidoId}/recebimentos")
    @PreAuthorize("hasAuthority('COMPRA_RECEBER')")
    public ResponseEntity<RecebimentoResponse> receber(@PathVariable Long pedidoId,
            @Valid @RequestBody NovoRecebimento request) {
        var itens = request.itens().stream().map(item -> new ItemInformadoNoRecebimento(item.produtoId(),
                new Quantidade(item.quantidade()),
                item.custoUnitario() == null ? null : new CustoUnitario(item.custoUnitario()))).toList();
        var recebimento = RecebimentoResponse.de(registrarRecebimento.executar(pedidoId, request.notaFiscal(), itens));
        return ResponseEntity.created(URI.create("/api/v1/recebimentos/" + recebimento.id())).body(recebimento);
    }

    @GetMapping("/pedidos-compra/{pedidoId}/recebimentos")
    @PreAuthorize("hasAuthority('COMPRA_LER')")
    public List<RecebimentoResponse> doPedido(@PathVariable Long pedidoId) {
        return consultar.doPedido(pedidoId).stream().map(RecebimentoResponse::de).toList();
    }

    @GetMapping("/recebimentos/{id}")
    @PreAuthorize("hasAuthority('COMPRA_LER')")
    public RecebimentoResponse buscar(@PathVariable Long id) {
        return RecebimentoResponse.de(consultar.porId(id));
    }

    @PostMapping("/recebimentos/{id}/estornar")
    @PreAuthorize("hasAuthority('COMPRA_RECEBER')")
    public RecebimentoResponse estornar(@PathVariable Long id, @Valid @RequestBody Estorno request) {
        return RecebimentoResponse.de(estornarRecebimento.executar(id, request.motivo()));
    }
}
