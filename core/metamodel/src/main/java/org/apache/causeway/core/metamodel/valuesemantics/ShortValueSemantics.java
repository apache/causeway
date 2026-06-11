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

import jakarta.inject.Named;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.NumericValueSemantics;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("causeway.metamodel.value.ShortValueSemantics")
@Primary
//has no effect @Priority(PriorityPrecedence.LATE)
public class ShortValueSemantics
extends NumericValueSemantics<Short>
implements
    DefaultsProvider<Short>,
    Parser<Short>,
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

    // -- PARSER

    @Override
    public Short parseTextRepresentation(final Context context, final String text) {
        var input = _Strings.blankToNullOrTrim(text);
        if(input==null)
            return null;
        try {
            return parseInteger(context, input)
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
                (short)0,
                (short)1,
                (short)2026,
                Short.MIN_VALUE,
                Short.MAX_VALUE);
    }

    // -- GROUPING VARIANTS

    @Component
    @Qualifier(NumericValueSemantics.NO_GROUPING)
    public static class NoGrouping extends ShortValueSemantics {
        @Override protected GroupingSeparatorProvider grouping() {
            return GroupingSeparatorProvider.NO_GROUPING;
        }
    }

    @Component
    @Qualifier(NumericValueSemantics.LOCALE_GROUPING_DISPLAY)
    public static class LocaleGroupingDisplay extends ShortValueSemantics {
        @Override protected GroupingSeparatorProvider grouping() {
            return GroupingSeparatorProvider.LOCALE_GROUPING_DISPLAY;
        }
    }

    @Component
    @Qualifier(NumericValueSemantics.LOCALE_GROUPING_ALL)
    public static class LocaleGroupingAll extends ShortValueSemantics {
        @Override protected GroupingSeparatorProvider grouping() {
            return GroupingSeparatorProvider.LOCALE_GROUPING_ALL;
        }
    }

}
