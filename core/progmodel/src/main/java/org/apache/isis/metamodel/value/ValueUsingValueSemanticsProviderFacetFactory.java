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


package org.apache.isis.metamodel.value;

import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.facets.object.value.ValueFacetUsingSemanticsProvider;


public abstract class ValueUsingValueSemanticsProviderFacetFactory extends FacetFactoryAbstract implements RuntimeContextAware,
        IsisConfigurationAware {

    private RuntimeContext runtimeContext;
    private IsisConfiguration configuration;

    protected ValueUsingValueSemanticsProviderFacetFactory(final Class<? extends Facet> adapterFacetType) {
        super(ObjectFeatureType.OBJECTS_ONLY);
    }

    protected void addFacets(final ValueSemanticsProviderAbstract adapter) {
        ValueFacetUsingSemanticsProvider facet = new ValueFacetUsingSemanticsProvider(adapter, adapter, getRuntimeContext());
        FacetUtil.addFacet(facet);
    }

    // ////////////////////////////////////////////////////
    // Dependencies (injected via setter)
    // ////////////////////////////////////////////////////

    protected RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    public void setRuntimeContext(final RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

    public void setIsisConfiguration(IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    public IsisConfiguration getConfiguration() {
        return configuration;
    }

}
