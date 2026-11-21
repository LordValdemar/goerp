package br.com.goerp.compartilhado.dominio;

import java.util.Set;

import br.com.goerp.compartilhado.dominio.excecao.GtinInvalidoException;

/** Código de barras GTIN (EAN-8, UPC-12, EAN-13 ou GTIN-14), com dígito verificador conferido. */
public record Gtin(String valor) {

    private static final Set<Integer> TAMANHOS_VALIDOS = Set.of(8, 12, 13, 14);
    private static final int PESO_IMPAR_DA_DIREITA = 3;

    public Gtin {
        String digitos = Digitos.somente(valor);
        if (!TAMANHOS_VALIDOS.contains(digitos.length()) || !digitoVerificadorCorreto(digitos)) {
            throw new GtinInvalidoException(valor);
        }
        valor = digitos;
    }

    public static Gtin de(String valor) {
        return new Gtin(valor);
    }

    /** Da direita para a esquerda, sem o verificador: pesos 3, 1, 3, 1... */
    private static boolean digitoVerificadorCorreto(String digitos) {
        int ultimo = digitos.length() - 1;
        int soma = 0;
        for (int i = ultimo - 1, posicao = 0; i >= 0; i--, posicao++) {
            int peso = posicao % 2 == 0 ? PESO_IMPAR_DA_DIREITA : 1;
            soma += Digitos.naPosicao(digitos, i) * peso;
        }
        int verificador = (10 - soma % 10) % 10;
        return verificador == Digitos.naPosicao(digitos, ultimo);
    }
}
