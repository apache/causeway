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

package org.apache.isis.core.metamodel.facets.object.projection;

import org.apache.isis.applib.annotation.Projecting;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.properties.projection.ProjectingFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class ProjectionFacetFromProjectingProperty extends ProjectionFacetAbstract {

    public static Class<? extends Facet> type() {
        return ProjectionFacet.class;
    }

    private final OneToOneAssociation projectingProperty;

    private ProjectionFacetFromProjectingProperty(
            final OneToOneAssociation projectingProperty,
            final FacetHolder holder) {
        super( holder);
        this.projectingProperty = projectingProperty;
    }

    public static ProjectionFacet create(final ObjectSpecification objectSpecification) {
        return objectSpecification.streamProperties(Contributed.EXCLUDED)
                .filter(otoa -> {
                        final ProjectingFacet projectingFacet = otoa
                                .getFacet(ProjectingFacet.class);
                        return projectingFacet != null && !projectingFacet.isNoop()
                                && projectingFacet.value() == Projecting.PROJECTED;
                })
                .map(otoa -> new ProjectionFacetFromProjectingProperty(otoa, objectSpecification))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ObjectAdapter projected(final ManagedObject owningAdapter) {
        return owningAdapter instanceof ObjectAdapter
                ? projectingProperty.get((ObjectAdapter) owningAdapter)
                : null ;
    }
}
