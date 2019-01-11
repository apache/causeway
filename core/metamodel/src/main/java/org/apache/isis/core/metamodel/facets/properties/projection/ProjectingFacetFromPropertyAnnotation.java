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

import org.apache.isis.applib.annotation.Projecting;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;

public class ProjectingFacetFromPropertyAnnotation extends ProjectingFacetAbstract {

    public static Class<? extends Facet> type() {
        return ProjectingFacet.class;
    }
    private final Projecting projecting;

    private ProjectingFacetFromPropertyAnnotation(
            final Projecting projecting,
            final FacetHolder holder) {
        super( holder);
        this.projecting = projecting;
    }

    public static ProjectingFacet create(final Property property, final FacetedMethod facetHolder) {
        if(property == null) {
            return null;
        }
        final Projecting projecting = property.projecting();
        switch (projecting) {
            case PROJECTED:
                return new ProjectingFacetFromPropertyAnnotation(projecting, facetHolder);
            case NOT_SPECIFIED:
            default:
                return null;
        }

    }

    @Override
    public Projecting value() {
        return projecting;
    }

}
