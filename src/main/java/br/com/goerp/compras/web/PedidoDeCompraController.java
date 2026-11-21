package br.com.goerp.compras.web;

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
import br.com.goerp.compras.aplicacao.AlterarPedidoDeCompra;
import br.com.goerp.compras.aplicacao.AndamentoDoPedidoDeCompra;
import br.com.goerp.compras.aplicacao.ConsultarPedidosDeCompra;
import br.com.goerp.compras.aplicacao.CriarPedidoDeCompra;
import br.com.goerp.compras.dominio.SituacaoDoPedidoDeCompra;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pedidos-compra")
@RequiredArgsConstructor
public class PedidoDeCompraController {

    record Motivo(@NotBlank @Size(max = 300) String motivo) {
    }

    private final CriarPedidoDeCompra criarPedido;
    private final AlterarPedidoDeCompra alterarPedido;
    private final AndamentoDoPedidoDeCompra andamento;
    private final ConsultarPedidosDeCompra consultar;

    @PostMapping
    @PreAuthorize("hasAuthority('COMPRA_GERENCIAR')")
    public ResponseEntity<PedidoDeCompraResponse> criar(@Valid @RequestBody PedidoDeCompraRequest request) {
        if (request.fornecedorId() == null) {
            throw new CampoObrigatorioException("fornecedorId");
        }
        var criado = PedidoDeCompraResponse.de(criarPedido.executar(request.fornecedorId(), request.paraDados()));
        return ResponseEntity.created(URI.create("/api/v1/pedidos-compra/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPRA_GERENCIAR')")
    public PedidoDeCompraResponse alterar(@PathVariable Long id, @Valid @RequestBody PedidoDeCompraRequest request) {
        return PedidoDeCompraResponse.de(alterarPedido.executar(id, request.versao(), request.paraDados()));
    }

    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasAuthority('COMPRA_GERENCIAR')")
    public PedidoDeCompraResponse enviar(@PathVariable Long id) {
        return PedidoDeCompraResponse.de(andamento.enviar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('COMPRA_GERENCIAR')")
    public PedidoDeCompraResponse cancelar(@PathVariable Long id, @Valid @RequestBody Motivo request) {
        return PedidoDeCompraResponse.de(andamento.cancelar(id, request.motivo()));
    }

    @PostMapping("/{id}/encerrar-saldo")
    @PreAuthorize("hasAuthority('COMPRA_GERENCIAR')")
    public PedidoDeCompraResponse encerrarSaldo(@PathVariable Long id, @Valid @RequestBody Motivo request) {
        return PedidoDeCompraResponse.de(andamento.encerrarSaldo(id, request.motivo()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPRA_LER')")
    public PedidoDeCompraResponse buscar(@PathVariable Long id) {
        return PedidoDeCompraResponse.de(consultar.porId(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMPRA_LER')")
    public PaginaResponse<PedidoDeCompraResponse> pesquisar(
            @RequestParam(required = false) Long fornecedorId,
            @RequestParam(required = false) SituacaoDoPedidoDeCompra situacao,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(consultar.pesquisar(fornecedorId, situacao, new Paginacao(pagina, tamanho)),
                PedidoDeCompraResponse::de);
    }
}
