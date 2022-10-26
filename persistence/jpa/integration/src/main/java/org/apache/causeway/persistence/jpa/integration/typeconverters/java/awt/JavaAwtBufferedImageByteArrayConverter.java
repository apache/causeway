/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.persistence.jpa.integration.typeconverters.java.awt;

import java.awt.image.BufferedImage;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.image._Images;

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

