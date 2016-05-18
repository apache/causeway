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

package org.apache.isis.core.metamodel.facets.object.value.vsp;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ConfigurationServiceInternal;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public abstract class ValueFacetUsingSemanticsProviderFactory<T> extends FacetFactoryAbstract implements AdapterManagerAware, ServicesInjectorAware {

    private IsisConfiguration configuration;
    private AdapterManager adapterManager;

    /**
     * Lazily created.
     */
    private ValueSemanticsProviderContext context;

    protected ValueFacetUsingSemanticsProviderFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    protected void addFacets(final ValueSemanticsProviderAndFacetAbstract<T> adapter) {
        final ValueFacetUsingSemanticsProvider facet = new ValueFacetUsingSemanticsProvider(adapter, adapter, getContext());
        FacetUtil.addFacet(facet);
    }

    // ////////////////////////////////////////////////////
    // Dependencies (injected via setter)
    // ////////////////////////////////////////////////////


    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    public ValueSemanticsProviderContext getContext() {
        if (context == null) {
            context = new ValueSemanticsProviderContext(getDeploymentCategory(), getSpecificationLoader(), adapterManager, servicesInjector);
        }
        return context;
    }


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        IsisConfiguration configuration = (IsisConfiguration) servicesInjector
                .lookupService(ConfigurationServiceInternal.class);
        this.configuration = configuration;
    }


    @Override
    public void setAdapterManager(final AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }


}
