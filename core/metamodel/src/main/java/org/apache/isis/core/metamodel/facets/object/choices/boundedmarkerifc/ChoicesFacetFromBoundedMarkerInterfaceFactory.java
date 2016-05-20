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

package org.apache.isis.core.metamodel.facets.object.choices.boundedmarkerifc;

import java.lang.reflect.Method;

import org.apache.isis.applib.marker.Bounded;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;

public class ChoicesFacetFromBoundedMarkerInterfaceFactory extends FacetFactoryAbstract {

    private PersistenceSessionServiceInternal persistenceSessionServiceInternal;

    public ChoicesFacetFromBoundedMarkerInterfaceFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        final boolean implementsMarker = Bounded.class.isAssignableFrom(processClassContaxt.getCls());
        FacetUtil.addFacet(create(implementsMarker, processClassContaxt.getFacetHolder()));
    }

    private ChoicesFacet create(
            final boolean implementsMarker,
            final FacetHolder holder) {
        return implementsMarker
                ? new ChoicesFacetFromBoundedMarkerInterface(
                    holder, getDeploymentCategory(), getAuthenticationSessionProvider(),
                persistenceSessionServiceInternal) : null;
    }

    public boolean recognizes(final Method method) {
        return false;
    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        this.persistenceSessionServiceInternal = servicesInjector.getPersistenceSessionServiceInternal();
    }

}
