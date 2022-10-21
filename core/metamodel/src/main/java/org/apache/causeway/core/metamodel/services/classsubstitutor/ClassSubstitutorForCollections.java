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
package org.apache.causeway.core.metamodel.services.classsubstitutor;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

import lombok.NonNull;

@Component
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".ClassSubstitutorForCollections")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT - 10)
public class ClassSubstitutorForCollections implements ClassSubstitutor {

    @Override
    public Substitution getSubstitution(@NonNull final Class<?> cls) {

        return ProgrammingModelConstants.CollectionSemantics.valueOf(cls)
            .map(ProgrammingModelConstants.CollectionSemantics::getContainerType)
            .map(Substitution::replaceWith) // replace container type with first replacement type that matches
            .orElse( Substitution.passThrough()) // indifferent
        ;

    }
}
