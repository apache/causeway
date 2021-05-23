package org.apache.isis.persistence.jpa.integration.typeconverters;

import java.awt.image.BufferedImage;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.image._Images;

/**
 * @since 2.0 {@index}
 */
@Converter(autoApply = true)
public class JavaAwtBufferedImageByteArrayConverter
implements AttributeConverter<BufferedImage, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(BufferedImage memberValue) {
        if (memberValue == null) {
            return null;
        }
        try {
            return _Images.toBytes(memberValue);
        } catch (Exception e) {
            throw new javax.persistence.PersistenceException(
                    "Error serialising object of type BufferedImage to byte array", e);
        }
    }

    @Override
    public BufferedImage convertToEntityAttribute(byte[] datastoreValue) {
        if (_NullSafe.size(datastoreValue) == 0) {
            return null;
        }
        try {
            return _Images.fromBytes(datastoreValue);
        } catch (Exception e) {
            throw new javax.persistence.PersistenceException(
                    "Error deserialising image datastoreValue", e);
        }
    }

}

