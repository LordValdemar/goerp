package br.com.goerp.compras.web;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.goerp.compras.aplicacao.ConsultarPedidosDeCompra.PedidoComPrazo;
import br.com.goerp.compras.aplicacao.PendenciasDeEntrega;
import br.com.goerp.compras.aplicacao.RecebimentosDoPeriodo;

final class ConsultasDeComprasResponses {

    private ConsultasDeComprasResponses() {
    }

    record Pendente(PedidoDeCompraResponse pedido, BigDecimal valorPendente) {
    }

    record Pendencias(List<Pendente> pedidos, BigDecimal valorAReceber, long atrasados) {

        static Pendencias de(PendenciasDeEntrega pendencias) {
            return new Pendencias(pendencias.pedidos().stream()
                    .map(p -> new Pendente(PedidoDeCompraResponse.de(new PedidoComPrazo(p.pedido(), p.atrasado())),
                            p.valorPendente().valor()))
                    .toList(), pendencias.valorAReceber().valor(), pendencias.atrasados());
        }
    }

    record Linha(RecebimentoResponse recebimento, Long fornecedorId, Long numeroDoPedido) {
    }

    record Recebimentos(List<Linha> recebimentos, BigDecimal total, Map<Long, BigDecimal> totalPorFornecedor) {

        static Recebimentos de(RecebimentosDoPeriodo periodo) {
            Map<Long, BigDecimal> porFornecedor = new LinkedHashMap<>();
            periodo.totalPorFornecedor().forEach((fornecedor, valor) -> porFornecedor.put(fornecedor, valor.valor()));
            return new Recebimentos(periodo.recebimentos().stream()
                    .map(l -> new Linha(RecebimentoResponse.de(l.recebimento()), l.fornecedorId(), l.numeroDoPedido()))
                    .toList(), periodo.total().valor(), porFornecedor);
        }
    }
}
