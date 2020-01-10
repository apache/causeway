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

package org.apache.isis.core.metamodel.services.classsubstitutor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.commons.internal.collections._Maps;

/**
 * Aggregates all {@link ClassSubstitutor}s.
 */
@Component
@Named("isisMetaModel.ClassSubstitutorRegistry")
@Order(OrderPrecedence.MIDPOINT)
public class ClassSubstitutorRegistry {

    private final List<ClassSubstitutor> classSubstitutors;

    @Inject
    public ClassSubstitutorRegistry(final List<ClassSubstitutor> classSubstitutors) {
        this.classSubstitutors = classSubstitutors;
    }

    private final Map<Class<?>, Optional<Class<?>>> cache = _Maps.newHashMap();

    // TODO: it would be preferable to return an Optional here.
    public Class<?> getClass(final Class<?> originalClass) {
        if(originalClass == null) {
            return null;
        }

        Optional<Class<?>> substitutedClass = cache.get(originalClass);


        cacheMiss:
        //noinspection OptionalAssignedToNull
        if(substitutedClass == null) {
            // don't yet know if this class needs to be substituted
            for (ClassSubstitutor classSubstitutor : classSubstitutors) {
                final Class<?> substitutedType = classSubstitutor.getClass(originalClass);
                if(substitutedType != originalClass) {
                    // one of the substitutors has made a substitution
                    // (though note, it might have substituted it with null).
                    substitutedClass = Optional.ofNullable(substitutedType);
                    break cacheMiss;
                }
            }
            // no substitution
            substitutedClass = Optional.of(originalClass);
            cache.put(originalClass, substitutedClass);
        }
        return substitutedClass.orElse(null);
    }
}
