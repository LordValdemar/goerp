package br.com.goerp.financeiro.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;
import static br.com.goerp.compartilhado.dominio.Textos.opcional;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;

/**
 * O que é comum a todas as parcelas de uma conta a pagar ou a receber.
 *
 * @param pessoaId           fornecedor (a pagar, opcional) ou cliente (a receber, obrigatório)
 * @param documento          número da nota, boleto, contrato... (opcional)
 * @param formaPrevista      como se espera pagar ou receber (padrão: a prazo)
 * @param documentoDeOrigem  id do recebimento de compra ou da venda que gerou o título (opcional)
 */
public record DadosDoTitulo(
        NaturezaDoTitulo natureza,
        Long pessoaId,
        String descricao,
        Long categoriaId,
        String documento,
        FormaDePagamento formaPrevista,
        OrigemDoTitulo origem,
        Long documentoDeOrigem) {

    public DadosDoTitulo {
        Objects.requireNonNull(natureza, "natureza");
        descricao = obrigatorio(descricao, "descricao");
        documento = opcional(documento);
        formaPrevista = Objects.requireNonNullElse(formaPrevista, FormaDePagamento.A_PRAZO);
        origem = Objects.requireNonNullElse(origem, OrigemDoTitulo.MANUAL);
        // conta a receber sem cliente não teria de quem cobrar
        if (natureza == NaturezaDoTitulo.A_RECEBER && pessoaId == null) {
            throw new CampoObrigatorioException("clienteId");
        }
    }
}
