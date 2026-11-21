package br.com.goerp.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface VendaJpaRepository extends JpaRepository<VendaJpa, Long> {

    /** [forma, soma] dos pagamentos das vendas finalizadas do caixa. */
    @Query("""
            SELECT p.forma, SUM(p.valor) FROM Venda v JOIN v.pagamentos p
            WHERE v.caixaId = :caixaId AND v.situacao = br.com.goerp.vendas.dominio.SituacaoDaVenda.FINALIZADA
            GROUP BY p.forma""")
    List<Object[]> pagamentosPorForma(Long caixaId);

    @Query("""
            SELECT COALESCE(SUM(v.troco), 0) FROM Venda v
            WHERE v.caixaId = :caixaId AND v.situacao = br.com.goerp.vendas.dominio.SituacaoDaVenda.FINALIZADA""")
    BigDecimal trocoDado(Long caixaId);
}
