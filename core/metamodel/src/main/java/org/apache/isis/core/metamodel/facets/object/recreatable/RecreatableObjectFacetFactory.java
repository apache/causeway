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

package org.apache.isis.core.metamodel.facets.object.recreatable;

import org.apache.isis.applib.RecreatableDomainObject;
import org.apache.isis.applib.ViewModel;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

public class RecreatableObjectFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware, AdapterManagerAware, MetaModelValidatorRefiner {

    private ServicesInjector servicesInjector;
    private AdapterManager adapterManager;

    public RecreatableObjectFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    /**
     * We simply attach all facets we can find; the {@link #refineMetaModelValidator(org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite, org.apache.isis.core.commons.config.IsisConfiguration) meta-model validation} will detect if multiple interfaces/annotations have
     * been attached.
     */
    @Override
    public void process(final ProcessClassContext processClassContext) {

        // ViewModel interface
        if (ViewModel.class.isAssignableFrom(processClassContext.getCls())) {
            FacetUtil.addFacet(new RecreatableObjectFacetForViewModelInterface(processClassContext.getFacetHolder()));
        }

        // ViewModel annotation
        final org.apache.isis.applib.annotation.ViewModel annotation = Annotations.getAnnotation(processClassContext.getCls(), org.apache.isis.applib.annotation.ViewModel.class);
        FacetUtil.addFacet(create(annotation, processClassContext.getFacetHolder()));

        // RecreatableDomainObject interface
        if (RecreatableDomainObject.class.isAssignableFrom(processClassContext.getCls())) {
            FacetUtil.addFacet(new RecreatableObjectFacetForRecreatableDomainObjectInterface(processClassContext.getFacetHolder()));
        }

        // DomainObject(nature=VIEW_MODEL) is managed by the DomainObjectFacetFactory
    }

    private ViewModelFacet create(final org.apache.isis.applib.annotation.ViewModel annotation, final FacetHolder holder) {
        return annotation != null ? new RecreatableObjectFacetForViewModelAnnotation(holder, getSpecificationLoader(), adapterManager, servicesInjector) : null;
    }

    // //////////////////////////////////////

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(final ObjectSpecification objectSpec, final ValidationFailures validationFailures) {
                final ViewModelFacet facet = objectSpec.getFacet(ViewModelFacet.class);
                final Facet underlyingFacet = facet != null ? facet.getUnderlyingFacet() : null;
                if(underlyingFacet != null) {
                    validationFailures.add(
                            "Class '%s' has multiple incompatible annotations/interfaces indicating that " +
                                    "it is a recreatable object of some sort (%s and %s)",
                            objectSpec.getFullIdentifier(),
                            facet.getClass().getSimpleName(),
                            underlyingFacet.getClass().getSimpleName());
                }
                return true;
            }
        }));
    }


    // //////////////////////////////////////



    @Override
    public void setServicesInjector(ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

    @Override
    public void setAdapterManager(AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

}
