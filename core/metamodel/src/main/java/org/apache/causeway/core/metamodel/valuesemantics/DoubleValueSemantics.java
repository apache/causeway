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

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.primitives._Doubles;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("causeway.val.DoubleValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class DoubleValueSemantics
extends ValueSemanticsAbstract<Double>
implements
    DefaultsProvider<Double>,
    Parser<Double>,
    Renderer<Double> {

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

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final Double value) {
        return renderTitle(value, getNumberFormat(context)::format);
    }

    @Override
    public String htmlPresentation(final Context context, final Double value) {
        return renderHtml(value, getNumberFormat(context)::format);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final Double value) {
        return value!=null
                ? getNumberFormat(context)
                    .format(value)
                : null;
    }

    @Override
    public Double parseTextRepresentation(final Context context, final String text) {
        return _Doubles.convertToDouble(super.parseDecimal(context, text))
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
        return Can.of(Double.MIN_VALUE, Double.MAX_VALUE);
    }


}
