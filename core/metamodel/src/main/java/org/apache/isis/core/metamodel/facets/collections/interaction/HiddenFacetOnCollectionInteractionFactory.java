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

package org.apache.isis.core.metamodel.facets.collections.interaction;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class HiddenFacetOnCollectionInteractionFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory, ServicesInjectorAware {

    private ServicesInjector servicesInjector;

    public HiddenFacetOnCollectionInteractionFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        //FacetUtil.addFacet(createFacet(processMethodContext.getFacetHolder()));
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {
        //FacetUtil.addFacet(createFacet(processMemberContext.getFacetHolder()));
    }

    private HiddenFacetOnCollectionInteraction createFacet(FacetHolder facetHolder) {
        return new HiddenFacetOnCollectionInteraction(facetHolder, servicesInjector);
    }


    @Override
    public void setServicesInjector(ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

}
