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

import jakarta.annotation.Priority;
import jakarta.inject.Named;

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

import org.jspecify.annotations.NonNull;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("causeway.metamodel.value.LongValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class LongValueSemantics
extends ValueSemanticsAbstract<Long>
implements
    DefaultsProvider<Long>,
    Parser<Long>,
    Renderer<Long>,
    IdStringifier.EntityAgnostic<Long>{

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

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Long value) {
        return decomposeAsNullable(value, UnaryOperator.identity(), ()->null);
    }

    @Override
    public Long compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueWithTypeDto::getLong, UnaryOperator.identity(), ()->null);
    }

    // -- ID STRINGIFIER

    @Override
    public String enstring(final @NonNull Long value) {
        return value.toString();
    }

    @Override
    public Long destring(final @NonNull String stringified) {
        return Long.parseLong(stringified);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final Long value) {
        return renderTitle(value, getNumberFormat(context)::format);
    }

    @Override
    public String htmlPresentation(final Context context, final Long value) {
        return renderHtml(value, getNumberFormat(context)::format);
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
        var input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        try {
            return super.parseInteger(context, input)
                    .map(BigInteger::longValueExact)
                    .orElse(null);
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

    @Override
    public Can<Long> getExamples() {
        return Can.of(Long.MIN_VALUE, Long.MAX_VALUE);
    }

}
