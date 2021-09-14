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
import java.util.Optional;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;
import org.apache.isis.core.metamodel.facets.value.temporal.TemporalValueFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.val;

public class TemporalConverterForLocalDateComponent implements BindingConverter<LocalDate> {

    @Getter(onMethod_ = {@Override})
    private final ObjectSpecification valueSpecification;

    @Getter
    private final ValueSemanticsProviderAndFacetAbstract<LocalDate> valueFacet;

    public TemporalConverterForLocalDateComponent(final ObjectSpecification valueSpecification) {
        this.valueSpecification = valueSpecification;
        this.valueFacet = _Casts.uncheckedCast(valueSpecification.getFacet(TemporalValueFacet.class));
    }

    @Override
    public ManagedObject wrap(final LocalDate localDate) {
        return ManagedObject.of(getValueSpecification(), localDate);
    }

    @Override
    public LocalDate unwrap(final ManagedObject object) {
        val localDate = (LocalDate) ManagedObjects.UnwrapUtil.single(object);
        return localDate;
    }

    @Override
    public String toString(final LocalDate value) {
        return valueFacet.parseableTextRepresentation(null, value);
    }

    @Override
    public LocalDate fromString(final String stringifiedValue) {
        val value = valueFacet.parseTextRepresentation(null, stringifiedValue);
        if(value==null) {
            return null;
        }
        if(value instanceof LocalDate) {
            return value;
        }
        // TODO might require additional cases
        throw _Exceptions.unmatchedCase(value.getClass());
    }

    @Override
    public Optional<String> tryParse(final String stringifiedValue) {
        return valueFacet.tryParseTextEntry(null, stringifiedValue)
                .map(Exception::getMessage); // TODO should be passed through the ExceptionRecognizer
    }


}