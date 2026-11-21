package br.com.goerp.cadastros.fornecedor.dominio;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.TipoDePessoa;

/** Fornecedor de uma empresa. O documento (CPF ou CNPJ) o identifica e não muda. */
public class Fornecedor {

    private final Long id;
    private final Long empresaId;
    private final Documento documento;
    private final Long versao;
    private DadosDoFornecedor dados;
    private boolean ativo;

    private Fornecedor(Long id, Long empresaId, Documento documento, DadosDoFornecedor dados, boolean ativo,
            Long versao) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.documento = Objects.requireNonNull(documento, "documento");
        this.dados = Objects.requireNonNull(dados, "dados");
        this.ativo = ativo;
        this.versao = versao;
    }

    public static Fornecedor novo(Long empresaId, Documento documento, DadosDoFornecedor dados) {
        return new Fornecedor(null, empresaId, documento, dados, true, null);
    }

    public static Fornecedor reconstituir(Long id, Long empresaId, Documento documento, DadosDoFornecedor dados,
            boolean ativo, Long versao) {
        return new Fornecedor(id, empresaId, documento, dados, ativo, versao);
    }

    public void alterar(DadosDoFornecedor novosDados) {
        this.dados = Objects.requireNonNull(novosDados, "dados");
    }

    public void inativar() {
        ativo = false;
    }

    public void ativar() {
        ativo = true;
    }

    public TipoDePessoa tipoDePessoa() {
        return documento.tipoDePessoa();
    }

    public Long id() {
        return id;
    }

    public Long empresaId() {
        return empresaId;
    }

    public Documento documento() {
        return documento;
    }

    public DadosDoFornecedor dados() {
        return dados;
    }

    public boolean ativo() {
        return ativo;
    }

    public Long versao() {
        return versao;
    }
}
