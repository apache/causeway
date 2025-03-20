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
package org.apache.causeway.persistence.jpa.integration.typeconverters.java.net;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Maps an {@link URL} to a simple String column in the corresponding database table.
 * <p>
 * Without this conversion, JPA maps {@link Serializable} types to a byte array.
 * @since 3.3 {@index}
 */
@Converter(autoApply = true)
public class JavaNetURLConverter
implements AttributeConverter<URL, String> {

    @Override
    public String convertToDatabaseColumn(final URL uuid) {
        return uuid != null
                ? uuid.toString()
                : null;
    }

    @Override
    public URL convertToEntityAttribute(final String datastoreValue) {
        try {
            return (datastoreValue != null)
                ? new URL(datastoreValue)
                : null;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL format in database: " + datastoreValue, e);
        }
    }

}
