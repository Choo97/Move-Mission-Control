package com.moving.reservation.privacy;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Component
@Converter
public class SensitiveStringConverter implements AttributeConverter<String, String> {

    private final PrivacyCryptoService cryptoService;

    public SensitiveStringConverter(PrivacyCryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return cryptoService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return cryptoService.decrypt(dbData);
    }
}
