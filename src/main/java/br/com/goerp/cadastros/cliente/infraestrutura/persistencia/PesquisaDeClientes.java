package br.com.goerp.cadastros.cliente.infraestrutura.persistencia;

import static br.com.goerp.compartilhado.infraestrutura.persistencia.Especificacoes.situacao;
import static br.com.goerp.compartilhado.infraestrutura.persistencia.Especificacoes.texto;

import org.springframework.data.jpa.domain.Specification;

import br.com.goerp.cadastros.cliente.dominio.FiltroDeClientes;

/** O texto procura no nome e no nome fantasia (qualquer parte) e no começo do documento. */
final class PesquisaDeClientes {

    private PesquisaDeClientes() {
    }

    static Specification<ClienteJpa> com(FiltroDeClientes filtro) {
        return Specification.allOf(situacao(filtro.ativo()),
                texto(filtro.texto(), "documento", "nome", "nomeFantasia"));
    }
}
