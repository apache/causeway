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
package org.apache.isis.core.metamodel.facets.value.booleans;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.adapters.AbstractValueSemanticsProvider;
import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.commons.internal.base._Strings;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
public class BooleanValueSemantics
extends AbstractValueSemanticsProvider<Boolean>
implements
    DefaultsProvider<Boolean>,
    EncoderDecoder<Boolean>,
    Parser<Boolean> {

    @Override
    public Boolean getDefaultValue() {
        return Boolean.TRUE;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final Boolean object) {
        return isSet(object) ? "T" : "F";
    }

    @Override
    public Boolean fromEncodedString(final String data) {
        final int dataLength = data.length();
        if (dataLength == 1) {
            switch (data.charAt(0)) {
            case 'T':
                return Boolean.TRUE;
            case 'F':
                return Boolean.FALSE;
            default:
                throw new UnrecoverableException("Invalid data for logical, expected 'T', 'F' or 'N, but got " + data.charAt(0));
            }
        } else if (dataLength == 4 || dataLength == 5) {
            switch (data.charAt(0)) {
            case 't':
                return Boolean.TRUE;
            case 'f':
                return Boolean.FALSE;
            default:
                throw new UnrecoverableException("Invalid data for logical, expected 't' or 'f', but got " + data.charAt(0));
            }
        }
        throw new UnrecoverableException("Invalid data for logical, expected 1, 4 or 5 bytes, got " + dataLength + ": " + data);
    }

    // -- PARSER

    @Override
    public String presentationValue(final Context context, final Boolean value) {
        return value == null ? "" : isSet(value) ? "True" : "False";
    }

    @Override
    public String parseableTextRepresentation(final Context context, final Boolean value) {
        return value != null ? value.toString(): null;
    }

    @Override
    public Boolean parseTextRepresentation(final Context context, final String text) {
        final var input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        if ("true".equalsIgnoreCase(input)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(input)) {
            return Boolean.FALSE;
        } else {
            throw new TextEntryParseException(String.format("'%s' cannot be parsed as a boolean", input));
        }
    }

    @Override
    public int typicalLength() {
        return maxLength();
    }

    @Override
    public int maxLength() {
        return 6;
    }

    // -- HELPER

    private boolean isSet(final Boolean value) {
        return value.booleanValue();
    }

}
