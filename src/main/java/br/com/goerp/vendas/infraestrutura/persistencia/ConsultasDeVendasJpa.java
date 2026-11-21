package br.com.goerp.vendas.infraestrutura.persistencia;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.infraestrutura.persistencia.Paginas;
import br.com.goerp.vendas.aplicacao.ConsultasDeVendas;
import br.com.goerp.vendas.aplicacao.FiltroDeVendas;
import br.com.goerp.vendas.aplicacao.ResumoDeVendas;
import br.com.goerp.vendas.dominio.FormaDePagamento;
import br.com.goerp.vendas.dominio.SituacaoDaVenda;
import br.com.goerp.vendas.dominio.Venda;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class ConsultasDeVendasJpa implements ConsultasDeVendas {

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Direction.DESC, "numero");

    private final VendaJpaRepository jpa;

    @Override
    public Pagina<Venda> pesquisar(FiltroDeVendas filtro, Paginacao paginacao) {
        return Paginas.daSpring(jpa.findAll(com(filtro, filtro.situacao()),
                Paginas.paraSpring(paginacao, MAIS_RECENTES_PRIMEIRO)), VendaRepositorioJpa::paraDominio);
    }

    /**
     * Soma em memória as vendas do período. Suficiente para o volume de um dia ou de um caixa;
     * para relatórios longos (módulo de relatórios), trocar por agregação no banco.
     */
    @Override
    public ResumoDeVendas resumir(FiltroDeVendas filtro) {
        List<VendaJpa> finalizadas = jpa.findAll(com(filtro, SituacaoDaVenda.FINALIZADA));
        List<VendaJpa> canceladas = jpa.findAll(com(filtro, SituacaoDaVenda.CANCELADA));

        Map<FormaDePagamento, Dinheiro> recebido = new EnumMap<>(FormaDePagamento.class);
        Dinheiro total = Dinheiro.ZERO;
        Dinheiro descontos = Dinheiro.ZERO;
        Dinheiro troco = Dinheiro.ZERO;
        for (VendaJpa registro : finalizadas) {
            Venda venda = VendaRepositorioJpa.paraDominio(registro);
            total = total.somar(venda.total());
            descontos = descontos.somar(venda.descontoTotal());
            troco = troco.somar(venda.troco());
            venda.pagamentos().forEach(p -> recebido.merge(p.forma(), p.valor(), Dinheiro::somar));
        }
        Dinheiro trocoDado = troco;
        recebido.computeIfPresent(FormaDePagamento.DINHEIRO, (forma, valor) -> valor.subtrair(trocoDado));
        Dinheiro totalCancelado = canceladas.stream().map(v -> Dinheiro.de(v.getTotal()))
                .reduce(Dinheiro.ZERO, Dinheiro::somar);
        return new ResumoDeVendas(finalizadas.size(), total, descontos, recebido, canceladas.size(), totalCancelado);
    }

    private static Specification<VendaJpa> com(FiltroDeVendas filtro, SituacaoDaVenda situacao) {
        return (venda, consulta, criterios) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (filtro.caixaId() != null) {
                condicoes.add(criterios.equal(venda.get("caixaId"), filtro.caixaId()));
            }
            if (filtro.de() != null) {
                condicoes.add(criterios.greaterThanOrEqualTo(venda.get("momento"), filtro.de()));
            }
            if (filtro.ate() != null) {
                condicoes.add(criterios.lessThan(venda.get("momento"), filtro.ate()));
            }
            if (situacao != null) {
                condicoes.add(criterios.equal(venda.get("situacao"), situacao));
            }
            return criterios.and(condicoes.toArray(Predicate[]::new));
        };
    }

}
