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

import java.math.BigDecimal;
import java.text.DecimalFormat;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.applib.value.semantics.DefaultsProvider;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacet;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.Setter;
import lombok.val;

@Component
@Named("isis.val.BigDecimalValueSemantics")
public class BigDecimalValueSemantics
extends ValueSemanticsAbstract<BigDecimal>
implements
    DefaultsProvider<BigDecimal>,
    EncoderDecoder<BigDecimal>,
    Parser<BigDecimal>,
    Renderer<BigDecimal> {

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

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final BigDecimal value) {
        try {
            return value.toPlainString();
        } catch (final Exception e) {
            throw new UnrecoverableException(e);
        }
    }

    @Override
    public BigDecimal fromEncodedString(final String data) {
        return new BigDecimal(data);
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final ValueSemanticsProvider.Context context, final BigDecimal value) {
        return render(value, getNumberFormat(context)::format);
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
        return super.parseDecimal(context, text);
    }

    @Override
    public int typicalLength() {
        return 10;
    }

    @Override
    protected void configureDecimalFormat(final Context context, final DecimalFormat format) {
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
        feature.lookupFacet(MaxFractionalDigitsFacet.class).stream()
        .mapToInt(MaxFractionalDigitsFacet::getMaxFractionalDigits)
        .filter(digits->digits>-1)
        .forEach(digits-> // cardinality 0 or 1
            format.setMaximumFractionDigits(digits));
    }

    @Override
    public Can<BigDecimal> getExamples() {
        return Can.of(new BigDecimal("-63.1"), BigDecimal.ZERO);
    }

}
