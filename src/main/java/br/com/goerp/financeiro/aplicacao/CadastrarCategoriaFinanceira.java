package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.financeiro.dominio.CategoriaFinanceira;
import br.com.goerp.financeiro.dominio.CategoriaFinanceiraJaCadastradaException;
import br.com.goerp.financeiro.dominio.CategoriaFinanceiraRepositorio;
import br.com.goerp.financeiro.dominio.TipoDeCategoria;

@CasoDeUso
public class CadastrarCategoriaFinanceira {

    private final CategoriaFinanceiraRepositorio categorias;
    private final EmpresaAtual empresaAtual;

    public CadastrarCategoriaFinanceira(CategoriaFinanceiraRepositorio categorias, EmpresaAtual empresaAtual) {
        this.categorias = categorias;
        this.empresaAtual = empresaAtual;
    }

    public CategoriaFinanceira executar(TipoDeCategoria tipo, String nome) {
        CategoriaFinanceira categoria = CategoriaFinanceira.nova(empresaAtual.id(), tipo, nome);
        if (categorias.existeComNome(categoria.nome())) {
            throw new CategoriaFinanceiraJaCadastradaException(categoria.nome());
        }
        return categorias.salvar(categoria);
    }
}
