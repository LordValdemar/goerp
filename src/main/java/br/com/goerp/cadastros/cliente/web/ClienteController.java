package br.com.goerp.cadastros.cliente.web;

import java.net.URI;

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

import br.com.goerp.cadastros.cliente.aplicacao.AlterarCliente;
import br.com.goerp.cadastros.cliente.aplicacao.BuscarCliente;
import br.com.goerp.cadastros.cliente.aplicacao.CadastrarCliente;
import br.com.goerp.cadastros.cliente.aplicacao.InativarCliente;
import br.com.goerp.cadastros.cliente.aplicacao.PesquisarClientes;
import br.com.goerp.cadastros.cliente.aplicacao.ReativarCliente;
import br.com.goerp.cadastros.cliente.dominio.FiltroDeClientes;
import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.web.PaginaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final PesquisarClientes pesquisarClientes;
    private final BuscarCliente buscarCliente;
    private final CadastrarCliente cadastrarCliente;
    private final AlterarCliente alterarCliente;
    private final InativarCliente inativarCliente;
    private final ReativarCliente reativarCliente;

    @GetMapping
    @PreAuthorize("hasAuthority('CLIENTE_LER')")
    public PaginaResponse<ClienteResponse> pesquisar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return PaginaResponse.de(
                pesquisarClientes.executar(new FiltroDeClientes(busca, ativo), new Paginacao(pagina, tamanho)),
                ClienteResponse::de);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_LER')")
    public ClienteResponse buscar(@PathVariable Long id) {
        return ClienteResponse.de(buscarCliente.executar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLIENTE_GERENCIAR')")
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request) {
        var criado = ClienteResponse.de(cadastrarCliente.executar(Documento.de(request.documento()), request.paraDados()));
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_GERENCIAR')")
    public ClienteResponse alterar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return ClienteResponse.de(alterarCliente.executar(id, request.versao(), request.paraDados()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CLIENTE_GERENCIAR')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        inativarCliente.executar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reativar")
    @PreAuthorize("hasAuthority('CLIENTE_GERENCIAR')")
    public ClienteResponse reativar(@PathVariable Long id) {
        return ClienteResponse.de(reativarCliente.executar(id));
    }
}
