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
import java.util.function.UnaryOperator;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.NonNull;
import lombok.Setter;
import lombok.val;

@Component
@Named("causeway.val.BigDecimalValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class BigDecimalValueSemantics
extends ValueSemanticsAbstract<BigDecimal>
implements
    DefaultsProvider<BigDecimal>,
    Parser<BigDecimal>,
    Renderer<BigDecimal>,
    IdStringifier.EntityAgnostic<BigDecimal> {

    @Setter @Inject
    private SpecificationLoader specificationLoader;

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

    // -- RENDERER

    @Override
    public String titlePresentation(final ValueSemanticsProvider.Context context, final BigDecimal value) {
        return renderTitle(value, getNumberFormat(context)::format);
    }

    @Override
    public String htmlPresentation(final ValueSemanticsProvider.Context context, final BigDecimal value) {
        return renderHtml(value, getNumberFormat(context)::format);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final BigDecimal value) {
        return value==null
                ? null
                : getNumberFormat(context)
                    .format(value);
    }

    @Override
    public BigDecimal parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        return super.parseDecimal(context, text)
                .orElse(null);
    }

    @Override
    public int typicalLength() {
        return 10;
    }

    @Override
    protected void configureDecimalFormat(
            final Context context, final DecimalFormat format, final FormatUsageFor usedFor) {
        if(context==null) {
            return;
        }
        context.getFeatureIdentifier();
        val feature = specificationLoader.loadFeature(context.getFeatureIdentifier())
                .orElse(null);
        if(feature==null) {
            return;
        }

        // evaluate any facets that provide the MaximumFractionDigits
        Facets.maxFractionalDigits(feature)
            .ifPresent(format::setMaximumFractionDigits);

        // we skip this when PARSING,
        // because we want to firstly parse any number value into a BigDecimal,
        // no matter the minimumFractionDigits, which can always be filled up with '0' digits later
        if(usedFor.isRendering()) {
            // evaluate any facets that provide the MinimumFractionDigits
            Facets.minFractionalDigits(feature)
                .ifPresent(format::setMinimumFractionDigits);
        }
    }

    @Override
    public Can<BigDecimal> getExamples() {
        return Can.of(
                new BigDecimal("1001"),
                new BigDecimal("-63.1"),
                new BigDecimal("0.001"),
                BigDecimal.ZERO);
    }

}
