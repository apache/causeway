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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutor.Substitution;

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

    private final Map<Class<?>, Substitution> cache = _Maps.newConcurrentHashMap();

    public Substitution getSubstitution(@Nullable final Class<?> originalClass) {
        if(originalClass == null) {
            return Substitution.dontReplaceClass();
        }
        return cache.computeIfAbsent(originalClass, this::findSubstitutionFor);
    }
    
    // -- HELPER 
    
    private Substitution findSubstitutionFor(final Class<?> originalClass) {
        
        return classSubstitutors.stream()
        .map(classSubstitutor->classSubstitutor.getSubstitution(originalClass))
        .filter(_NullSafe::isPresent)
        .findFirst()
        .orElse(Substitution.dontReplaceClass());
         
    }
    
}
