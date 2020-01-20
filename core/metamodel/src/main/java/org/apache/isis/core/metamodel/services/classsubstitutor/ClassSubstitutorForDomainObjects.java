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

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistryHolder;

import lombok.NonNull;

@Component
@Named("isisMetaModel.ClassSubstitutorForDomainObjects")
@Order(OrderPrecedence.MIDPOINT - 20) // before ClassSubstitutorForCollections
public class ClassSubstitutorForDomainObjects implements ClassSubstitutor {

    private IsisBeanTypeRegistry isisBeanTypeRegistry;

    @Inject
    public ClassSubstitutorForDomainObjects(IsisBeanTypeRegistryHolder beanTypeRegistryHolder) {
        this.isisBeanTypeRegistry = beanTypeRegistryHolder.getIsisBeanTypeRegistry();
    }
    
    @Override
    public Substitution getSubstitution(@NonNull Class<?> cls) {
        
        if(isisBeanTypeRegistry.getViewModelTypes().contains(cls)) {
            return Substitution.neverReplaceClass();
        }
        if(isisBeanTypeRegistry.getEntityTypes().contains(cls)) {
            return Substitution.neverReplaceClass();
        }
        return Substitution.passThrough(); // indifferent
    }
    
}
