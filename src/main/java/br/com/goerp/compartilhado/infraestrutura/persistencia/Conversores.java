package br.com.goerp.compartilhado.infraestrutura.persistencia;

import java.math.BigDecimal;

import br.com.goerp.compartilhado.dominio.Cep;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.Gtin;
import br.com.goerp.compartilhado.dominio.Ncm;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.Telefone;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Gravação dos objetos de valor como colunas simples. */
public final class Conversores {

    private Conversores() {
    }

    /**
     * Não é automático: {@code Cnpj} também é um {@code Documento} e já tem conversor próprio.
     * Use {@code @Convert(converter = Conversores.DocumentoConverter.class)} nos campos do tipo Documento.
     */
    @Converter
    public static class DocumentoConverter implements AttributeConverter<Documento, String> {
        @Override
        public String convertToDatabaseColumn(Documento documento) {
            return documento == null ? null : documento.valor();
        }

        @Override
        public Documento convertToEntityAttribute(String valor) {
            return valor == null ? null : Documento.de(valor);
        }
    }

    @Converter(autoApply = true)
    public static class CepConverter implements AttributeConverter<Cep, String> {
        @Override
        public String convertToDatabaseColumn(Cep cep) {
            return cep == null ? null : cep.valor();
        }

        @Override
        public Cep convertToEntityAttribute(String valor) {
            return valor == null ? null : Cep.de(valor);
        }
    }

    @Converter(autoApply = true)
    public static class TelefoneConverter implements AttributeConverter<Telefone, String> {
        @Override
        public String convertToDatabaseColumn(Telefone telefone) {
            return telefone == null ? null : telefone.valor();
        }

        @Override
        public Telefone convertToEntityAttribute(String valor) {
            return valor == null ? null : Telefone.de(valor);
        }
    }

    @Converter(autoApply = true)
    public static class DinheiroConverter implements AttributeConverter<Dinheiro, BigDecimal> {
        @Override
        public BigDecimal convertToDatabaseColumn(Dinheiro dinheiro) {
            return dinheiro == null ? null : dinheiro.valor();
        }

        @Override
        public Dinheiro convertToEntityAttribute(BigDecimal valor) {
            return valor == null ? null : Dinheiro.de(valor);
        }
    }

    @Converter(autoApply = true)
    public static class QuantidadeConverter implements AttributeConverter<Quantidade, BigDecimal> {
        @Override
        public BigDecimal convertToDatabaseColumn(Quantidade quantidade) {
            return quantidade == null ? null : quantidade.valor();
        }

        @Override
        public Quantidade convertToEntityAttribute(BigDecimal valor) {
            return valor == null ? null : new Quantidade(valor);
        }
    }

    @Converter(autoApply = true)
    public static class GtinConverter implements AttributeConverter<Gtin, String> {
        @Override
        public String convertToDatabaseColumn(Gtin gtin) {
            return gtin == null ? null : gtin.valor();
        }

        @Override
        public Gtin convertToEntityAttribute(String valor) {
            return valor == null ? null : Gtin.de(valor);
        }
    }

    @Converter(autoApply = true)
    public static class NcmConverter implements AttributeConverter<Ncm, String> {
        @Override
        public String convertToDatabaseColumn(Ncm ncm) {
            return ncm == null ? null : ncm.valor();
        }

        @Override
        public Ncm convertToEntityAttribute(String valor) {
            return valor == null ? null : Ncm.de(valor);
        }
    }
}
