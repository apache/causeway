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

import java.time.LocalDate;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.val;

public final class TemporalConverter implements BindingConverter<LocalDate> {

    @Getter(onMethod_ = {@Override})
    private final ObjectSpecification valueSpecification;
    private final TemporalValueFacet<?> valueFacet; 

    public TemporalConverter(final ObjectSpecification valueSpecification) {
        this.valueSpecification = valueSpecification;

        this.valueFacet = (TemporalValueFacet<?>) lookupFacetOneOf(getSupportedFacets())
                .orElseThrow(()->_Exceptions.noSuchElement("missing 'temporal' value facet"));
    }

    @Override
    public ManagedObject wrap(LocalDate localDate) {
        return ManagedObject.of(getValueSpecification(), localDate);
    }

    @Override
    public LocalDate unwrap(ManagedObject object) {
        val localDate = (LocalDate) ManagedObjects.UnwrapUtil.single(object);
        return localDate;
    }
    
    // for performance reasons in order of likelihood (just guessing)
    @Getter
    private final static Can<Class<? extends Facet>> supportedFacets = Can.of(
            TemporalValueFacet.class);

}