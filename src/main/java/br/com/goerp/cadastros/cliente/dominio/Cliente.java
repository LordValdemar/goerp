package br.com.goerp.cadastros.cliente.dominio;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.TipoDePessoa;

/**
 * Cliente de uma empresa. O documento (CPF ou CNPJ) o identifica e não muda: se foi digitado
 * errado, inative e cadastre de novo (ele aparece em notas fiscais já emitidas).
 */
public class Cliente {

    private final Long id;
    private final Long empresaId;
    private final Documento documento;
    private final Long versao;
    private DadosDoCliente dados;
    private boolean ativo;

    private Cliente(Long id, Long empresaId, Documento documento, DadosDoCliente dados, boolean ativo, Long versao) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.documento = Objects.requireNonNull(documento, "documento");
        this.dados = Objects.requireNonNull(dados, "dados");
        this.ativo = ativo;
        this.versao = versao;
    }

    public static Cliente novo(Long empresaId, Documento documento, DadosDoCliente dados) {
        return new Cliente(null, empresaId, documento, dados, true, null);
    }

    public static Cliente reconstituir(Long id, Long empresaId, Documento documento, DadosDoCliente dados,
            boolean ativo, Long versao) {
        return new Cliente(id, empresaId, documento, dados, ativo, versao);
    }

    public void alterar(DadosDoCliente novosDados) {
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

    public DadosDoCliente dados() {
        return dados;
    }

    public boolean ativo() {
        return ativo;
    }

    /** Versão lida do banco; nula enquanto não foi gravado. Usada para detectar edição simultânea. */
    public Long versao() {
        return versao;
    }
}
