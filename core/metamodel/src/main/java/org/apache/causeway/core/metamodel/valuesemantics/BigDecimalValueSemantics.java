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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.function.UnaryOperator;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.value.semantics.NumericValueSemantics;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.Setter;

@Component
@Named("causeway.metamodel.value.BigDecimalValueSemantics")
@Primary
//has no effect @Priority(PriorityPrecedence.LATE)
public class BigDecimalValueSemantics
extends NumericValueSemanticsAbstract<BigDecimal>
implements IdStringifier.EntityAgnostic<BigDecimal> {

    @Setter @Inject
    private CausewayConfiguration causewayConfiguration;

    @Override
    public Class<BigDecimal> getCorrespondingClass() {
        return BigDecimal.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.BIG_DECIMAL;
    }

    @Override
    public BigDecimal getDefaultValue() {
        return BigDecimal.ZERO;
    }

    @Override
    protected boolean isFloatingPoint() {
        return true;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final BigDecimal value) {
        return decomposeAsNullable(value, UnaryOperator.identity(), ()->null);
    }

    @Override
    public BigDecimal compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueWithTypeDto::getBigDecimal, UnaryOperator.identity(), ()->null);
    }

    // -- ID STRINGIFIER

    @Override
    public String enstring(final @NonNull BigDecimal value) {
        return value.toString();
    }

    @Override
    public BigDecimal destring(final @NonNull String stringified) {
        return new BigDecimal(stringified);
    }

    // -- PARSER

    @Override
    public BigDecimal parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        var input = _Strings.blankToNullOrTrim(text);
        if(input==null)
            return null;
        return parseDecimal(context, text)
            .orElse(null);
    }

    @Override
    public int typicalLength() {
        return 10;
    }

    @Override
    protected void configureDecimalFormat(
            final ValueSemanticsProvider.Context context, final DecimalFormat format, final FormatUsageFor usedFor) {

        super.configureDecimalFormat(context, format, usedFor);

        var bigDecimalConfig = causewayConfiguration.valueTypes().bigDecimal();
        if(!usedFor.isParsing()
                || bigDecimalConfig.editing().preserveScale()) {
            if(format.getMinimumFractionDigits()==0) {
                // apply a minScale if configured.
                Optional.ofNullable(bigDecimalConfig.display().minScale())
                    .ifPresent(format::setMinimumFractionDigits);
            }
        }
    }

    @Override
    public Can<BigDecimal> getExamples() {
        return Can.of(
                new BigDecimal("1001"),
                new BigDecimal("-63.1"),
                new BigDecimal("0.001"),
                BigDecimal.ZERO,
                BigDecimal.ONE,
                BigDecimal.TEN,
                BigDecimal.valueOf(123_456_789_012L),
                BigDecimal.valueOf(1234567.8890f),
                BigDecimal.valueOf(123_456_789_012L, 3));
    }

    // -- GROUPING VARIANTS

    @Component
    @Qualifier(NumericValueSemantics.NO_GROUPING)
    public static class NoGrouping extends BigDecimalValueSemantics {
        @Override public GroupingSeparatorProvider grouping() {
            return GroupingSeparatorProvider.NO_GROUPING;
        }
    }

    @Component
    @Qualifier(NumericValueSemantics.LOCALE_GROUPING_DISPLAY)
    public static class LocaleGroupingDisplay extends BigDecimalValueSemantics {
        @Override public GroupingSeparatorProvider grouping() {
            return GroupingSeparatorProvider.LOCALE_GROUPING_DISPLAY;
        }
    }

    @Component
    @Qualifier(NumericValueSemantics.LOCALE_GROUPING_ALL)
    public static class LocaleGroupingAll extends BigDecimalValueSemantics {
        @Override public GroupingSeparatorProvider grouping() {
            return GroupingSeparatorProvider.LOCALE_GROUPING_ALL;
        }
    }

}
