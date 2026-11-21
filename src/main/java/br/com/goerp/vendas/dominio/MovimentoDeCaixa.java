package br.com.goerp.vendas.dominio;

import java.time.Instant;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

public record MovimentoDeCaixa(TipoDeMovimentoDeCaixa tipo, Dinheiro valor, String motivo, Long usuarioId,
        Instant momento) {

    public MovimentoDeCaixa {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(valor, "valor");
        Objects.requireNonNull(momento, "momento");
        if (valor.compareTo(Dinheiro.ZERO) <= 0) {
            throw new ValorInvalidoException("O valor do " + tipo.name().toLowerCase() + " precisa ser maior que zero");
        }
    }
}
