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
package org.apache.isis.core.metamodel.valuesemantics;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.value.semantics.DefaultsProvider;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("isis.val.BooleanValueSemantics")
public class BooleanValueSemantics
extends ValueSemanticsAbstract<Boolean>
implements
    DefaultsProvider<Boolean>,
    EncoderDecoder<Boolean>,
    Parser<Boolean>,
    Renderer<Boolean> {

    @Override
    public Class<Boolean> getCorrespondingClass() {
        return Boolean.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.BOOLEAN;
    }

    @Override
    public Boolean getDefaultValue() {
        return Boolean.FALSE;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final Boolean value) {
        if(value==null) {
            return null;
        }
        return value.booleanValue() ? "T" : "F";
    }

    @Override
    public Boolean fromEncodedString(final String data) {
        if(data==null) {
            return null;
        }
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

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final ValueSemanticsProvider.Context context, final Boolean value) {
        return render(value, v->v.booleanValue() ? "True" : "False");
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final Boolean value) {
        return value != null ? value.toString(): null;
    }

    @Override
    public Boolean parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
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

    @Override
    public Can<Boolean> getExamples() {
        return Can.of(Boolean.TRUE, Boolean.FALSE);
    }

}
