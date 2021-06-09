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

package org.apache.isis.core.metamodel.facets.object.domainobject.recreatable;

import java.util.Objects;
import java.util.Optional;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.PostConstructMethodCache;
import org.apache.isis.core.metamodel.facets.object.recreatable.RecreatableObjectFacetDeclarativeInitializingAbstract;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;

public class RecreatableObjectFacetForDomainObjectAnnotation
extends RecreatableObjectFacetDeclarativeInitializingAbstract {

    public static ViewModelFacet create(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder,
            final PostConstructMethodCache postConstructMethodCache,
            final Facet.Precedence precedence) {

        return domainObjectIfAny
                .map(DomainObject::nature)
                .map(nature -> {
                    switch (nature) {
                    case NOT_SPECIFIED:
                    case BEAN:
                    case ENTITY:
                    case MIXIN:
                        // not a recreatable object, so no facet
                        return null;
                    case VIEW_MODEL:
                        return new RecreatableObjectFacetForDomainObjectAnnotation(
                                holder, postConstructMethodCache, precedence);
                    }
                    // shouldn't happen, the above switch should match all cases.
                    throw new IllegalArgumentException("nature of '" + nature + "' not recognized");
                })
                .filter(Objects::nonNull)
                .orElse(null);
    }

    private RecreatableObjectFacetForDomainObjectAnnotation(
            final FacetHolder holder,
            final PostConstructMethodCache postConstructMethodCache,
            final Facet.Precedence precedence) {

        super(holder, RecreationMechanism.INITIALIZES, postConstructMethodCache, precedence);
    }

}
