package br.com.goerp.estoque.web;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.estoque.aplicacao.AbrirInventario;
import br.com.goerp.estoque.aplicacao.BuscarInventario;
import br.com.goerp.estoque.aplicacao.CancelarInventario;
import br.com.goerp.estoque.aplicacao.FinalizarInventario;
import br.com.goerp.estoque.aplicacao.RegistrarContagem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/estoque/inventarios")
@RequiredArgsConstructor
public class InventarioController {

    record Abertura(@NotBlank @Size(max = 150) String descricao) {
    }

    record Contagem(@NotNull BigDecimal quantidade) {
    }

    private final AbrirInventario abrirInventario;
    private final BuscarInventario buscarInventario;
    private final RegistrarContagem registrarContagem;
    private final FinalizarInventario finalizarInventario;
    private final CancelarInventario cancelarInventario;

    @GetMapping
    @PreAuthorize("hasAuthority('ESTOQUE_LER')")
    public List<InventarioResponse> listar() {
        return buscarInventario.todos().stream().map(InventarioResponse::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ESTOQUE_LER')")
    public InventarioResponse buscar(@PathVariable Long id) {
        return InventarioResponse.de(buscarInventario.executar(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ESTOQUE_AJUSTAR')")
    public ResponseEntity<InventarioResponse> abrir(@Valid @RequestBody Abertura request) {
        var aberto = InventarioResponse.de(abrirInventario.executar(request.descricao()));
        return ResponseEntity.created(URI.create("/api/v1/estoque/inventarios/" + aberto.id())).body(aberto);
    }

    /** Quem conta o estoque pode não ter permissão de ajustar: basta movimentar. */
    @PutMapping("/{id}/contagens/{produtoId}")
    @PreAuthorize("hasAuthority('ESTOQUE_MOVIMENTAR') or hasAuthority('ESTOQUE_AJUSTAR')")
    public InventarioResponse contar(@PathVariable Long id, @PathVariable Long produtoId,
            @Valid @RequestBody Contagem request) {
        return InventarioResponse.de(registrarContagem.executar(id, produtoId, new Quantidade(request.quantidade())));
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasAuthority('ESTOQUE_AJUSTAR')")
    public InventarioResponse finalizar(@PathVariable Long id) {
        return InventarioResponse.de(finalizarInventario.executar(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('ESTOQUE_AJUSTAR')")
    public InventarioResponse cancelar(@PathVariable Long id) {
        return InventarioResponse.de(cancelarInventario.executar(id));
    }
}
