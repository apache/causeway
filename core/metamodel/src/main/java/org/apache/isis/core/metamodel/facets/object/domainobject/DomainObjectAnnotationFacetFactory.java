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

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.Audited;
import org.apache.isis.applib.annotation.AutoComplete;
import org.apache.isis.applib.annotation.Bounded;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.eventbus.CollectionDomainEvent;
import org.apache.isis.applib.services.eventbus.ObjectCreatedEvent;
import org.apache.isis.applib.services.eventbus.ObjectLoadedEvent;
import org.apache.isis.applib.services.eventbus.ObjectPersistedEvent;
import org.apache.isis.applib.services.eventbus.ObjectPersistingEvent;
import org.apache.isis.applib.services.eventbus.ObjectRemovingEvent;
import org.apache.isis.applib.services.eventbus.ObjectUpdatedEvent;
import org.apache.isis.applib.services.eventbus.ObjectUpdatingEvent;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.lang.Nullable;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.MethodFinderUtils;
import org.apache.isis.core.metamodel.facets.PostConstructMethodCache;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacet;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.auditing.AuditableFacetForAuditedAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.auditing.AuditableFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForAutoCompleteAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.choices.ChoicesFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.choices.ChoicesFacetFromBoundedAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.CollectionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.ImmutableFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.objectspecid.ObjectSpecIdFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.objectspecid.ObjectSpecIdFacetForJdoPersistenceCapableAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.objectspecid.ObjectSpecIdFacetFromObjectTypeAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.publishing.PublishedObjectFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.publishing.PublishedObjectFacetForPublishedObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.recreatable.RecreatableObjectFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.immutableannot.ImmutableFacetForImmutableAnnotation;
import org.apache.isis.core.metamodel.facets.object.mixin.MetaModelValidatorForMixinTypes;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForValidationFailures;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.core.metamodel.util.EventUtil;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;


public class DomainObjectAnnotationFacetFactory extends FacetFactoryAbstract
        implements MetaModelValidatorRefiner, PostConstructMethodCache {

    private final MetaModelValidatorForDeprecatedAnnotation auditedValidator = new MetaModelValidatorForDeprecatedAnnotation(Audited.class);
    private final MetaModelValidatorForDeprecatedAnnotation publishedObjectValidator = new MetaModelValidatorForDeprecatedAnnotation(PublishedObject.class);
    private final MetaModelValidatorForDeprecatedAnnotation autoCompleteValidator = new MetaModelValidatorForDeprecatedAnnotation(AutoComplete.class);
    private final MetaModelValidatorForDeprecatedAnnotation boundedValidator = new MetaModelValidatorForDeprecatedAnnotation(Bounded.class);
    private final MetaModelValidatorForDeprecatedAnnotation immutableValidator = new MetaModelValidatorForDeprecatedAnnotation(Immutable.class);
    private final MetaModelValidatorForDeprecatedAnnotation objectTypeValidator = new MetaModelValidatorForDeprecatedAnnotation(ObjectType.class);
    private final MetaModelValidatorForValidationFailures autoCompleteMethodInvalid = new MetaModelValidatorForValidationFailures();
    private final MetaModelValidatorForMixinTypes mixinTypeValidator = new MetaModelValidatorForMixinTypes("@DomainObject#nature=MIXIN");



    public DomainObjectAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        processAuditing(processClassContext);
        processPublishing(processClassContext);
        processAutoComplete(processClassContext);
        processBounded(processClassContext);
        processEditing(processClassContext);
        processObjectType(processClassContext);
        processNature(processClassContext);
        processLifecycleEvents(processClassContext);
        processDomainEvents(processClassContext);

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
        auditableFacet = auditedValidator.flagIfPresent(
                            AuditableFacetForAuditedAnnotation.create(annotation, holder), null);

        // else check for @DomainObject(auditing=....)
        if(auditableFacet == null) {
            auditableFacet = AuditableFacetForDomainObjectAnnotation.create(domainObject, getConfiguration(), holder);
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
        final PublishedObject publishedObject = Annotations.getAnnotation(processClassContext.getCls(),
                PublishedObject.class);
        publishedObjectFacet = publishedObjectValidator.flagIfPresent(
                                    PublishedObjectFacetForPublishedObjectAnnotation.create(publishedObject, facetHolder));

        // else check from @DomainObject(publishing=...)
        if(publishedObjectFacet == null) {
            publishedObjectFacet=
                    PublishedObjectFacetForDomainObjectAnnotation.create(domainObject, getConfiguration(), facetHolder);
        }

        // then add
        FacetUtil.addFacet(publishedObjectFacet);
    }

    void processAutoComplete(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        // check for the deprecated @AutoComplete annotation first
        final AutoComplete autoCompleteAnnot = Annotations.getAnnotation(cls, AutoComplete.class);
        Facet facet = autoCompleteValidator.flagIfPresent(createFor(facetHolder, autoCompleteAnnot, cls));

        // else check from @DomainObject(auditing=...)
        if(facet == null) {
            final DomainObject domainObjectAnnot = Annotations.getAnnotation(cls, DomainObject.class);
            facet = createFor(domainObjectAnnot, facetHolder, cls);
        }

        // then add
        FacetUtil.addFacet(facet);
    }

    private AutoCompleteFacet createFor(
            final FacetHolder facetHolder,
            final AutoComplete annotation,
            final Class<?> cls) {
        if(annotation == null) {
            return null;
        }

        final Class<?> repositoryClass = annotation.repository();
        final String actionName = annotation.action();

        final Method repositoryMethod = findRepositoryMethod(cls, "@AutoComplete", repositoryClass, actionName);
        if(repositoryMethod == null) {
            return null;
        }
        return new AutoCompleteFacetForAutoCompleteAnnotation(
                        facetHolder, repositoryClass, repositoryMethod, servicesInjector);
    }

    private AutoCompleteFacet createFor(
            final DomainObject domainObject,
            final FacetHolder facetHolder,
            final Class<?> cls) {
        if(domainObject == null) {
            return null;
        }

        final Class<?> repositoryClass = domainObject.autoCompleteRepository();
        if(repositoryClass == Object.class) {
            return null;
        }
        final String actionName = domainObject.autoCompleteAction();

        final Method repositoryMethod = findRepositoryMethod(cls, "@DomainObject", repositoryClass, actionName);
        if(repositoryMethod == null) {
            return null;
        }

        return new AutoCompleteFacetForDomainObjectAnnotation(
                        facetHolder, repositoryClass, repositoryMethod, servicesInjector);
    }

    private Method findRepositoryMethod(
            final Class<?> cls,
            final String annotationName,
            final Class<?> repositoryClass,
            final String methodName) {
        final Method[] methods = repositoryClass.getMethods();
        for (Method method : methods) {
            if(method.getName().equals(methodName)) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if(parameterTypes.length == 1 && parameterTypes[0].equals(String.class)) {
                    return method;
                }
            }
        }
        autoCompleteMethodInvalid.addFailure(
                "%s annotation on %s specifies method '%s' that does not exist in repository '%s'",
                annotationName, cls.getName(), methodName, repositoryClass.getName());
        return null;
    }

    void processBounded(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        // check for the deprecated @Bounded annotation first
        final Bounded annotation = Annotations.getAnnotation(processClassContext.getCls(), Bounded.class);
        Facet facet = boundedValidator.flagIfPresent(
            ChoicesFacetFromBoundedAnnotation.create(annotation, processClassContext.getFacetHolder(),
                    getDeploymentCategory(),
                    getAuthenticationSessionProvider(),
                    persistenceSessionServiceInternal));

        // else check from @DomainObject(bounded=...)
        if(facet == null) {
            facet = ChoicesFacetForDomainObjectAnnotation.create(domainObject, facetHolder, getDeploymentCategory(),
                    getAuthenticationSessionProvider(), persistenceSessionServiceInternal);
        }

        // then add
        FacetUtil.addFacet(facet);
    }

    void processEditing(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        // check for the deprecated annotation first
        final Immutable annotation = Annotations.getAnnotation(processClassContext.getCls(), Immutable.class);
        ImmutableFacet facet = immutableValidator.flagIfPresent(
                ImmutableFacetForImmutableAnnotation.create(annotation, processClassContext.getFacetHolder()));

        // else check from @DomainObject(editing=...)
        if(facet == null) {
            facet = ImmutableFacetForDomainObjectAnnotation.create(domainObject, getConfiguration(), facetHolder);
        }

        // then add
        FacetUtil.addFacet(facet);
    }

    void processObjectType(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        // check for the deprecated annotation first
        final ObjectType annotation = Annotations.getAnnotation(processClassContext.getCls(), ObjectType.class);
        Facet facet = objectTypeValidator.flagIfPresent(
                ObjectSpecIdFacetFromObjectTypeAnnotation.create(annotation, processClassContext.getFacetHolder()));

        // else check from @DomainObject(objectType=...)
        if(facet == null) {
            facet = ObjectSpecIdFacetForDomainObjectAnnotation.create(domainObject, facetHolder);
        }

        // else check for @PersistenceCapable(schema=...)
        if(facet == null) {
            final JdoPersistenceCapableFacet jdoPersistenceCapableFacet = facetHolder.getFacet(JdoPersistenceCapableFacet.class);
            if(jdoPersistenceCapableFacet != null) {
                facet = ObjectSpecIdFacetForJdoPersistenceCapableAnnotation.create(jdoPersistenceCapableFacet, facetHolder);
            }
        }

        // then add
        FacetUtil.addFacet(facet);
    }


    void processNature(final ProcessClassContext processClassContext) {
        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);

        if(domainObject == null) {
            return;
        }

        final FacetHolder facetHolder = processClassContext.getFacetHolder();

        final PostConstructMethodCache postConstructMethodCache = this;
        final ViewModelFacet recreatableObjectFacet = RecreatableObjectFacetForDomainObjectAnnotation.create(
                domainObject, getSpecificationLoader(), persistenceSessionServiceInternal, servicesInjector,
                facetHolder, postConstructMethodCache);

        if(recreatableObjectFacet != null) {
            FacetUtil.addFacet(recreatableObjectFacet);
        } else {
            if(domainObject.nature() == Nature.MIXIN) {

                if(!mixinTypeValidator.ensureMixinType(cls)) {
                    return;
                }

                final MixinFacet mixinFacet = MixinFacetForDomainObjectAnnotation.create(domainObject, cls, facetHolder, servicesInjector);
                FacetUtil.addFacet(mixinFacet);
            }
        }

    }


    private void processLifecycleEvents(final ProcessClassContext processClassContext) {

        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        if(domainObject == null) {
            return;
        }
        final FacetHolder holder = processClassContext.getFacetHolder();

        processLifecycleEventCreated(domainObject, holder);
        processLifecycleEventLoaded(domainObject, holder);
        processLifecycleEventPersisted(domainObject, holder);
        processLifecycleEventPersisting(domainObject, holder);
        processLifecycleEventRemoving(domainObject, holder);
        processLifecycleEventUpdated(domainObject, holder);
        processLifecycleEventUpdating(domainObject, holder);
    }


    private void processDomainEvents(final ProcessClassContext processClassContext) {

        final Class<?> cls = processClassContext.getCls();
        final DomainObject domainObject = Annotations.getAnnotation(cls, DomainObject.class);
        if(domainObject == null) {
            return;
        }
        final FacetHolder holder = processClassContext.getFacetHolder();

        processDomainEventAction(domainObject, holder);
        processDomainEventProperty(domainObject, holder);
        processDomainEventCollection(domainObject, holder);
    }

    private void processLifecycleEventCreated(final DomainObject domainObject, final FacetHolder holder) {
        final Class<? extends ObjectCreatedEvent<?>> lifecycleEvent = domainObject.createdLifecycleEvent();

        final CreatedLifecycleEventFacetForDomainObjectAnnotation facet =
                new CreatedLifecycleEventFacetForDomainObjectAnnotation(
                        holder, lifecycleEvent, getSpecificationLoader());

        if(EventUtil.eventTypeIsPostable(
                facet.getEventType(),
                ObjectCreatedEvent.Noop.class,
                ObjectCreatedEvent.Default.class,
                "isis.reflector.facet.domainObjectAnnotation.createdLifecycleEvent.postForDefault",
                getConfiguration())) {
            FacetUtil.addFacet(facet);
        }
    }

    private void processLifecycleEventLoaded(final DomainObject domainObject, final FacetHolder holder) {
        final Class<? extends ObjectLoadedEvent<?>> lifecycleEvent = domainObject.loadedLifecycleEvent();

        final LoadedLifecycleEventFacetForDomainObjectAnnotation facet =
                new LoadedLifecycleEventFacetForDomainObjectAnnotation(
                        holder, lifecycleEvent, getSpecificationLoader());

        if(EventUtil.eventTypeIsPostable(
                facet.getEventType(),
                ObjectLoadedEvent.Noop.class,
                ObjectLoadedEvent.Default.class,
                "isis.reflector.facet.domainObjectAnnotation.loadedLifecycleEvent.postForDefault",
                getConfiguration())) {
            FacetUtil.addFacet(facet);
        }
    }

    private void processLifecycleEventPersisting(final DomainObject domainObject, final FacetHolder holder) {
        final Class<? extends ObjectPersistingEvent<?>> lifecycleEvent = domainObject.persistingLifecycleEvent();

        final PersistingLifecycleEventFacetForDomainObjectAnnotation facet =
                new PersistingLifecycleEventFacetForDomainObjectAnnotation(
                        holder, lifecycleEvent, getSpecificationLoader());

        if(EventUtil.eventTypeIsPostable(
                facet.getEventType(),
                ObjectPersistingEvent.Noop.class,
                ObjectPersistingEvent.Default.class,
                "isis.reflector.facet.domainObjectAnnotation.persistingLifecycleEvent.postForDefault",
                getConfiguration())) {
            FacetUtil.addFacet(facet);
        }
    }

    private void processLifecycleEventPersisted(final DomainObject domainObject, final FacetHolder holder) {
        final Class<? extends ObjectPersistedEvent<?>> lifecycleEvent = domainObject.persistedLifecycleEvent();

        final PersistedLifecycleEventFacetForDomainObjectAnnotation facet =
                new PersistedLifecycleEventFacetForDomainObjectAnnotation(
                        holder, lifecycleEvent, getSpecificationLoader());

        if(EventUtil.eventTypeIsPostable(
                facet.getEventType(),
                ObjectPersistedEvent.Noop.class,
                ObjectPersistedEvent.Default.class,
                "isis.reflector.facet.domainObjectAnnotation.persistedLifecycleEvent.postForDefault",
                getConfiguration())) {
            FacetUtil.addFacet(facet);
        }
    }

    private void processLifecycleEventRemoving(final DomainObject domainObject, final FacetHolder holder) {
        final Class<? extends ObjectRemovingEvent<?>> lifecycleEvent = domainObject.removingLifecycleEvent();

        final RemovingLifecycleEventFacetForDomainObjectAnnotation facet =
                new RemovingLifecycleEventFacetForDomainObjectAnnotation(
                        holder, lifecycleEvent, getSpecificationLoader());

        if(EventUtil.eventTypeIsPostable(
                facet.getEventType(),
                ObjectRemovingEvent.Noop.class,
                ObjectRemovingEvent.Default.class,
                "isis.reflector.facet.domainObjectAnnotation.removingLifecycleEvent.postForDefault",
                getConfiguration())) {
            FacetUtil.addFacet(facet);
        }
    }

    private void processLifecycleEventUpdated(final DomainObject domainObject, final FacetHolder holder) {
        final Class<? extends ObjectUpdatedEvent<?>> lifecycleEvent = domainObject.updatedLifecycleEvent();

        final UpdatedLifecycleEventFacetForDomainObjectAnnotation facet =
                new UpdatedLifecycleEventFacetForDomainObjectAnnotation(
                        holder, lifecycleEvent, getSpecificationLoader());

        if(EventUtil.eventTypeIsPostable(
                facet.getEventType(),
                ObjectUpdatedEvent.Noop.class,
                ObjectUpdatedEvent.Default.class,
                "isis.reflector.facet.domainObjectAnnotation.updatedLifecycleEvent.postForDefault",
                getConfiguration())) {
            FacetUtil.addFacet(facet);
        }
    }

    private void processLifecycleEventUpdating(final DomainObject domainObject, final FacetHolder holder) {
        final Class<? extends ObjectUpdatingEvent<?>> lifecycleEvent = domainObject.updatingLifecycleEvent();

        final UpdatingLifecycleEventFacetForDomainObjectAnnotation facet =
                new UpdatingLifecycleEventFacetForDomainObjectAnnotation(
                        holder, lifecycleEvent, getSpecificationLoader());

        if(EventUtil.eventTypeIsPostable(
                facet.getEventType(),
                ObjectUpdatingEvent.Noop.class,
                ObjectUpdatingEvent.Default.class,
                "isis.reflector.facet.domainObjectAnnotation.updatingLifecycleEvent.postForDefault",
                getConfiguration())) {
            FacetUtil.addFacet(facet);
        }
    }

    private void processDomainEventAction(final DomainObject domainObject, final FacetHolder holder) {
        final Class<? extends ActionDomainEvent<?>> domainEvent = domainObject.actionDomainEvent();

        if(domainEvent != ActionDomainEvent.Default.class) {
            final ActionDomainEventDefaultFacetForDomainObjectAnnotation facet =
                    new ActionDomainEventDefaultFacetForDomainObjectAnnotation(
                        holder, domainEvent, getSpecificationLoader());
            FacetUtil.addFacet(facet);
        }
    }

    private void processDomainEventProperty(final DomainObject domainObject, final FacetHolder holder) {
        final Class<? extends PropertyDomainEvent<?,?>> domainEvent = domainObject.propertyDomainEvent();

        if(domainEvent != PropertyDomainEvent.Default.class) {
            final PropertyDomainEventDefaultFacetForDomainObjectAnnotation facet =
                    new PropertyDomainEventDefaultFacetForDomainObjectAnnotation(
                        holder, domainEvent, getSpecificationLoader());
            FacetUtil.addFacet(facet);
        }
    }

    private void processDomainEventCollection(final DomainObject domainObject, final FacetHolder holder) {
        final Class<? extends CollectionDomainEvent<?,?>> domainEvent = domainObject.collectionDomainEvent();

        if(domainEvent != CollectionDomainEvent.Default.class) {
            final CollectionDomainEventDefaultFacetForDomainObjectAnnotation facet =
                    new CollectionDomainEventDefaultFacetForDomainObjectAnnotation(
                        holder, domainEvent, getSpecificationLoader());
            FacetUtil.addFacet(facet);
        }
    }

    // //////////////////////////////////////

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {

        metaModelValidator.add(new MetaModelValidatorVisiting(new MetaModelValidatorVisiting.Visitor() {
            @Override
            public boolean visit(final ObjectSpecification thisSpec, final ValidationFailures validationFailures) {

                validate(thisSpec, validationFailures);
                return true;
            }

            private void validate(final ObjectSpecification thisSpec, final ValidationFailures validationFailures) {
                if(!thisSpec.isPersistenceCapableOrViewModel()) {
                    return;
                }

                final Map<ObjectSpecId, ObjectSpecification> specById = Maps.newHashMap();
                for (final ObjectSpecification otherSpec : getSpecificationLoader().allSpecifications()) {

                    if(thisSpec == otherSpec) {
                        continue;
                    }

                    if(!otherSpec.isPersistenceCapableOrViewModel()) {
                        continue;
                    }

                    final ObjectSpecId objectSpecId = otherSpec.getSpecId();
                    if (objectSpecId == null) {
                        continue;
                    }
                    final ObjectSpecification existingSpec = specById.put(objectSpecId, otherSpec);
                    if (existingSpec == null) {
                        continue;
                    }
                    validationFailures.add(
                            "%s: cannot have two entities with same object type (@Discriminator, @DomainObject(objectType=...), @ObjectType or @PersistenceCapable(schema=...)); %s " +
                            "has same value (%s).",
                            existingSpec.getFullIdentifier(),
                            otherSpec.getFullIdentifier(),
                            objectSpecId);
                }

                final AutoCompleteFacet autoCompleteFacet = thisSpec.getFacet(AutoCompleteFacet.class);
                if(autoCompleteFacet != null && !autoCompleteFacet.isNoop() && autoCompleteFacet instanceof AutoCompleteFacetAbstract) {
                    final AutoCompleteFacetAbstract facet = (AutoCompleteFacetForDomainObjectAnnotation) autoCompleteFacet;
                    final Class<?> repositoryClass = facet.getRepositoryClass();
                    final boolean isRegistered = servicesInjector.isRegisteredService(repositoryClass);
                    if(!isRegistered) {
                        validationFailures.add(
                                "@DomainObject annotation on %s specifies unknown repository '%s'",
                                thisSpec.getFullIdentifier(), repositoryClass.getName());
                    }

                }
            }

        }));

        metaModelValidator.add(publishedObjectValidator);
        metaModelValidator.add(auditedValidator);
        metaModelValidator.add(autoCompleteValidator);
        metaModelValidator.add(boundedValidator);
        metaModelValidator.add(immutableValidator);
        metaModelValidator.add(objectTypeValidator);

        metaModelValidator.add(autoCompleteMethodInvalid);
        metaModelValidator.add(mixinTypeValidator);
    }

    // //////////////////////////////////////


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        IsisConfiguration configuration = getConfiguration();

        publishedObjectValidator.setConfiguration(configuration);
        auditedValidator.setConfiguration(configuration);
        autoCompleteValidator.setConfiguration(configuration);
        boundedValidator.setConfiguration(configuration);
        immutableValidator.setConfiguration(configuration);
        objectTypeValidator.setConfiguration(configuration);

        this.persistenceSessionServiceInternal = servicesInjector.getPersistenceSessionServiceInternal();

    }


    // //////////////////////////////////////

    private final Map<Class, Nullable<Method>> postConstructMethods = Maps.newHashMap();

    public Method postConstructMethodFor(final Object pojo) {
        return MethodFinderUtils.findAnnotatedMethod(pojo, PostConstruct.class, postConstructMethods);
    }


    PersistenceSessionServiceInternal persistenceSessionServiceInternal;

}
