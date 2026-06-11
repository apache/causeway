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

import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.NumericValueSemantics;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

@Component
@Named("causeway.metamodel.value.BigIntegerValueSemantics")
@Primary
//has no effect @Priority(PriorityPrecedence.LATE)
public class BigIntegerValueSemantics
extends NumericValueSemantics<BigInteger>
implements
    DefaultsProvider<BigInteger>,
    Parser<BigInteger>,
    IdStringifier.EntityAgnostic<BigInteger> {

    @Override
    public Class<BigInteger> getCorrespondingClass() {
        return BigInteger.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.BIG_INTEGER;
    }

    @Override
    public BigInteger getDefaultValue() {
        return BigInteger.ZERO;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final BigInteger value) {
        return decomposeAsNullable(value, UnaryOperator.identity(), ()->null);
    }

    @Override
    public BigInteger compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueWithTypeDto::getBigInteger, UnaryOperator.identity(), ()->null);
    }

    // -- ID STRINGIFIER

    @Override
    public String enstring(final @NonNull BigInteger value) {
        return value.toString();
    }

    @Override
    public BigInteger destring(final @NonNull String stringified) {
        return new BigInteger(stringified);
    }

    // -- PARSER

    @Override
    public BigInteger parseTextRepresentation(final Context context, final String text) {
        return parseInteger(context, text)
                .orElse(null);
    }

    @Override
    public int typicalLength() {
        return 10;
    }

    @Override
    public Can<BigInteger> getExamples() {
        return Can.of(
                BigInteger.valueOf(42L),
                BigInteger.valueOf(-63L),
                BigInteger.ZERO,
                BigInteger.ONE,
                BigInteger.TEN,
                BigInteger.valueOf(123_456_789_012L)
                );
    }

    // -- GROUPING VARIANTS

    @Component
    @Qualifier(NumericValueSemantics.NO_GROUPING)
    public static class NoGrouping extends BigIntegerValueSemantics {
        @Override protected GroupingSeparatorProvider grouping() {
            return GroupingSeparatorProvider.NO_GROUPING;
        }
    }

    @Component
    @Qualifier(NumericValueSemantics.LOCALE_GROUPING)
    public static class LocaleGrouping extends BigIntegerValueSemantics {
        @Override protected GroupingSeparatorProvider grouping() {
            return GroupingSeparatorProvider.LOCALE_GROUPING;
        }
    }

}
