package br.com.goerp.empresa.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;
import static br.com.goerp.compartilhado.dominio.Textos.opcional;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Cnpj;

/** Empresa cliente do ERP. Cada empresa enxerga apenas os próprios dados. */
public class Empresa {

    private final Long id;
    private final Cnpj cnpj;
    private String razaoSocial;
    private String nomeFantasia;
    private boolean ativa;

    private Empresa(Long id, Cnpj cnpj, String razaoSocial, String nomeFantasia, boolean ativa) {
        this.id = id;
        this.cnpj = Objects.requireNonNull(cnpj, "cnpj");
        this.ativa = ativa;
        alterarDados(razaoSocial, nomeFantasia);
    }

    public static Empresa nova(Cnpj cnpj, String razaoSocial, String nomeFantasia) {
        return new Empresa(null, cnpj, razaoSocial, nomeFantasia, true);
    }

    /** Recria uma empresa já existente, a partir dos dados persistidos. */
    public static Empresa reconstituir(Long id, Cnpj cnpj, String razaoSocial, String nomeFantasia, boolean ativa) {
        return new Empresa(id, cnpj, razaoSocial, nomeFantasia, ativa);
    }

    public void alterarDados(String razaoSocial, String nomeFantasia) {
        this.razaoSocial = obrigatorio(razaoSocial, "razaoSocial");
        this.nomeFantasia = opcional(nomeFantasia);
    }

    public void inativar() {
        ativa = false;
    }

    public void ativar() {
        ativa = true;
    }

    public Long id() {
        return id;
    }

    public Cnpj cnpj() {
        return cnpj;
    }

    public String razaoSocial() {
        return razaoSocial;
    }

    public String nomeFantasia() {
        return nomeFantasia;
    }

    public boolean ativa() {
        return ativa;
    }
}
