package br.com.goerp.compartilhado.web;

import java.util.List;
import java.util.function.Function;

import br.com.goerp.compartilhado.dominio.Pagina;

public record PaginaResponse<T>(List<T> itens, int numero, int tamanho, long total, int totalDePaginas) {

    public static <D, T> PaginaResponse<T> de(Pagina<D> pagina, Function<D, T> conversao) {
        Pagina<T> convertida = pagina.mapear(conversao);
        return new PaginaResponse<>(convertida.itens(), convertida.numero(), convertida.tamanho(), convertida.total(),
                convertida.totalDePaginas());
    }
}
