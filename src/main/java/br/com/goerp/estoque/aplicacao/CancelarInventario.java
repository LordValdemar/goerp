package br.com.goerp.estoque.aplicacao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.estoque.dominio.Inventario;
import br.com.goerp.estoque.dominio.InventarioRepositorio;

/** Descarta as contagens sem mexer no estoque. */
@CasoDeUso
public class CancelarInventario {

    private final InventarioRepositorio inventarios;
    private final BuscarInventario buscarInventario;
    private final Clock relogio;

    public CancelarInventario(InventarioRepositorio inventarios, BuscarInventario buscarInventario, Clock relogio) {
        this.inventarios = inventarios;
        this.buscarInventario = buscarInventario;
        this.relogio = relogio;
    }

    public Inventario executar(Long inventarioId) {
        Inventario inventario = buscarInventario.executar(inventarioId);
        inventario.cancelar(relogio.instant());
        return inventarios.salvar(inventario);
    }
}
