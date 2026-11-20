package br.com.goerp.cadastros.cliente.infraestrutura.persistencia;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import br.com.goerp.cadastros.cliente.dominio.FiltroDeClientes;
import br.com.goerp.compartilhado.dominio.Digitos;
import jakarta.persistence.criteria.Predicate;

/**
 * Monta a consulta só com os filtros informados. O texto procura em qualquer parte do nome e do
 * nome fantasia e no começo do documento (só se o texto tiver dígitos).
 */
final class PesquisaDeClientes {

    private PesquisaDeClientes() {
    }

    static Specification<ClienteJpa> com(FiltroDeClientes filtro) {
        return (cliente, consulta, criterios) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (filtro.ativo() != null) {
                condicoes.add(criterios.equal(cliente.get("ativo"), filtro.ativo()));
            }
            if (filtro.texto() != null) {
                String trecho = "%" + filtro.texto().toLowerCase(Locale.ROOT) + "%";
                List<Predicate> alternativas = new ArrayList<>();
                alternativas.add(criterios.like(criterios.lower(cliente.get("nome")), trecho));
                alternativas.add(criterios.like(criterios.lower(cliente.get("nomeFantasia")), trecho));
                String digitos = Digitos.somente(filtro.texto());
                if (!digitos.isEmpty()) {
                    alternativas.add(criterios.like(cliente.get("documento").as(String.class), digitos + "%"));
                }
                condicoes.add(criterios.or(alternativas.toArray(Predicate[]::new)));
            }
            return criterios.and(condicoes.toArray(Predicate[]::new));
        };
    }
}
