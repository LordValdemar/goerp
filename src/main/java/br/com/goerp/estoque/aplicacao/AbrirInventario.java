package br.com.goerp.estoque.aplicacao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.estoque.dominio.Inventario;
import br.com.goerp.estoque.dominio.InventarioJaAbertoException;
import br.com.goerp.estoque.dominio.InventarioRepositorio;

/** Só um inventário aberto por vez: dois ao mesmo tempo poderiam ajustar o mesmo produto duas vezes. */
@CasoDeUso
public class AbrirInventario {

    private final InventarioRepositorio inventarios;
    private final EmpresaAtual empresaAtual;
    private final Clock relogio;

    public AbrirInventario(InventarioRepositorio inventarios, EmpresaAtual empresaAtual, Clock relogio) {
        this.inventarios = inventarios;
        this.empresaAtual = empresaAtual;
        this.relogio = relogio;
    }

    public Inventario executar(String descricao) {
        if (inventarios.existeAberto()) {
            throw new InventarioJaAbertoException();
        }
        return inventarios.salvar(Inventario.abrir(empresaAtual.id(), descricao, relogio.instant()));
    }
}
