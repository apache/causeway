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

package org.apache.isis.core.metamodel.facets;

import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.ServicesInjectorAware;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public abstract class FacetFactoryAbstract implements FacetFactory, ServicesInjectorAware {

    private final List<FeatureType> featureTypes;

    public FacetFactoryAbstract(final List<FeatureType> featureTypes) {
        this.featureTypes = _Lists.unmodifiable(featureTypes);
    }

    @Override
    public List<FeatureType> getFeatureTypes() {
        return featureTypes;
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
    }

    @Override
    public void processParams(final ProcessParameterContext processParameterContext) {
    }

    // -- dependencies (injected)

    protected ServicesInjector servicesInjector;

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }


    // -- dependencies (looked up from services injector)

    protected SpecificationLoader getSpecificationLoader() {
        return servicesInjector.getSpecificationLoader();
    }

    protected AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return servicesInjector.getAuthenticationSessionProvider();
    }

    protected IsisConfiguration getConfiguration() {
        return _Config.getConfiguration();
    }


}
