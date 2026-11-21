package br.com.goerp.financeiro.dominio;

import java.time.Instant;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;

/**
 * Um pagamento registrado num título. Não é apagado: o estorno o marca e devolve o saldo devedor.
 *
 * @param numero sequência do pagamento dentro do título (1, 2, ...)
 */
public record Baixa(
        Long id,
        int numero,
        Long contaId,
        FormaDePagamento forma,
        Dinheiro valorPago,
        Dinheiro juros,
        Dinheiro multa,
        Dinheiro desconto,
        Long usuarioId,
        Instant momento,
        boolean estornada,
        String motivoDoEstorno,
        Instant estornadaEm) {

    public Baixa {
        Objects.requireNonNull(contaId, "contaId");
        Objects.requireNonNull(forma, "forma");
        Objects.requireNonNull(valorPago, "valorPago");
        Objects.requireNonNull(juros, "juros");
        Objects.requireNonNull(multa, "multa");
        Objects.requireNonNull(desconto, "desconto");
        Objects.requireNonNull(momento, "momento");
    }

    static Baixa de(int numero, PedidoDeBaixa pedido, Instant agora) {
        return new Baixa(null, numero, pedido.contaId(), pedido.forma(), pedido.valorPago(), pedido.juros(),
                pedido.multa(), pedido.desconto(), pedido.usuarioId(), agora, false, null, null);
    }

    public Dinheiro valorAmortizado() {
        return valorPago.somar(desconto).subtrair(juros).subtrair(multa);
    }

    Baixa estornar(String motivo, Instant agora) {
        if (estornada) {
            throw new BaixaJaEstornadaException();
        }
        return new Baixa(id, numero, contaId, forma, valorPago, juros, multa, desconto, usuarioId, momento, true, motivo,
                agora);
    }
}
