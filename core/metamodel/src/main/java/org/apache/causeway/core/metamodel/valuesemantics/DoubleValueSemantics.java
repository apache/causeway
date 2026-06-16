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

import java.util.function.UnaryOperator;

import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.NumericValueSemantics;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.primitives._Doubles;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("causeway.metamodel.value.DoubleValueSemantics")
@Primary
//has no effect @Priority(PriorityPrecedence.LATE)
public class DoubleValueSemantics
extends NumericValueSemanticsAbstract<Double>
implements
    DefaultsProvider<Double>,
    Parser<Double> {

    @Override
    public Class<Double> getCorrespondingClass() {
        return Double.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.DOUBLE;
    }

    @Override
    public Double getDefaultValue() {
        return 0.;
    }

    @Override
    protected boolean isIntegerOnly() {
        return false;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Double value) {
        return decomposeAsNullable(value, UnaryOperator.identity(), ()->null);
    }

    @Override
    public Double compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueWithTypeDto::getDouble, UnaryOperator.identity(), ()->null);
    }

    // -- PARSER

    @Override
    public Double parseTextRepresentation(final Context context, final String text) {
        return _Doubles.convertToDouble(parseDecimal(context, text))
                .orElse(null);
    }

    @Override
    public int typicalLength() {
        //XXX research - legacy value, what motivates this number?
        return 10;
    }

    @Override
    public int maxLength() {
        //XXX research - legacy value, what motivates this number?
        return 25;
    }

    @Override
    public Can<Double> getExamples() {
        return Can.of(1.0d, 0.1d, Math.PI, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    // -- GROUPING VARIANTS

    @Component
    @Qualifier(NumericValueSemantics.NO_GROUPING)
    public static class NoGrouping extends DoubleValueSemantics {
        @Override public GroupingSeparatorProvider grouping() {
            return GroupingSeparatorProvider.NO_GROUPING;
        }
    }

    @Component
    @Qualifier(NumericValueSemantics.LOCALE_GROUPING_DISPLAY)
    public static class LocaleGroupingDisplay extends DoubleValueSemantics {
        @Override public GroupingSeparatorProvider grouping() {
            return GroupingSeparatorProvider.LOCALE_GROUPING_DISPLAY;
        }
    }

    @Component
    @Qualifier(NumericValueSemantics.LOCALE_GROUPING_ALL)
    public static class LocaleGroupingAll extends DoubleValueSemantics {
        @Override public GroupingSeparatorProvider grouping() {
            return GroupingSeparatorProvider.LOCALE_GROUPING_ALL;
        }
    }

}
