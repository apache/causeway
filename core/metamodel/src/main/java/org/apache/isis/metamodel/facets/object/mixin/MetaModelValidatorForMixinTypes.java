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
package org.apache.isis.metamodel.facets.object.mixin;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.internal.reflection._Reflect;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorForValidationFailures;

import lombok.NonNull;

public class MetaModelValidatorForMixinTypes extends MetaModelValidatorForValidationFailures {

    private final String annotation;

    public MetaModelValidatorForMixinTypes(final String annotation) {
        this.annotation = annotation;
    }

    public boolean ensureMixinType(
            @NonNull FacetHolder facetHolder,
            final Class<?> candidateMixinType) {

        if (_Reflect.hasPublic1ArgConstructor(candidateMixinType)) {
            return true;
        }
        
        onFailure(
                facetHolder,
                Identifier.classIdentifier(candidateMixinType),
                "%s: annotated with %s annotation but does not have a public 1-arg constructor",
                candidateMixinType.getName(), 
                annotation);
        
        return false;
    }

    
}
