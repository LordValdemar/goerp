package br.com.goerp.cadastros.produto.dominio;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Cest;
import br.com.goerp.compartilhado.dominio.Ncm;

/**
 * Classificação fiscal usada na emissão de notas. As regras de tributação (CFOP, CST, alíquotas)
 * ficam no módulo fiscal, validadas com o contador.
 *
 * @param cest opcional: só alguns NCMs (substituição tributária) exigem
 */
public record DadosFiscais(Ncm ncm, Cest cest, OrigemDaMercadoria origem) {

    public DadosFiscais {
        Objects.requireNonNull(ncm, "ncm");
        origem = Objects.requireNonNullElse(origem, OrigemDaMercadoria.NACIONAL);
    }
}
