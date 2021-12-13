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

import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.value.semantics.DefaultsProvider;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("isis.val.ByteValueSemantics")
public class ByteValueSemantics
extends ValueSemanticsAbstract<Byte>
implements
    DefaultsProvider<Byte>,
    EncoderDecoder<Byte>,
    Parser<Byte>,
    Renderer<Byte> {

    @Override
    public Class<Byte> getCorrespondingClass() {
        return Byte.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.BYTE;
    }

    @Override
    public Byte getDefaultValue() {
        return Byte.valueOf((byte) 0);
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final Byte object) {
        return object.toString();
    }

    @Override
    public Byte fromEncodedString(final String data) {
        return Byte.parseByte(data);
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final Context context, final Byte value) {
        return render(value, getNumberFormat(context)::format);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final Byte value) {
        return value==null
                ? null
                : getNumberFormat(context)
                    .format(value);
    }

    @Override
    public Byte parseTextRepresentation(final Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        try {
            return super.parseInteger(context, input).byteValueExact();
        } catch (final NumberFormatException | ArithmeticException e) {
            throw new TextEntryParseException("Not a 8-bit signed integer " + input, e);
        }
    }

    @Override
    public int typicalLength() {
        //-128
        return 4;
    }

    @Override
    public int maxLength() {
        //-128.0
        return 6;
    }

    @Override
    public Can<Byte> getExamples() {
        return Can.of(
                Byte.MIN_VALUE,
                Byte.MAX_VALUE);
    }

}
