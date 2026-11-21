package br.com.goerp.vendas.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.vendas.dominio.Venda;
import br.com.goerp.vendas.dominio.VendaRepositorio;

@CasoDeUso
public class BuscarVenda {

    private final VendaRepositorio vendas;

    public BuscarVenda(VendaRepositorio vendas) {
        this.vendas = vendas;
    }

    public Venda executar(Long id) {
        return vendas.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Venda", id));
    }
}
