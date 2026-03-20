package com.payauth.payauth.common.crypto;
import jakarta.persistence.AttributeConverter; // 여기!
import jakarta.persistence.Converter;        // 여기!
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class StringCryptoConverter implements AttributeConverter<String, String>{
    private final CryptoUtil cryptoUtil;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return (attribute == null || attribute.isBlank()) ? attribute : cryptoUtil.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return (dbData == null || dbData.isBlank()) ? dbData : cryptoUtil.decrypt(dbData);
    }
}
