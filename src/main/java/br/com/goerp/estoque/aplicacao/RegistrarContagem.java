package br.com.goerp.estoque.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.estoque.dominio.Inventario;
import br.com.goerp.estoque.dominio.InventarioRepositorio;
import br.com.goerp.estoque.dominio.ProdutoSemControleDeEstoqueException;
import br.com.goerp.estoque.dominio.QuantidadeFracionadaException;
import br.com.goerp.estoque.dominio.RegrasDeEstoque;

/** Registra (ou corrige) quanto foi contado de um produto. O estoque só muda ao finalizar. */
@CasoDeUso
public class RegistrarContagem {

    private final InventarioRepositorio inventarios;
    private final BuscarInventario buscarInventario;
    private final MovimentadorDeEstoque movimentador;

    public RegistrarContagem(InventarioRepositorio inventarios, BuscarInventario buscarInventario,
            MovimentadorDeEstoque movimentador) {
        this.inventarios = inventarios;
        this.buscarInventario = buscarInventario;
        this.movimentador = movimentador;
    }

    public Inventario executar(Long inventarioId, Long produtoId, Quantidade contada) {
        Inventario inventario = buscarInventario.executar(inventarioId);
        RegrasDeEstoque regras = movimentador.regrasDo(produtoId);
        if (!regras.controlaEstoque()) {
            throw new ProdutoSemControleDeEstoqueException();
        }
        if (!regras.aceitaFracao() && !contada.inteira()) {
            throw new QuantidadeFracionadaException();
        }
        inventario.registrarContagem(produtoId, contada);
        return inventarios.salvar(inventario);
    }
}
