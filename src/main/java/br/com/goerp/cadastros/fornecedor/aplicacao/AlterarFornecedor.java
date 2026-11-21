package br.com.goerp.cadastros.fornecedor.aplicacao;

import java.util.Objects;

import br.com.goerp.cadastros.fornecedor.dominio.Fornecedor;
import br.com.goerp.cadastros.fornecedor.dominio.FornecedorRepositorio;
import br.com.goerp.cadastros.fornecedor.dominio.DadosDoFornecedor;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;

/**
 * Recebe a versão que o usuário leu: se o fornecedor mudou desde então, recusa (409) em vez de
 * sobrescrever a alteração de outra pessoa.
 */
@CasoDeUso
public class AlterarFornecedor {

    private final FornecedorRepositorio fornecedores;
    private final BuscarFornecedor buscarFornecedor;

    public AlterarFornecedor(FornecedorRepositorio fornecedores, BuscarFornecedor buscarFornecedor) {
        this.fornecedores = fornecedores;
        this.buscarFornecedor = buscarFornecedor;
    }

    public Fornecedor executar(Long id, Long versaoLida, DadosDoFornecedor dados) {
        if (versaoLida == null) {
            throw new CampoObrigatorioException("versao");
        }
        Fornecedor fornecedor = buscarFornecedor.executar(id);
        if (!Objects.equals(fornecedor.versao(), versaoLida)) {
            throw new ConflitoDeEdicaoException("Fornecedor", id);
        }
        fornecedor.alterar(dados);
        return fornecedores.salvar(fornecedor);
    }
}
