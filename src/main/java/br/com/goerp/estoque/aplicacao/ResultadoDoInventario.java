package br.com.goerp.estoque.aplicacao;

import java.util.List;

import br.com.goerp.estoque.dominio.Inventario;
import br.com.goerp.estoque.dominio.Movimentacao;

/** Inventário finalizado e os ajustes gerados (produtos sem diferença não geram ajuste). */
public record ResultadoDoInventario(Inventario inventario, List<Movimentacao> ajustes) {
}
