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
package org.apache.causeway.core.metamodel.valuesemantics;

import java.math.BigInteger;
import java.util.function.UnaryOperator;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.NonNull;
import lombok.val;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("causeway.val.ShortValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class ShortValueSemantics
extends ValueSemanticsAbstract<Short>
implements
    DefaultsProvider<Short>,
    Parser<Short>,
    Renderer<Short>,
    IdStringifier.EntityAgnostic<Short> {

    @Override
    public Class<Short> getCorrespondingClass() {
        return Short.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.SHORT;
    }

    @Override
    public Short getDefaultValue() {
        return Short.valueOf((short) 0);
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Short value) {
        return decomposeAsNullable(value, UnaryOperator.identity(), ()->null);
    }

    @Override
    public Short compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueWithTypeDto::getShort, UnaryOperator.identity(), ()->null);
    }

    // -- ID STRINGIFIER

    @Override
    public String enstring(final @NonNull Short value) {
        return value.toString();
    }

    @Override
    public Short destring(final @NonNull String stringified) {
        return Short.parseShort(stringified);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final Short value) {
        return renderTitle(value, getNumberFormat(context)::format);
    }

    @Override
    public String htmlPresentation(final Context context, final Short value) {
        return renderHtml(value, getNumberFormat(context)::format);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final Short value) {
        return value==null
                ? null
                : getNumberFormat(context)
                    .format(value);
    }

    @Override
    public Short parseTextRepresentation(final Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        try {
            return super.parseInteger(context, input)
                    .map(BigInteger::shortValueExact)
                    .orElse(null);
        } catch (final NumberFormatException | ArithmeticException e) {
            throw new TextEntryParseException("Not a 16-bit signed integer " + input, e);
        }
    }

    @Override
    public int typicalLength() {
        //-32768
        return 6;
    }

    @Override
    public int maxLength() {
        //-32,768.0
        return 9;
    }

    @Override
    public Can<Short> getExamples() {
        return Can.of(
                Short.MIN_VALUE,
                Short.MAX_VALUE);
    }

}
