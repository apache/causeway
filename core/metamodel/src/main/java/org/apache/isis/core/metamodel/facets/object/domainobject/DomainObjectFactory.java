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
package org.apache.isis.core.metamodel.facets.object.domainobject;


import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.QuerySubmitterAware;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;


public class DomainObjectFactory extends FacetFactoryAbstract implements IsisConfigurationAware, AdapterManagerAware, ServicesInjectorAware, SpecificationLoaderAware, QuerySubmitterAware {

    private IsisConfiguration configuration;
    private AdapterManager adapterManager;
    private ServicesInjector servicesInjector;
    private QuerySubmitter querySubmitter;

    public DomainObjectFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {

        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        if(domainObject == null) {
            return;
        }

        processAuditing(processClassContext);
        processPublishing(processClassContext);
        processAutoComplete(processClassContext);
        processBounded(processClassContext);
        processEditing(processClassContext);
        processObjectType(processClassContext);
        processViewModel(processClassContext);
    }

    private void processAuditing(ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        //
        // this rule originally implemented only in AuditableFacetFromConfigurationFactory
        // but think should apply in general
        //
        if(HasTransactionId.class.isAssignableFrom(cls)) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        FacetUtil.addFacet(AuditableFacetForDomainObjectAnnotation.create(domainObject, configuration, facetHolder));
    }


    private void processPublishing(ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacet(PublishedObjectFacetForDomainObjectAnnotation.create(domainObject, configuration, facetHolder));
    }

    private void processAutoComplete(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacet(AutoCompleteFacetForDomainObjectAnnotation.create(domainObject, getSpecificationLoader(), adapterManager, servicesInjector, facetHolder));
    }

    private void processBounded(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacet(ChoicesFacetForDomainObjectAnnotation.create(domainObject, querySubmitter, facetHolder));
    }

    private void processEditing(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacet(ImmutableFacetForDomainObjectAnnotation.create(domainObject, configuration, facetHolder));
    }

    private void processObjectType(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacet(ObjectSpecIdFacetForDomainObjectAnnotation.create(domainObject, facetHolder));
    }

    private void processViewModel(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacet(ViewModelFacetForDomainObjectAnnotation.create(domainObject, getSpecificationLoader(), adapterManager, servicesInjector, facetHolder));
    }


    @Override
    public void setConfiguration(IsisConfiguration configuration) {
        this.configuration = configuration;
    }


    @Override
    public void setAdapterManager(AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    @Override
    public void setServicesInjector(ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

    @Override
    public void setQuerySubmitter(final QuerySubmitter querySubmitter) {
        this.querySubmitter = querySubmitter;
    }

}
