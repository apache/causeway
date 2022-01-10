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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Vector;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;

import lombok.NonNull;

@Component
@Named("isis.metamodel.ClassSubstitutorForCollections")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT - 10)
public class ClassSubstitutorForCollections implements ClassSubstitutor {

    @Override
    public Substitution getSubstitution(@NonNull Class<?> cls) {
        if(Vector.class.isAssignableFrom(cls)) {
            return Substitution.replaceWith(Vector.class);
        }
        if(List.class.isAssignableFrom(cls)) {
            return Substitution.replaceWith(List.class);
        }
        if(SortedSet.class.isAssignableFrom(cls)) {
            return Substitution.replaceWith(SortedSet.class);
        }
        if(Set.class.isAssignableFrom(cls)) {
            return Substitution.replaceWith(Set.class);
        }
        if(Collection.class.isAssignableFrom(cls)) {
            return Substitution.replaceWith(Collection.class);
        }

        return Substitution.passThrough(); // indifferent
    }
}
