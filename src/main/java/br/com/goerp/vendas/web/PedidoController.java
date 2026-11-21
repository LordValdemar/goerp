package br.com.goerp.vendas.web;

import java.net.URI;

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
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.web.PaginaResponse;
import br.com.goerp.vendas.aplicacao.pedido.AlterarPedido;
import br.com.goerp.vendas.aplicacao.pedido.AndamentoDoPedido;
import br.com.goerp.vendas.aplicacao.pedido.ConsultarPedidos;
import br.com.goerp.vendas.aplicacao.pedido.CriarPedido;
import br.com.goerp.vendas.dominio.SituacaoDoPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pedidos-venda")
@RequiredArgsConstructor
public class PedidoController {

    record Cancelamento(@NotBlank @Size(max = 300) String motivo) {
    }

    private final CriarPedido criarPedido;
    private final AlterarPedido alterarPedido;
    private final AndamentoDoPedido andamento;
    private final ConsultarPedidos consultarPedidos;

    @PostMapping
    @PreAuthorize("hasAuthority('PEDIDO_GERENCIAR')")
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        if (request.clienteId() == null) {
            throw new CampoObrigatorioException("clienteId");
        }
        var criado = PedidoResponse.de(criarPedido.executar(request.clienteId(), request.paraDados()));
        return ResponseEntity.created(URI.create("/api/v1/pedidos-venda/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_GERENCIAR')")
    public PedidoResponse alterar(@PathVariable Long id, @Valid @RequestBody PedidoRequest request) {
        return PedidoResponse.de(alterarPedido.executar(id, request.versao(), request.paraDados()));
    }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAuthority('PEDIDO_GERENCIAR')")
    public PedidoResponse confirmar(@PathVariable Long id) {
        return PedidoResponse.de(andamento.confirmar(id));
    }

    @PostMapping("/{id}/faturar")
    @PreAuthorize("hasAuthority('PEDIDO_FATURAR')")
    public PedidoResponse faturar(@PathVariable Long id) {
        return PedidoResponse.de(andamento.faturar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('PEDIDO_GERENCIAR')")
    public PedidoResponse cancelar(@PathVariable Long id, @Valid @RequestBody Cancelamento request) {
        return PedidoResponse.de(andamento.cancelar(id, request.motivo()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_LER')")
    public PedidoResponse buscar(@PathVariable Long id) {
        return PedidoResponse.de(consultarPedidos.porId(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PEDIDO_LER')")
    public PaginaResponse<PedidoResponse> pesquisar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) SituacaoDoPedido situacao,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(consultarPedidos.pesquisar(clienteId, situacao, new Paginacao(pagina, tamanho)),
                PedidoResponse::de);
    }
}
