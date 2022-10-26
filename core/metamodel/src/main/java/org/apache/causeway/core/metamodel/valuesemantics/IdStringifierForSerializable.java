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

import java.io.Serializable;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.services.urlencoding.UrlEncodingService;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.resources._Serializables;

import lombok.NonNull;

/**
 * Used as a fallback if no other {@link ValueSemanticsProvider}
 * is available to handle the corresponding value type.
 */
@Component
@Named("causeway.val.IdStringifierForSerializable")
@Priority(PriorityPrecedence.LAST)
public class IdStringifierForSerializable
implements
    IdStringifier.EntityAgnostic<Serializable>{

    private final UrlEncodingService codec;

    @Inject
    public IdStringifierForSerializable(
            final @NonNull UrlEncodingService codec) {
        this.codec = codec;
    }

//    @Override
//    public ValueType getSchemaValueType() {
//        return ValueType.STRING;
//    }

    @Override
    public Class<Serializable> getCorrespondingClass() {
        return Serializable.class;
    }

//    // -- COMPOSER
//
//    @Override
//    public ValueDecomposition decompose(final Serializable value) {
//        return decomposeAsString(value, this::enstring, ()->null);
//    }
//
//    @Override
//    public Serializable compose(final ValueDecomposition decomposition) {
//        return composeFromString(decomposition, this::destring, ()->null);
//    }

    // -- ID STRINGIFIER

    @Override
    public String enstring(final @NonNull Serializable id) {
        // even though null case is guarded by lombok - keep null check for symmetry
        return id != null
                ? codec.encode(_Serializables.write(id))
                : null;
    }

    @Override
    public Serializable destring(
            @NonNull final String stringified) {
        return destringAs(stringified, Serializable.class);
    }

//    @Override
//    public Can<Serializable> getExamples() {
//        return Can.of(
//                Integer.MAX_VALUE,
//                "Hallo World",
//                new BigDecimal("3.1415"));
//    }

    // -- HELPER

    private <T extends Serializable> T destringAs(
            final @NonNull String stringified,
            final @NonNull Class<T> requiredClass) {
        return _Strings.isNotEmpty(stringified)
                ? _Serializables.read(requiredClass, codec.decode(stringified))
                : null;
    }




}
