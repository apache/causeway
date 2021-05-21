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
package org.apache.isis.core.metamodel.facets.object.mixin;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import static org.apache.isis.commons.internal.reflection._Reflect.Filter.paramCount;

import lombok.NonNull;
import lombok.val;

public class MetaModelValidatorForMixinTypes {

    private final String annotation;

    public MetaModelValidatorForMixinTypes(final String annotation) {
        this.annotation = annotation;
    }

    public boolean ensureMixinType(
            @NonNull FacetHolder facetHolder,
            final Class<?> candidateMixinType) {

        val mixinContructors = _Reflect
                .getPublicConstructors(candidateMixinType)
                .filter(paramCount(1));

        if(mixinContructors.getCardinality().isOne()) {
            return true; // happy case
        }

        if(mixinContructors.getCardinality().isZero()) {
            ValidationFailure.raise(
                    facetHolder.getSpecificationLoader(),
                    Identifier.classIdentifier(LogicalType.fqcn(candidateMixinType)),
                    String.format(
                        "%s: annotated with %s annotation but does not have a public 1-arg constructor",
                        candidateMixinType.getName(),
                        annotation)
                    );
        } else {
            ValidationFailure.raise(
                    facetHolder.getSpecificationLoader(),
                    Identifier.classIdentifier(LogicalType.fqcn(candidateMixinType)),
                    String.format(
                            "%s: annotated with %s annotation needs a single public 1-arg constructor but has %d",
                            candidateMixinType.getName(),
                            annotation,
                            mixinContructors.size())
                    );
        }
        return false;
    }


}
