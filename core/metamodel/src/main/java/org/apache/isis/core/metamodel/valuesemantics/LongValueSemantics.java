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

import org.apache.isis.applib.adapters.ValueSemanticsAbstract;
import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("isis.val.LongValueSemantics")
public class LongValueSemantics
extends ValueSemanticsAbstract<Long>
implements
    DefaultsProvider<Long>,
    EncoderDecoder<Long>,
    Parser<Long>,
    Renderer<Long> {

    @Override
    public Class<Long> getCorrespondingClass() {
        return Long.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.LONG;
    }

    @Override
    public Long getDefaultValue() {
        return 0L;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final Long object) {
        return object.toString();
    }

    @Override
    public Long fromEncodedString(final String data) {
        return Long.parseLong(data);
    }

    // -- RENDERER

    @Override
    public String simpleTextRepresentation(final Context context, final Long value) {
        return render(value, getNumberFormat(context)::format);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final Long value) {
        return value==null
                ? null
                : getNumberFormat(context)
                    .format(value);
    }

    @Override
    public Long parseTextRepresentation(final Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        try {
            return super.parseInteger(context, input).longValueExact();
        } catch (final NumberFormatException | ArithmeticException e) {
            throw new TextEntryParseException("Not a 64-bit signed integer " + input, e);
        }
    }

    @Override
    public int typicalLength() {
        // -9223372036854775808
        return 20;
    }

    @Override
    public int maxLength() {
        // -9,223,372,036,854,775,808.0
        return 28;
    }

}
