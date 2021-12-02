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

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutor.Substitution;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Aggregates all {@link ClassSubstitutor}s.
 */
@Component
@Named("isis.metamodel.ClassSubstitutorRegistry")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Log4j2
public class ClassSubstitutorRegistry {

    private final List<ClassSubstitutor> classSubstitutors;

    @Inject
    public ClassSubstitutorRegistry(final List<ClassSubstitutor> classSubstitutors) {
        this.classSubstitutors = classSubstitutors;
    }

    private final Map<Class<?>, Substitution> cache = _Maps.newConcurrentHashMap();

    /**
     * @param originalClass
     * @return (non-null) the aggregated Substitution that applies to given originalClass
     */
    public Substitution getSubstitution(final @Nullable Class<?> originalClass) {
        if(originalClass == null) {
            return Substitution.neverReplaceClass();
        }
        return cache.computeIfAbsent(originalClass, this::findSubstitutionFor);
    }

    // -- HELPER

    private Substitution findSubstitutionFor(final Class<?> originalClass) {

        return classSubstitutors.stream()
        .map(classSubstitutor->getSubstitutionElseWarn(classSubstitutor, originalClass))
        .filter(_Predicates.not(Substitution::isPassThrough))
        .findFirst()
        .orElse(Substitution.neverReplaceClass());

    }

    private Substitution getSubstitutionElseWarn(final ClassSubstitutor substitutor, final Class<?> originalClass) {

        val substitution = substitutor.getSubstitution(originalClass);
        if(substitution == null) {
            log.warn("ClassSubstitutor.getSubstitution(Class) must never return null! "
                    + "However, substitutor {} just did for class argument {}. "
                    + "Pass-through was applied instead.",
                    substitutor.getClass().getName(),
                    originalClass.getClass().getName());
            return Substitution.passThrough();
        }
        return substitution;
    }

}
