package br.com.goerp.estoque.aplicacao;

import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.estoque.dominio.Inventario;
import br.com.goerp.estoque.dominio.InventarioRepositorio;

@CasoDeUso
public class BuscarInventario {

    private final InventarioRepositorio inventarios;

    public BuscarInventario(InventarioRepositorio inventarios) {
        this.inventarios = inventarios;
    }

    public Inventario executar(Long id) {
        return inventarios.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Inventário", id));
    }

    public List<Inventario> todos() {
        return inventarios.listar();
    }
}
