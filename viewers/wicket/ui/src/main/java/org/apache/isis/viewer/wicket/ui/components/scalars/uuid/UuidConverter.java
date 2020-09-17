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
package org.apache.isis.viewer.wicket.ui.components.scalars.uuid;

import java.util.Locale;
import java.util.UUID;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import org.apache.isis.commons.internal.base._Strings;

/**
 * The {@link IConverter} implementation that our {@link UuidTextField} delegates to for converting strings into values.
 */
public class UuidConverter implements IConverter<UUID>
{
    private static final long serialVersionUID = 1L;

    //    private static final Pattern pattern = Pattern.compile(
    //            "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}");

    @Override
    public UUID convertToObject(String value, Locale locale)
            throws ConversionException {

        if(_Strings.isNullOrEmpty(value)) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw newConversionException(value);
        }
    }

    @Override
    public String convertToString(UUID value, Locale locale) {
        return value != null ? value.toString() : null;
    }

    private ConversionException newConversionException(String value) {
        return new ConversionException(
                String.format("Failed to convert '%s' to a UUID", value));
    }
}