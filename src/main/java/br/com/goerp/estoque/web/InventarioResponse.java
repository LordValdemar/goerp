package br.com.goerp.estoque.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import br.com.goerp.estoque.aplicacao.ResultadoDoInventario;
import br.com.goerp.estoque.dominio.Inventario;
import br.com.goerp.estoque.dominio.SituacaoDoInventario;

public record InventarioResponse(
        Long id,
        String descricao,
        SituacaoDoInventario situacao,
        Instant abertoEm,
        Instant encerradoEm,
        List<Contagem> contagens,
        List<MovimentacaoResponse> ajustes) {

    public record Contagem(Long produtoId, BigDecimal quantidadeContada) {
    }

    static InventarioResponse de(Inventario inventario) {
        return de(inventario, List.of());
    }

    static InventarioResponse de(ResultadoDoInventario resultado) {
        return de(resultado.inventario(), resultado.ajustes().stream().map(MovimentacaoResponse::de).toList());
    }

    private static InventarioResponse de(Inventario inventario, List<MovimentacaoResponse> ajustes) {
        List<Contagem> contagens = inventario.contagens().entrySet().stream()
                .map(contagem -> new Contagem(contagem.getKey(), contagem.getValue().valor()))
                .toList();
        return new InventarioResponse(inventario.id(), inventario.descricao(), inventario.situacao(),
                inventario.abertoEm(), inventario.encerradoEm(), contagens, ajustes);
    }
}
