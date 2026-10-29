package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class    BooleanNumeroConverter implements AttributeConverter<Boolean, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Boolean valor) {
        return valor == null ? null : (valor ? 1 : 0);
    }
    @Override
    public Boolean convertToEntityAttribute(Integer valor) {
        return valor == null ? null : valor == 1;
    }
}

