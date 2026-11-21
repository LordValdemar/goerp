package br.com.goerp.estoque.dominio;

import java.util.List;
import java.util.Optional;

import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

/** Movimentações da empresa atual. Só inclui e consulta: movimentação não se altera nem se apaga. */
public interface MovimentacaoRepositorio {

    Movimentacao registrar(Movimentacao movimentacao);

    Optional<Movimentacao> buscarPorId(Long id);

    boolean foiEstornada(Long id);

    /** Movimentações do produto, da mais recente para a mais antiga. */
    Pagina<Movimentacao> extrato(Long produtoId, Paginacao paginacao);

    /** Movimentações (não estornos) geradas por um documento de outro módulo, ex.: uma venda. */
    List<Movimentacao> doDocumento(OrigemDaMovimentacao origem, Long documentoDeOrigem);
}
