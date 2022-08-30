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

import java.math.BigInteger;
import java.util.function.UnaryOperator;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.applib.value.semantics.DefaultsProvider;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueDecomposition;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;

import lombok.NonNull;
import lombok.val;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("isis.val.ByteValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class ByteValueSemantics
extends ValueSemanticsAbstract<Byte>
implements
    DefaultsProvider<Byte>,
    Parser<Byte>,
    Renderer<Byte>,
    IdStringifier.EntityAgnostic<Byte> {

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

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Byte value) {
        return decomposeAsNullable(value, UnaryOperator.identity(), ()->null);
    }

    @Override
    public Byte compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueWithTypeDto::getByte, UnaryOperator.identity(), ()->null);
    }

    // -- ID STRINGIFIER

    @Override
    public String enstring(final @NonNull Byte value) {
        return value.toString();
    }

    @Override
    public Byte destring(final @NonNull String stringified) {
        return Byte.parseByte(stringified);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final Byte value) {
        return renderTitle(value, getNumberFormat(context)::format);
    }

    @Override
    public String htmlPresentation(final Context context, final Byte value) {
        return renderHtml(value, getNumberFormat(context)::format);
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
            return super.parseInteger(context, input)
                    .map(BigInteger::byteValueExact)
                    .orElse(null);
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
