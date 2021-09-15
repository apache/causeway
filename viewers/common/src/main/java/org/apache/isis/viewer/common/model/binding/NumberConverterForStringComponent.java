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

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.val;

public final class NumberConverterForStringComponent
implements BindingConverter<String> {

    @Getter(onMethod_ = {@Override})
    private final ObjectSpecification valueSpecification;
    private final ParseableFacet parsableFacet;

    @SuppressWarnings("unchecked")
    public NumberConverterForStringComponent(final ObjectSpecification valueSpecification) {
        this.valueSpecification = valueSpecification;

        this.parsableFacet = lookupFacet(ParseableFacet.class)
                .orElseThrow(()->_Exceptions.noSuchElement("missing 'ParseableFacet'"));
    }

    @Override
    public ManagedObject wrap(final String stringifiedNumber) {

        if(tryParse(stringifiedNumber).isPresent()) {
            // return an intermediate placeholder
            return ManagedObject.empty(getValueSpecification());
        }

        val number = //parsableFacet.parseTextRepresentation(null, stringifiedNumber);
                0;
        return ManagedObject.of(getValueSpecification(), number);
    }

    @Override
    public String unwrap(final ManagedObject object) {
        //val number = (Number) ManagedObjects.UnwrapUtil.single(object);
        return "0";//parsableFacet.parseableTextRepresentation(null, number);
    }

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
        return Optional.empty();
//                parsableFacet.tryParseTextEntry(null, stringifiedValue)
//                .map(Exception::getMessage); // TODO should be passed through the ExceptionRecognizer
    }

}