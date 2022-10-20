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
package org.apache.causeway.core.metamodel.facets.object.projection;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Projecting;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.properties.projection.ProjectingFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

public class ProjectionFacetFromProjectingProperty
extends ProjectionFacetAbstract {

    private final OneToOneAssociation projectingProperty;

    public static Optional<ProjectionFacet> create(final ObjectSpecification objectSpecification) {
        return objectSpecification.streamProperties(MixedIn.EXCLUDED)
        .filter(prop ->
            prop.lookupNonFallbackFacet(ProjectingFacet.class)
            .map(projectingFacet -> projectingFacet.value() == Projecting.PROJECTED)
            .orElse(false)
        )
        .findFirst()
        .map(prop -> new ProjectionFacetFromProjectingProperty(prop, objectSpecification));
    }

    private ProjectionFacetFromProjectingProperty(
            final OneToOneAssociation projectingProperty,
            final FacetHolder holder) {

        super(holder);
        this.projectingProperty = projectingProperty;
    }

    @Override
    public ManagedObject projected(final ManagedObject owningAdapter) {
        return projectingProperty.get(owningAdapter);
    }
}
