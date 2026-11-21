package br.com.goerp.compartilhado.infraestrutura.persistencia;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import br.com.goerp.compartilhado.dominio.Digitos;
import jakarta.persistence.criteria.Predicate;

/**
 * Filtros reutilizáveis para pesquisas. Cada um só entra na consulta se o valor foi informado,
 * o que evita parâmetros nulos (que o PostgreSQL não consegue tipar).
 */
public final class Especificacoes {

    private Especificacoes() {
    }

    public static <T> Specification<T> situacao(Boolean ativo) {
        return (raiz, consulta, criterios) -> ativo == null ? null : criterios.equal(raiz.get("ativo"), ativo);
    }

    /**
     * Texto em qualquer parte dos campos de texto informados, ou no começo do campo de código
     * numérico (ex.: documento) quando o texto contém dígitos.
     */
    public static <T> Specification<T> texto(String texto, String campoNumerico, String... camposDeTexto) {
        return (raiz, consulta, criterios) -> {
            if (texto == null || texto.isBlank()) {
                return null;
            }
            String trecho = "%" + texto.strip().toLowerCase(Locale.ROOT) + "%";
            List<Predicate> alternativas = new ArrayList<>();
            for (String campo : camposDeTexto) {
                alternativas.add(criterios.like(criterios.lower(raiz.get(campo)), trecho));
            }
            String digitos = Digitos.somente(texto);
            if (campoNumerico != null && !digitos.isEmpty()) {
                alternativas.add(criterios.like(raiz.get(campoNumerico).as(String.class), digitos + "%"));
            }
            return criterios.or(alternativas.toArray(Predicate[]::new));
        };
    }
}
