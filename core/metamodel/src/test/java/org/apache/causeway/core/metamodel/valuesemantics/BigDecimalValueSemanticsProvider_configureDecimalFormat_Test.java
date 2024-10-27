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

import java.text.DecimalFormat;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacetAbstract;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MinFractionalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MinFractionalDigitsFacetAbstract;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

@ExtendWith(MockitoExtension.class)
class BigDecimalValueSemanticsProvider_configureDecimalFormat_Test {

    @Mock Identifier mockIdentifier;
    @Mock ObjectFeature mockObjectFeature;
    @Mock SpecificationLoader mockSpecificationLoader;

    ValueSemanticsProvider.Context context;
    CausewayConfiguration causewayConfiguration;

    BigDecimalValueSemantics valueSemantics;

    @BeforeEach
    void setUpObjects() throws Exception {

        context = ValueSemanticsProvider.Context.of(mockIdentifier, null);

        causewayConfiguration = CausewayConfiguration.builder().build();
        causewayConfiguration.getValueTypes().getBigDecimal().setMinScale(null);

        // expecting
        Mockito.lenient().when(mockSpecificationLoader.loadFeature(mockIdentifier)).thenReturn(Optional.of(mockObjectFeature));

        valueSemantics = new BigDecimalValueSemantics();
        valueSemantics.setSpecificationLoader(mockSpecificationLoader);
        valueSemantics.setCausewayConfiguration(causewayConfiguration);
    }

    @Test
    void max_and_min_facets_set() {

        int maxScale = 10;
        int minScale = 2;

        // expecting
        Mockito.lenient().when(mockObjectFeature.lookupFacet(MaxFractionalDigitsFacet.class))
                .thenReturn(Optional.of(new MaxFractionalDigitsFacetAbstract(maxScale, mockObjectFeature) {}));
        Mockito.lenient().when(mockObjectFeature.lookupFacet(MinFractionalDigitsFacet.class))
               .thenReturn(Optional.of(new MinFractionalDigitsFacetAbstract(minScale, mockObjectFeature) {}));

        // when
        DecimalFormat format = new DecimalFormat();
        valueSemantics.configureDecimalFormat(context, format, ValueSemanticsAbstract.FormatUsageFor.RENDERING);

        // then
        Assertions.assertThat(format.getMaximumFractionDigits()).isEqualTo(maxScale);
        Assertions.assertThat(format.getMinimumFractionDigits()).isEqualTo(minScale);
    }

    @Test
    void min_facets_not_set_but_fallback() {

        int maxScale = 10;
        int fallbackScale = 3;

        // expecting
        Mockito.lenient().when(mockObjectFeature.lookupFacet(MaxFractionalDigitsFacet.class))
                .thenReturn(Optional.of(new MaxFractionalDigitsFacetAbstract(maxScale, mockObjectFeature) {}));
        Mockito.lenient().when(mockObjectFeature.lookupFacet(MinFractionalDigitsFacet.class))
               .thenReturn(Optional.empty());

        causewayConfiguration.getValueTypes().getBigDecimal().setMinScale(fallbackScale);

        // when
        DecimalFormat format = new DecimalFormat();
        valueSemantics.configureDecimalFormat(context, format, ValueSemanticsAbstract.FormatUsageFor.RENDERING);

        // then
        Assertions.assertThat(format.getMaximumFractionDigits()).isEqualTo(maxScale);
        Assertions.assertThat(format.getMinimumFractionDigits()).isEqualTo(fallbackScale);
    }

    @Test
    void min_facets_not_set_and_no_fallback() {

        int maxScale = 10;
        int defaultScale = 0;

        // expecting
        Mockito.lenient().when(mockObjectFeature.lookupFacet(MaxFractionalDigitsFacet.class))
                .thenReturn(Optional.of(new MaxFractionalDigitsFacetAbstract(maxScale, mockObjectFeature) {}));
        Mockito.lenient().when(mockObjectFeature.lookupFacet(MinFractionalDigitsFacet.class))
               .thenReturn(Optional.empty());

        causewayConfiguration.getValueTypes().getBigDecimal().setMinScale(null);

        // when
        DecimalFormat format = new DecimalFormat();
        valueSemantics.configureDecimalFormat(context, format, ValueSemanticsAbstract.FormatUsageFor.RENDERING);

        // then
        Assertions.assertThat(format.getMaximumFractionDigits()).isEqualTo(maxScale);
        Assertions.assertThat(format.getMinimumFractionDigits()).isEqualTo(defaultScale);
    }

}
