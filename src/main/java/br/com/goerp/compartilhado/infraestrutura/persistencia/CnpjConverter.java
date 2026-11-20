package br.com.goerp.compartilhado.infraestrutura.persistencia;

import br.com.goerp.compartilhado.dominio.Cnpj;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CnpjConverter implements AttributeConverter<Cnpj, String> {

    @Override
    public String convertToDatabaseColumn(Cnpj cnpj) {
        return cnpj == null ? null : cnpj.valor();
    }

    @Override
    public Cnpj convertToEntityAttribute(String valor) {
        return valor == null ? null : Cnpj.de(valor);
    }
}
