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
package org.apache.isis.core.metamodel.facets.properties.projection;

import java.util.Optional;

import org.apache.isis.applib.annotations.Projecting;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

import lombok.val;

public class ProjectingFacetFromPropertyAnnotation
extends ProjectingFacetAbstract {

    public static Optional<ProjectingFacet> create(
            final Optional<Property> propertyIfAny,
            final FacetedMethod facetHolder) {

        if(!propertyIfAny.isPresent()) {
            return Optional.empty();
        }

        val projecting = propertyIfAny.get().projecting();
        switch (projecting) {
        case PROJECTED:
            return Optional.of(new ProjectingFacetFromPropertyAnnotation(projecting, facetHolder));
        case NOT_SPECIFIED:
        default:
            return Optional.empty();
        }

    }


    private final Projecting projecting;

    private ProjectingFacetFromPropertyAnnotation(
            final Projecting projecting,
            final FacetHolder holder) {
        super( holder);
        this.projecting = projecting;
    }

    @Override
    public Projecting value() {
        return projecting;
    }

}
