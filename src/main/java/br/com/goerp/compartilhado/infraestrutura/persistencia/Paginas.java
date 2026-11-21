package br.com.goerp.compartilhado.infraestrutura.persistencia;

import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

/** Converte entre a paginação da aplicação e a do Spring Data. */
public final class Paginas {

    private Paginas() {
    }

    public static Pageable paraSpring(Paginacao paginacao, Sort ordem) {
        return PageRequest.of(paginacao.numero(), paginacao.tamanho(), ordem);
    }

    public static <J, D> Pagina<D> daSpring(Page<J> pagina, Function<J, D> conversao) {
        return new Pagina<>(pagina.getContent().stream().map(conversao).toList(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements());
    }
}
