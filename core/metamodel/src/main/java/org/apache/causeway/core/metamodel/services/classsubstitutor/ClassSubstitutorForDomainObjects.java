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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.metamodel.BeanSort.BeanPolicy;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

import org.jspecify.annotations.NonNull;

@Component
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".ClassSubstitutorForDomainObjects")
@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT - 20) // before ClassSubstitutorForCollections
public class ClassSubstitutorForDomainObjects implements ClassSubstitutor {

    private CausewayBeanTypeRegistry causewayBeanTypeRegistry;

    @Inject
    public ClassSubstitutorForDomainObjects(final CausewayBeanTypeRegistry causewayBeanTypeRegistry) {
        this.causewayBeanTypeRegistry = causewayBeanTypeRegistry;
    }

    @Override
    public Substitution getSubstitution(final @NonNull Class<?> cls) {
        var notSubstitutable = causewayBeanTypeRegistry.lookupScannedType(cls)
            .map(CausewayBeanMetaData::beanSort)
            .map(BeanSort::policy)
            .map(BeanPolicy::isNotSubstitutable)
            .orElse(false);
        return notSubstitutable
            ? Substitution.neverReplaceClass()
            : Substitution.passThrough(); // indifferent
    }

}
