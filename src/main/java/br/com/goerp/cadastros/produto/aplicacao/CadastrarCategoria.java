package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.Categoria;
import br.com.goerp.cadastros.produto.dominio.CategoriaJaCadastradaException;
import br.com.goerp.cadastros.produto.dominio.CategoriaRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;

@CasoDeUso
public class CadastrarCategoria {

    private final CategoriaRepositorio categorias;
    private final EmpresaAtual empresaAtual;

    public CadastrarCategoria(CategoriaRepositorio categorias, EmpresaAtual empresaAtual) {
        this.categorias = categorias;
        this.empresaAtual = empresaAtual;
    }

    public Categoria executar(String nome) {
        Categoria categoria = Categoria.nova(empresaAtual.id(), nome);
        if (categorias.existeComNome(categoria.nome())) {
            throw new CategoriaJaCadastradaException(categoria.nome());
        }
        return categorias.salvar(categoria);
    }
}
