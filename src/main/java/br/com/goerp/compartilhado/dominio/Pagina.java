package br.com.goerp.compartilhado.dominio;

import java.util.List;
import java.util.function.Function;

/** Uma página de resultados, sem depender do Spring Data. */
public record Pagina<T>(List<T> itens, int numero, int tamanho, long total) {

    public Pagina {
        itens = List.copyOf(itens);
    }

    public int totalDePaginas() {
        return tamanho == 0 ? 0 : (int) ((total + tamanho - 1) / tamanho);
    }

    public <R> Pagina<R> mapear(Function<T, R> conversao) {
        return new Pagina<>(itens.stream().map(conversao).toList(), numero, tamanho, total);
    }
}
