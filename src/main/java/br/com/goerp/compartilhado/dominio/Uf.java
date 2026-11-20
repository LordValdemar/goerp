package br.com.goerp.compartilhado.dominio;

import java.util.Arrays;
import java.util.Locale;

import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/** Unidades da federação, com o código IBGE (os 2 primeiros dígitos do código de cada município). */
public enum Uf {
    RO(11), AC(12), AM(13), RR(14), PA(15), AP(16), TO(17),
    MA(21), PI(22), CE(23), RN(24), PB(25), PE(26), AL(27), SE(28), BA(29),
    MG(31), ES(32), RJ(33), SP(35),
    PR(41), SC(42), RS(43),
    MS(50), MT(51), GO(52), DF(53);

    private final int codigoIbge;

    Uf(int codigoIbge) {
        this.codigoIbge = codigoIbge;
    }

    public int codigoIbge() {
        return codigoIbge;
    }

    public static Uf de(String sigla) {
        return Arrays.stream(values())
                .filter(uf -> sigla != null && uf.name().equals(sigla.strip().toUpperCase(Locale.ROOT)))
                .findFirst()
                .orElseThrow(() -> new ValorInvalidoException("UF inválida: " + sigla));
    }
}
