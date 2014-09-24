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

package org.apache.isis.core.metamodel.facets.object.viewmodel.annotation;

import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class ViewModelFacetAnnotationFactory extends FacetFactoryAbstract implements ServicesInjectorAware, AdapterManagerAware {

    private ServicesInjector servicesInjector;
    private AdapterManager adapterManager;

    public ViewModelFacetAnnotationFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final ViewModel annotation = Annotations.getAnnotation(processClassContext.getCls(), ViewModel.class);
        FacetUtil.addFacet(create(annotation, processClassContext.getFacetHolder()));
    }

    private ViewModelFacet create(final ViewModel annotation, final FacetHolder holder) {
        return annotation != null ? new ViewModelFacetAnnotation(holder, getSpecificationLoader(), adapterManager, servicesInjector) : null;
    }

    @Override
    public void setServicesInjector(ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

    @Override
    public void setAdapterManager(AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }
}
