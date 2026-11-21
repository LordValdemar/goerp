package br.com.goerp.compartilhado.infraestrutura.persistencia;

import br.com.goerp.compartilhado.dominio.Email;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email email) {
        return email == null ? null : email.valor();
    }

    @Override
    public Email convertToEntityAttribute(String valor) {
        return valor == null ? null : Email.de(valor);
    }
}
