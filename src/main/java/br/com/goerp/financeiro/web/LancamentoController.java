package br.com.goerp.financeiro.web;

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

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.financeiro.aplicacao.BuscarLancamento;
import br.com.goerp.financeiro.aplicacao.EstornarLancamento;
import br.com.goerp.financeiro.aplicacao.RegistrarLancamento;
import br.com.goerp.financeiro.aplicacao.TransferirEntreContas;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LancamentoController {

    private final RegistrarLancamento registrar;
    private final TransferirEntreContas transferir;
    private final EstornarLancamento estornar;
    private final BuscarLancamento buscar;

    public record TransferenciaResponse(long numero, LancamentoResponse saida, LancamentoResponse entrada) {
    }

    @PostMapping("/lancamentos-financeiros")
    @PreAuthorize("hasAuthority('LANCAMENTO_REGISTRAR')")
    public ResponseEntity<LancamentoResponse> registrar(@Valid @RequestBody LancamentoRequests.Avulso request) {
        var criado = LancamentoResponse.de(registrar.executar(request.contaId(), request.sentido(),
                Dinheiro.de(request.valor()), request.forma(), request.categoriaId(), request.descricao()));
        return ResponseEntity.created(URI.create("/api/v1/lancamentos-financeiros/" + criado.id())).body(criado);
    }

    @GetMapping("/lancamentos-financeiros/{id}")
    @PreAuthorize("hasAuthority('FINANCEIRO_LER')")
    public LancamentoResponse buscar(@PathVariable Long id) {
        return LancamentoResponse.de(buscar.executar(id));
    }

    /** Avulso ou transferência (estorna as duas pontas); devolve os estornos lançados. */
    @PostMapping("/lancamentos-financeiros/{id}/estornar")
    @PreAuthorize("hasAuthority('LANCAMENTO_REGISTRAR')")
    public List<LancamentoResponse> estornar(@PathVariable Long id,
            @Valid @RequestBody LancamentoRequests.Estorno request) {
        return estornar.executar(id, request.motivo()).stream().map(LancamentoResponse::de).toList();
    }

    @PostMapping("/transferencias-financeiras")
    @PreAuthorize("hasAuthority('LANCAMENTO_REGISTRAR')")
    public ResponseEntity<TransferenciaResponse> transferir(
            @Valid @RequestBody LancamentoRequests.Transferencia request) {
        var feita = transferir.executar(request.contaOrigemId(), request.contaDestinoId(), Dinheiro.de(request.valor()),
                request.forma(), request.descricao());
        return ResponseEntity.status(201).body(new TransferenciaResponse(feita.numero(),
                LancamentoResponse.de(feita.saida()), LancamentoResponse.de(feita.entrada())));
    }
}
