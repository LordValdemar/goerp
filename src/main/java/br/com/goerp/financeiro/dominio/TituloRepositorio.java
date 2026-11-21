package br.com.goerp.financeiro.dominio;

import java.util.List;
import java.util.Optional;

import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

/** Títulos da empresa atual (o filtro por empresa é automático). */
public interface TituloRepositorio {

    /** Grava o título e seus pagamentos (os novos recebem id). */
    Titulo salvar(Titulo titulo);

    Optional<Titulo> buscarPorId(Long id);

    /** Busca e trava o título até o fim da transação (dois pagamentos simultâneos esperam a vez). */
    Optional<Titulo> travar(Long id);

    /** Por vencimento, mais antigos primeiro. */
    Pagina<Titulo> pesquisar(FiltroDeTitulos filtro, Paginacao paginacao);

    TotalDeTitulos totalizar(FiltroDeTitulos filtro);

    List<Titulo> doDocumento(OrigemDoTitulo origem, Long documentoDeOrigem);
}
