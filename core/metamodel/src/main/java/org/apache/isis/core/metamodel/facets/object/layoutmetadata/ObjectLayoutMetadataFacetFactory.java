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
package org.apache.isis.core.metamodel.facets.object.layoutmetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.layout.ObjectLayoutMetadataService;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class ObjectLayoutMetadataFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectLayoutMetadataFacetFactory.class);

    public ObjectLayoutMetadataFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final TranslationService translationService = servicesInjector.lookupService(TranslationService.class);
        final ObjectLayoutMetadataService objectLayoutMetadataService = servicesInjector.lookupService(ObjectLayoutMetadataService.class);
        FacetUtil.addFacet(
                ObjectLayoutMetadataFacetDefault.create(facetHolder,
                        translationService, objectLayoutMetadataService));
    }

    private ServicesInjector servicesInjector;

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

}
