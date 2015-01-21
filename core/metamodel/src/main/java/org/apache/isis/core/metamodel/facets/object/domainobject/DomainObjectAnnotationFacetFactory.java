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


import org.apache.isis.applib.annotation.Audited;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.QuerySubmitterAware;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacet;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.object.domainobject.auditing.AuditableFacetForAuditedAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.auditing.AuditableFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.choices.ChoicesFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.ImmutableFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.objectspecid.ObjectSpecIdFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.publishing.PublishedObjectFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.publishing.PublishedObjectFacetForPublishedObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.recreatable.RecreatableObjectFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.immutableannot.ImmutableFacetForImmutableAnnotation;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;


public class DomainObjectAnnotationFacetFactory extends FacetFactoryAbstract implements IsisConfigurationAware, AdapterManagerAware, ServicesInjectorAware, SpecificationLoaderAware, QuerySubmitterAware {

    private IsisConfiguration configuration;
    private AdapterManager adapterManager;
    private ServicesInjector servicesInjector;
    private QuerySubmitter querySubmitter;

    public DomainObjectAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {

        processAuditing(processClassContext);
        processPublishing(processClassContext);
        processAutoComplete(processClassContext);
        processBounded(processClassContext);
        processEditing(processClassContext);
        processObjectType(processClassContext);
        processNature(processClassContext);
    }

    void processAuditing(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder holder = processClassContext.getFacetHolder();

        //
        // this rule originally implemented only in AuditableFacetFromConfigurationFactory
        // but think should apply in general
        //
        if(HasTransactionId.class.isAssignableFrom(cls)) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }


        AuditableFacet auditableFacet;

        // check for the deprecated annotation first
        final Audited annotation = Annotations.getAnnotation(cls, Audited.class);
        auditableFacet = AuditableFacetForAuditedAnnotation.create(annotation, holder);

        // else check for @DomainObject(auditing=....)
        if(auditableFacet == null) {
            auditableFacet = AuditableFacetForDomainObjectAnnotation.create(domainObject, configuration, holder);
        }

        // then add
        FacetUtil.addFacet(auditableFacet);
    }


    void processPublishing(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing, see above
        //
        if(HasTransactionId.class.isAssignableFrom(cls)) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        PublishedObjectFacet publishedObjectFacet;

        // check for the deprecated @PublishedObject annotation first
        final PublishedObject publishedObject = Annotations.getAnnotation(processClassContext.getCls(), PublishedObject.class);
        publishedObjectFacet = PublishedObjectFacetForPublishedObjectAnnotation.create(publishedObject, facetHolder);

        // else check from @DomainObject(publishing=...)
        if(publishedObjectFacet == null) {
            publishedObjectFacet=
                    PublishedObjectFacetForDomainObjectAnnotation.create(domainObject, configuration, facetHolder);
        }

        // then add
        FacetUtil.addFacet(publishedObjectFacet);
    }

    void processAutoComplete(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final AutoCompleteFacet facet = AutoCompleteFacetForDomainObjectAnnotation.create(
                domainObject, getSpecificationLoader(), adapterManager, servicesInjector, facetHolder);

        FacetUtil.addFacet(facet);
    }

    void processBounded(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final Facet facet = ChoicesFacetForDomainObjectAnnotation.create(domainObject, querySubmitter, facetHolder);

        FacetUtil.addFacet(facet);
    }

    void processEditing(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        // check for the deprecated annotation first
        final Immutable annotation = Annotations.getAnnotation(processClassContext.getCls(), Immutable.class);
        ImmutableFacet facet = ImmutableFacetForImmutableAnnotation.create(annotation, processClassContext.getFacetHolder());

        // else check from @DomainObject(editing=...)
        if(facet == null) {
            facet = ImmutableFacetForDomainObjectAnnotation.create(domainObject, configuration, facetHolder);
        }

        // then add
        FacetUtil.addFacet(facet);
    }

    void processObjectType(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final ObjectSpecIdFacet facet = ObjectSpecIdFacetForDomainObjectAnnotation.create(domainObject, facetHolder);

        FacetUtil.addFacet(facet);
    }

    void processNature(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final ViewModelFacet facet = RecreatableObjectFacetForDomainObjectAnnotation.create(
                domainObject, getSpecificationLoader(), adapterManager, servicesInjector, facetHolder);

        FacetUtil.addFacet(facet);
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
