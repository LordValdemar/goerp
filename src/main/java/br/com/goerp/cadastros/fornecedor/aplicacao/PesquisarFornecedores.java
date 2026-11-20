package br.com.goerp.cadastros.fornecedor.aplicacao;

import br.com.goerp.cadastros.fornecedor.dominio.Fornecedor;
import br.com.goerp.cadastros.fornecedor.dominio.FornecedorRepositorio;
import br.com.goerp.cadastros.fornecedor.dominio.FiltroDeFornecedores;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

@CasoDeUso
public class PesquisarFornecedores {

    private final FornecedorRepositorio fornecedores;

    public PesquisarFornecedores(FornecedorRepositorio fornecedores) {
        this.fornecedores = fornecedores;
    }

    public Pagina<Fornecedor> executar(FiltroDeFornecedores filtro, Paginacao paginacao) {
        return fornecedores.pesquisar(filtro, paginacao);
    }
}
