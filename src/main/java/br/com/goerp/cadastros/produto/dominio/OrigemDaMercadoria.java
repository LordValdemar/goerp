package br.com.goerp.cadastros.produto.dominio;

/** Origem da mercadoria para o ICMS (tabela da NF-e, códigos 0 a 8). */
public enum OrigemDaMercadoria {
    NACIONAL(0, "Nacional"),
    ESTRANGEIRA_IMPORTACAO_DIRETA(1, "Estrangeira, importação direta"),
    ESTRANGEIRA_MERCADO_INTERNO(2, "Estrangeira, adquirida no mercado interno"),
    NACIONAL_IMPORTADO_ACIMA_40(3, "Nacional, conteúdo de importação acima de 40% e até 70%"),
    NACIONAL_PROCESSOS_BASICOS(4, "Nacional, produzida conforme processos produtivos básicos"),
    NACIONAL_IMPORTADO_ATE_40(5, "Nacional, conteúdo de importação até 40%"),
    ESTRANGEIRA_IMPORTACAO_DIRETA_SEM_SIMILAR(6, "Estrangeira, importação direta, sem similar nacional (lista CAMEX)"),
    ESTRANGEIRA_MERCADO_INTERNO_SEM_SIMILAR(7, "Estrangeira, mercado interno, sem similar nacional (lista CAMEX)"),
    NACIONAL_IMPORTADO_ACIMA_70(8, "Nacional, conteúdo de importação acima de 70%");

    private final int codigo;
    private final String descricao;

    OrigemDaMercadoria(int codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public int codigo() {
        return codigo;
    }

    public String descricao() {
        return descricao;
    }
}
