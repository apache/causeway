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

package org.apache.isis.core.metamodel.facets.object.parseable.annotcfg;

import com.google.common.base.Strings;

import org.apache.isis.applib.annotation.Parseable;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAware;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.parseable.ParserUtil;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAware;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class ParseableFacetAnnotationElseConfigurationFactory extends FacetFactoryAbstract implements IsisConfigurationAware, AuthenticationSessionProviderAware, AdapterManagerAware, ServicesInjectorAware, RuntimeContextAware {

    private IsisConfiguration configuration;

    private AuthenticationSessionProvider authenticationSessionProvider;
    private AdapterManager adapterManager;
    private ServicesInjector servicesInjector;

    private RuntimeContext runtimeContext;

    public ParseableFacetAnnotationElseConfigurationFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        FacetUtil.addFacet(create(processClassContaxt.getCls(), processClassContaxt.getFacetHolder()));
    }

    private ParseableFacetAbstract create(final Class<?> cls, final FacetHolder holder) {
        final Parseable annotation = Annotations.getAnnotation(cls, Parseable.class);

        // create from annotation, if present
        if (annotation != null) {
            final ParseableFacetAnnotation facet = new ParseableFacetAnnotation(cls, getIsisConfiguration(), holder, getDeploymentCategory(), authenticationSessionProvider, adapterManager, servicesInjector);
            if (facet.isValid()) {
                return facet;
            }
        }

        // otherwise, try to create from configuration, if present
        final String parserName = ParserUtil.parserNameFromConfiguration(cls, getIsisConfiguration());
        if (!Strings.isNullOrEmpty(parserName)) {
            final ParseableFacetFromConfiguration facet = new ParseableFacetFromConfiguration(parserName, holder, getDeploymentCategory(), authenticationSessionProvider, servicesInjector, adapterManager);
            if (facet.isValid()) {
                return facet;
            }
        }

        return null;
    }

    // ////////////////////////////////////////////////////////////////////
    // Dependencies (injected via setters since *Aware)
    // ////////////////////////////////////////////////////////////////////

    /**
     * Derived from {@link #setRuntimeContext(RuntimeContext)} (since {@link RuntimeContextAware}).
     */
    private DeploymentCategory getDeploymentCategory() {
        return runtimeContext.getDeploymentCategory();
    }

    public IsisConfiguration getIsisConfiguration() {
        return configuration;
    }

    /**
     * Injected since {@link IsisConfigurationAware}.
     */
    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setAuthenticationSessionProvider(final AuthenticationSessionProvider authenticationSessionProvider) {
        this.authenticationSessionProvider = authenticationSessionProvider;
    }

    @Override
    public void setAdapterManager(final AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    @Override
    public void setServicesInjector(final ServicesInjector dependencyInjector) {
        this.servicesInjector = dependencyInjector;
    }

    @Override
    public void setRuntimeContext(RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

}
