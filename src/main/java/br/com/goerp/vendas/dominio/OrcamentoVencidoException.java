package br.com.goerp.vendas.dominio;

import java.time.LocalDate;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class OrcamentoVencidoException extends ExcecaoDeDominio {

    public OrcamentoVencidoException(LocalDate validade) {
        super("Orçamento vencido em " + validade + "; altere a validade (os preços serão atualizados) antes de confirmar");
    }
}
