package br.com.goerp.cadastros.fornecedor.aplicacao;

import br.com.goerp.cadastros.fornecedor.dominio.Fornecedor;
import br.com.goerp.cadastros.fornecedor.dominio.FornecedorJaCadastradoException;
import br.com.goerp.cadastros.fornecedor.dominio.FornecedorRepositorio;
import br.com.goerp.cadastros.fornecedor.dominio.DadosDoFornecedor;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.dominio.Documento;

@CasoDeUso
public class CadastrarFornecedor {

    private final FornecedorRepositorio fornecedores;
    private final EmpresaAtual empresaAtual;

    public CadastrarFornecedor(FornecedorRepositorio fornecedores, EmpresaAtual empresaAtual) {
        this.fornecedores = fornecedores;
        this.empresaAtual = empresaAtual;
    }

    public Fornecedor executar(Documento documento, DadosDoFornecedor dados) {
        if (fornecedores.existeComDocumento(documento)) {
            throw new FornecedorJaCadastradoException(documento);
        }
        return fornecedores.salvar(Fornecedor.novo(empresaAtual.id(), documento, dados));
    }
}
