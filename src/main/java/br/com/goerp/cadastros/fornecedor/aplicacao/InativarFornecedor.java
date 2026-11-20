package br.com.goerp.cadastros.fornecedor.aplicacao;

import br.com.goerp.cadastros.fornecedor.dominio.Fornecedor;
import br.com.goerp.cadastros.fornecedor.dominio.FornecedorRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

@CasoDeUso
public class InativarFornecedor {

    private final FornecedorRepositorio fornecedores;
    private final BuscarFornecedor buscarFornecedor;

    public InativarFornecedor(FornecedorRepositorio fornecedores, BuscarFornecedor buscarFornecedor) {
        this.fornecedores = fornecedores;
        this.buscarFornecedor = buscarFornecedor;
    }

    public void executar(Long id) {
        Fornecedor fornecedor = buscarFornecedor.executar(id);
        fornecedor.inativar();
        fornecedores.salvar(fornecedor);
    }
}
