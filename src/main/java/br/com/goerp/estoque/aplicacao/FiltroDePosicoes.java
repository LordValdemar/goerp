package br.com.goerp.estoque.aplicacao;

/**
 * @param texto          parte da descrição ou do código (opcional)
 * @param somenteARepor  só produtos no estoque mínimo ou abaixo
 */
public record FiltroDePosicoes(String texto, boolean somenteARepor) {

    public FiltroDePosicoes {
        texto = texto == null ? "" : texto.strip();
    }
}
