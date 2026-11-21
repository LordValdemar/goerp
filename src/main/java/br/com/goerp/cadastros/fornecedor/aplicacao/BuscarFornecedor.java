package br.com.goerp.cadastros.fornecedor.aplicacao;

import br.com.goerp.cadastros.fornecedor.dominio.Fornecedor;
import br.com.goerp.cadastros.fornecedor.dominio.FornecedorRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;

@CasoDeUso
public class BuscarFornecedor {

    private final FornecedorRepositorio fornecedores;

    public BuscarFornecedor(FornecedorRepositorio fornecedores) {
        this.fornecedores = fornecedores;
    }

    public Fornecedor executar(Long id) {
        return fornecedores.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor", id));
    }
}
