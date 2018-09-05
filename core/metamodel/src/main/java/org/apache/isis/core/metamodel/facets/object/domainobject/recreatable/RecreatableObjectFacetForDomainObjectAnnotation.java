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

import java.util.List;
import java.util.Objects;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.PostConstructMethodCache;
import org.apache.isis.core.metamodel.facets.object.recreatable.RecreatableObjectFacetDeclarativeInitializingAbstract;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class RecreatableObjectFacetForDomainObjectAnnotation extends
RecreatableObjectFacetDeclarativeInitializingAbstract {

    public static ViewModelFacet create(
            final List<DomainObject> domainObjects,
            final ObjectAdapterProvider adapterProvider,
            final ServicesInjector servicesInjector,
            final FacetHolder holder,
            final PostConstructMethodCache postConstructMethodCache) {

        return domainObjects.stream()
                .map(DomainObject::nature)
                .map(nature -> {
                    switch (nature) {
                    case NOT_SPECIFIED:
                    case JDO_ENTITY:
                    case MIXIN:
                        // not a recreatable object, so no facet
                        return null;

                    case VIEW_MODEL:
                    case EXTERNAL_ENTITY:
                    case INMEMORY_ENTITY:
                        final ViewModelFacet existingFacet = holder.getFacet(ViewModelFacet.class);
                        if (existingFacet != null) {
                            return null;
                        }
                        return new RecreatableObjectFacetForDomainObjectAnnotation(
                                holder, adapterProvider, servicesInjector, postConstructMethodCache);
                    }
                    // shouldn't happen, the above switch should match all cases.
                    throw new IllegalArgumentException("nature of '" + nature + "' not recognized");
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private RecreatableObjectFacetForDomainObjectAnnotation(
            final FacetHolder holder,
            final ObjectAdapterProvider adapterProvider,
            final ServicesInjector servicesInjector,
            final PostConstructMethodCache postConstructMethodCache) {
        super(holder, RecreationMechanism.INITIALIZES, adapterProvider, servicesInjector,
                postConstructMethodCache);
    }

}
