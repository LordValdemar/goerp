package br.com.goerp.cadastros.fornecedor.dominio;

import java.util.Optional;

import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

/** Fornecedores da empresa atual (o filtro por empresa é automático). */
public interface FornecedorRepositorio {

    Fornecedor salvar(Fornecedor fornecedor);

    Optional<Fornecedor> buscarPorId(Long id);

    boolean existeComDocumento(Documento documento);

    Pagina<Fornecedor> pesquisar(FiltroDeFornecedores filtro, Paginacao paginacao);
}
