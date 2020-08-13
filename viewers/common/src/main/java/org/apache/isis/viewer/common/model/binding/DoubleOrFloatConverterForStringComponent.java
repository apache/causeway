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

import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.facets.value.doubles.DoubleFloatingPointValueFacet;
import org.apache.isis.core.metamodel.facets.value.floats.FloatingPointValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.val;

public final class DoubleOrFloatConverterForStringComponent implements BindingConverter<String> {

    @Getter(onMethod_ = {@Override})
    private final ObjectSpecification valueSpecification;
    private final ValueSemanticsProviderAndFacetAbstract<? extends Number> valueFacet; 

    @SuppressWarnings("unchecked") 
    public DoubleOrFloatConverterForStringComponent(final ObjectSpecification valueSpecification) {
        this.valueSpecification = valueSpecification;

        this.valueFacet = lookupFacetOneOf(
                DoubleFloatingPointValueFacet.class,
                FloatingPointValueFacet.class)
                .map(ValueSemanticsProviderAndFacetAbstract.class::cast)
                .orElseThrow(()->_Exceptions.noSuchElement("missing 'double' or 'float' value facet"));
    }

    @Override
    public ManagedObject wrap(String stringifiedNumber) {
        val number = valueFacet.parseTextEntry(null, stringifiedNumber);
        return ManagedObject.of(getValueSpecification(), number);
    }

    @Override
    public String unwrap(ManagedObject object) {
        val number = (Number) ManagedObjects.UnwrapUtil.single(object);
        return valueFacet.parseableTitleOf(number);
    }

}