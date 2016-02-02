/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.core.metamodel.facets.object.grid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.layout.GridService;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.services.grid.GridNormalizerService;

public class GridFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware {

    private static final Logger LOG = LoggerFactory.getLogger(GridFacetFactory.class);

    public GridFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final TranslationService translationService =
                servicesInjector.lookupService(TranslationService.class);
        final GridService gridService =
                servicesInjector.lookupService(GridService.class);
        final GridNormalizerService gridNormalizerService =
                servicesInjector.lookupService(GridNormalizerService.class);

        FacetUtil.addFacet(
                GridFacetDefault.create(facetHolder,
                        translationService, gridService, gridNormalizerService, getDeploymentCategory()));
    }

    private ServicesInjector servicesInjector;

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

}
