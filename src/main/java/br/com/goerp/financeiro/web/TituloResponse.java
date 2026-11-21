package br.com.goerp.financeiro.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.goerp.financeiro.aplicacao.TituloConsultado;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.OrigemDoTitulo;
import br.com.goerp.financeiro.dominio.SituacaoDoTitulo;
import br.com.goerp.financeiro.dominio.Titulo;

public record TituloResponse(Long id, NaturezaDoTitulo natureza, Long fornecedorId, Long clienteId, String descricao,
        Long categoriaId, String documento, FormaDePagamento formaPrevista, OrigemDoTitulo origem,
        Long documentoDeOrigem, int parcela, int parcelas, BigDecimal valor, LocalDate vencimento,
        BigDecimal saldoDevedor, BigDecimal totalPago, SituacaoDoTitulo situacao, boolean vencido, long diasEmAtraso,
        List<Pagamento> pagamentos, String motivoDoCancelamento, Instant canceladoEm, Long versao) {

    /** Cada pagamento com a forma e a conta usadas. */
    public record Pagamento(int numero, Long contaId, FormaDePagamento forma, BigDecimal valorPago, BigDecimal juros,
            BigDecimal multa, BigDecimal desconto, Long usuarioId, Instant momento, boolean estornado,
            String motivoDoEstorno, Instant estornadoEm) {
    }

    static TituloResponse de(TituloConsultado consultado) {
        Titulo t = consultado.titulo();
        boolean aPagar = t.natureza() == NaturezaDoTitulo.A_PAGAR;
        return new TituloResponse(t.id(), t.natureza(), aPagar ? t.pessoaId() : null, aPagar ? null : t.pessoaId(),
                t.descricao(), t.categoriaId(), t.documento(), t.formaPrevista(), t.origem(), t.documentoDeOrigem(),
                t.parcela(), t.parcelas(), t.valor().valor(), t.vencimento(), t.saldoDevedor().valor(),
                t.totalPago().valor(), t.situacao(), consultado.vencido(), consultado.diasEmAtraso(),
                t.baixas().stream().map(b -> new Pagamento(b.numero(), b.contaId(), b.forma(), b.valorPago().valor(),
                        b.juros().valor(), b.multa().valor(), b.desconto().valor(), b.usuarioId(), b.momento(),
                        b.estornada(), b.motivoDoEstorno(), b.estornadaEm())).toList(),
                t.motivoDoCancelamento(), t.canceladoEm(), t.versao());
    }
}
