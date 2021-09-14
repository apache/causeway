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
package org.apache.isis.viewer.common.model.binding;

import java.util.Optional;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.facets.value.biginteger.BigIntegerValueFacet;
import org.apache.isis.core.metamodel.facets.value.bytes.ByteValueFacet;
import org.apache.isis.core.metamodel.facets.value.doubles.DoubleFloatingPointValueFacet;
import org.apache.isis.core.metamodel.facets.value.floats.FloatingPointValueFacet;
import org.apache.isis.core.metamodel.facets.value.integer.IntegerValueFacet;
import org.apache.isis.core.metamodel.facets.value.longs.LongValueFacet;
import org.apache.isis.core.metamodel.facets.value.shortint.ShortValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.val;

public final class NumberConverterForStringComponent implements BindingConverter<String> {

    @Getter(onMethod_ = {@Override})
    private final ObjectSpecification valueSpecification;
    private final ValueSemanticsProviderAndFacetAbstract<? extends Number> valueFacet;

    @SuppressWarnings("unchecked")
    public NumberConverterForStringComponent(final ObjectSpecification valueSpecification) {
        this.valueSpecification = valueSpecification;

        this.valueFacet = lookupFacetOneOf(getSupportedFacets())
                .map(ValueSemanticsProviderAndFacetAbstract.class::cast)
                .orElseThrow(()->_Exceptions.noSuchElement("missing 'number' value facet"));
    }

    @Override
    public ManagedObject wrap(final String stringifiedNumber) {

        if(tryParse(stringifiedNumber).isPresent()) {
            // return an intermediate placeholder
            return ManagedObject.empty(getValueSpecification());
        }

        val number = valueFacet.parseTextRepresentation(null, stringifiedNumber);
        return ManagedObject.of(getValueSpecification(), number);
    }

    @Override
    public String unwrap(final ManagedObject object) {
        val number = (Number) ManagedObjects.UnwrapUtil.single(object);
        return valueFacet.parseableTextRepresentation(null, number);
    }


    // for performance reasons in order of likelihood (just guessing)
    @Getter
    private final static Can<Class<? extends Facet>> supportedFacets = Can.of(
            IntegerValueFacet.class,
            DoubleFloatingPointValueFacet.class,
            ByteValueFacet.class,
            LongValueFacet.class,
            BigIntegerValueFacet.class,
            BigDecimalValueFacet.class,
            ShortValueFacet.class,
            FloatingPointValueFacet.class);

    @Override
    public String toString(final String value) {
        return value; // identity
    }

    @Override
    public String fromString(final String stringifiedValue) {
        return stringifiedValue; // identity
    }

    @Override
    public Optional<String> tryParse(final String stringifiedValue) {
        return valueFacet.tryParseTextEntry(null, stringifiedValue)
                .map(Exception::getMessage); // TODO should be passed through the ExceptionRecognizer
    }

}