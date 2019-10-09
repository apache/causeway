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
package org.apache.isis.metamodel.facets.object.domainobject;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.events.lifecycle.ObjectCreatedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectLoadedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectPersistedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectPersistingEvent;
import org.apache.isis.applib.events.lifecycle.ObjectRemovingEvent;
import org.apache.isis.applib.events.lifecycle.ObjectUpdatedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectUpdatingEvent;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.ObjectSpecIdFacetFactory;
import org.apache.isis.metamodel.facets.PostConstructMethodCache;
import org.apache.isis.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.metamodel.facets.object.autocomplete.AutoCompleteFacetAbstract;
import org.apache.isis.metamodel.facets.object.callbacks.CreatedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.callbacks.LoadedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.callbacks.PersistedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.callbacks.PersistingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.callbacks.RemovingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.callbacks.UpdatingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.auditing.AuditableFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.choices.ChoicesFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.domainevents.CollectionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.editing.ImmutableFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.objectspecid.ObjectSpecIdFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.publishing.PublishedObjectFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.recreatable.RecreatableObjectFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.metamodel.facets.object.mixin.MetaModelValidatorForMixinTypes;
import org.apache.isis.metamodel.facets.object.mixin.MixinFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorForValidationFailures;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.metamodel.util.EventUtil;

import lombok.val;


public class DomainObjectAnnotationFacetFactory extends FacetFactoryAbstract
implements MetaModelRefiner, PostConstructMethodCache, ObjectSpecIdFacetFactory {

    private final MetaModelValidatorForValidationFailures autoCompleteMethodInvalid = 
            new MetaModelValidatorForValidationFailures();
    private final MetaModelValidatorForMixinTypes mixinTypeValidator = 
            new MetaModelValidatorForMixinTypes("@DomainObject#nature=MIXIN");



    public DomainObjectAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessObjectSpecIdContext processClassContext) {
        processObjectType(processClassContext);
    }


    @Override
    public void process(final ProcessClassContext processClassContext) {
        
        processAuditing(processClassContext);
        processPublishing(processClassContext);
        processAutoComplete(processClassContext);
        processBounded(processClassContext);
        processEditing(processClassContext);
        processNature(processClassContext);
        processLifecycleEvents(processClassContext);
        processDomainEvents(processClassContext);

    }


    void processAuditing(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();
        
        //
        // this rule originally implemented only in AuditableFacetFromConfigurationFactory
        // but think should apply in general
        //
        if(HasUniqueId.class.isAssignableFrom(cls)) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @DomainObject(auditing=....)
        val auditing = processClassContext.synthesizeOnType(DomainObject.class).map(DomainObject::auditing);
        val auditableFacet = AuditableFacetForDomainObjectAnnotation
                .create(auditing, getConfiguration(), facetHolder);

        // then add
        FacetUtil.addFacet(auditableFacet);
    }


    void processPublishing(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing, see above
        //
        if(HasUniqueId.class.isAssignableFrom(cls)) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check from @DomainObject(publishing=...)
        val publishing = processClassContext.synthesizeOnType(DomainObject.class).map(DomainObject::publishing);
        val publishedObjectFacet = PublishedObjectFacetForDomainObjectAnnotation
                .create(publishing, getConfiguration(), facetHolder);

        // then add
        FacetUtil.addFacet(publishedObjectFacet);
    }
    
    // -- AUTO COMPLETE

    void processAutoComplete(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        // check from @DomainObject(autoCompleteRepository=...)
        val domainObjectIfAny = processClassContext.synthesizeOnType(DomainObject.class);
        val facet = createFor(domainObjectIfAny, facetHolder, cls);

        // then add
        FacetUtil.addFacet(facet);
    }
    
    private final static class AnnotHelper {
        AnnotHelper(DomainObject domainObject) {
            this.autoCompleteRepository = domainObject.autoCompleteRepository();
            this.autoCompleteAction = domainObject.autoCompleteAction();
        }
        final Class<?> autoCompleteRepository;
        final String autoCompleteAction;
        Method repositoryMethod;
    }

    private AutoCompleteFacet createFor(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder facetHolder,
            final Class<?> cls) {
        
        if(!domainObjectIfAny.isPresent()) {
            return null;
        }

        return domainObjectIfAny
                .map(domainObject -> new AnnotHelper(domainObject))
                .filter(a -> a.autoCompleteRepository != Object.class)
                .filter(a -> {
                    a.repositoryMethod = findRepositoryMethod(
                            facetHolder, 
                            cls, 
                            "@DomainObject", 
                            a.autoCompleteRepository, 
                            a.autoCompleteAction);
                    
                    return a.repositoryMethod != null;
                })
                .map(a -> new AutoCompleteFacetForDomainObjectAnnotation(
                        facetHolder, a.autoCompleteRepository, a.repositoryMethod))
                .orElse(null);
    }

    private Method findRepositoryMethod(
            final FacetHolder facetHolder,
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
        autoCompleteMethodInvalid.onFailure(
                facetHolder,
                Identifier.classIdentifier(cls),
                "%s annotation on %s specifies method '%s' that does not exist in repository '%s'",
                annotationName, cls.getName(), methodName, repositoryClass.getName());
        return null;
    }
    
    // -- BOUNDED

    void processBounded(final ProcessClassContext processClassContext) {
        val facetHolder = processClassContext.getFacetHolder();

        // check from @DomainObject(bounded=...)
        val domainObjectIfAny = processClassContext.synthesizeOnType(DomainObject.class);
        val facet = ChoicesFacetForDomainObjectAnnotation.create(domainObjectIfAny, facetHolder);

        // then add
        FacetUtil.addFacet(facet);
    }

    void processEditing(final ProcessClassContext processClassContext) {
        val facetHolder = processClassContext.getFacetHolder();

        // check from @DomainObject(editing=...)
        val domainObjectIfAny = processClassContext.synthesizeOnType(DomainObject.class);
        ImmutableFacet facet = ImmutableFacetForDomainObjectAnnotation
                .create(domainObjectIfAny, getConfiguration(), facetHolder);

        // then add
        FacetUtil.addFacet(facet);
    }

    void processObjectType(final ProcessObjectSpecIdContext processClassContext) {
        
        val facetHolder = processClassContext.getFacetHolder();

        // check from @DomainObject(objectType=...)
        val domainObjectIfAny = processClassContext.synthesizeOnType(DomainObject.class);
        val facet = ObjectSpecIdFacetForDomainObjectAnnotation.create(domainObjectIfAny, facetHolder);

        //FIXME [2033] removed here (module 'metamodel'), should be re-implemented in 'jdo-common'         
        // else check for @PersistenceCapable(schema=...)
        //        if(facet == null) {
        //            final JdoPersistenceCapableFacet jdoPersistenceCapableFacet = facetHolder.getFacet(JdoPersistenceCapableFacet.class);
        //            if(jdoPersistenceCapableFacet != null) {
        //                facet = ObjectSpecIdFacetForJdoPersistenceCapableAnnotation.create(jdoPersistenceCapableFacet, facetHolder);
        //            }
        //        }

        // then add
        FacetUtil.addFacet(facet);
    }


    void processNature(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();
        val domainObjectIfAny = processClassContext.synthesizeOnType(DomainObject.class);

        if(!domainObjectIfAny.isPresent()) {
            return;
        }

        val postConstructMethodCache = this;
        final ViewModelFacet recreatableObjectFacet = 
                RecreatableObjectFacetForDomainObjectAnnotation.create(
                    domainObjectIfAny, 
                    facetHolder, 
                    postConstructMethodCache);

        if(recreatableObjectFacet != null) {
            FacetUtil.addFacet(recreatableObjectFacet);
        } else {

            val mixinDomainObjectIfAny =
                    domainObjectIfAny
                    .filter(domainObject -> domainObject.nature() == Nature.MIXIN)
                    .filter(domainObject -> mixinTypeValidator.ensureMixinType(facetHolder, cls));

            val mixinFacet = 
                    MixinFacetForDomainObjectAnnotation.create(mixinDomainObjectIfAny, cls, facetHolder, getServiceInjector());
            FacetUtil.addFacet(mixinFacet);
        }

    }


    private void processLifecycleEvents(final ProcessClassContext processClassContext) {
        
        val domainObjectIfAny = processClassContext.synthesizeOnType(DomainObject.class);
        if(!domainObjectIfAny.isPresent()) {
            return;
        }
        val facetHolder = processClassContext.getFacetHolder();
        
        processLifecycleEventCreated(domainObjectIfAny, facetHolder);
        processLifecycleEventLoaded(domainObjectIfAny, facetHolder);
        processLifecycleEventPersisted(domainObjectIfAny, facetHolder);
        processLifecycleEventPersisting(domainObjectIfAny, facetHolder);
        processLifecycleEventRemoving(domainObjectIfAny, facetHolder);
        processLifecycleEventUpdated(domainObjectIfAny, facetHolder);
        processLifecycleEventUpdating(domainObjectIfAny, facetHolder);
    }


    private void processDomainEvents(final ProcessClassContext processClassContext) {
        val domainObjectIfAny = processClassContext.synthesizeOnType(DomainObject.class);
        if(!domainObjectIfAny.isPresent()) {
            return;
        }
        val facetHolder = processClassContext.getFacetHolder();

        processDomainEventAction(domainObjectIfAny, facetHolder);
        processDomainEventProperty(domainObjectIfAny, facetHolder);
        processDomainEventCollection(domainObjectIfAny, facetHolder);
    }

    private void processLifecycleEventCreated(final Optional<DomainObject> domainObjectIfAny, final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::createdLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectCreatedEvent.Noop.class,
                ObjectCreatedEvent.Default.class,
                getConfiguration().getReflector().getFacet().getDomainObjectAnnotation().getCreatedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new CreatedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(FacetUtil::addFacet);
    }

    private void processLifecycleEventLoaded(final Optional<DomainObject> domainObjectIfAny, final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::loadedLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectLoadedEvent.Noop.class,
                ObjectLoadedEvent.Default.class,
                getConfiguration().getReflector().getFacet().getDomainObjectAnnotation().getLoadedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new LoadedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(FacetUtil::addFacet);
    }

    private void processLifecycleEventPersisting(final Optional<DomainObject> domainObjectIfAny, final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::persistingLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectPersistingEvent.Noop.class,
                ObjectPersistingEvent.Default.class,
                getConfiguration().getReflector().getFacet().getDomainObjectAnnotation().getPersistingLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new PersistingLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(FacetUtil::addFacet);
    }

    private void processLifecycleEventPersisted(final Optional<DomainObject> domainObjectIfAny, final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::persistedLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectPersistedEvent.Noop.class,
                ObjectPersistedEvent.Default.class,
                getConfiguration().getReflector().getFacet().getDomainObjectAnnotation().getPersistedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new PersistedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(FacetUtil::addFacet);
    }

    private void processLifecycleEventRemoving(final Optional<DomainObject> domainObjectIfAny, final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::removingLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectRemovingEvent.Noop.class,
                ObjectRemovingEvent.Default.class,
                getConfiguration().getReflector().getFacet().getDomainObjectAnnotation().getRemovingLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new RemovingLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(FacetUtil::addFacet);
    }

    private void processLifecycleEventUpdated(final Optional<DomainObject> domainObjectIfAny, final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::updatedLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectUpdatedEvent.Noop.class,
                ObjectUpdatedEvent.Default.class,
                getConfiguration().getReflector().getFacet().getDomainObjectAnnotation().getUpdatedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new UpdatedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(FacetUtil::addFacet);
    }

    private void processLifecycleEventUpdating(final Optional<DomainObject> domainObjectIfAny, final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::updatingLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectUpdatingEvent.Noop.class,
                ObjectUpdatingEvent.Default.class,
                getConfiguration().getReflector().getFacet().getDomainObjectAnnotation().getUpdatingLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new UpdatingLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(FacetUtil::addFacet);
    }

    private void processDomainEventAction(final Optional<DomainObject> domainObjectIfAny, final FacetHolder holder) {
        
        domainObjectIfAny
        .map(DomainObject::actionDomainEvent)
        .filter(domainEvent -> domainEvent != ActionDomainEvent.Default.class)
        .map(domainEvent -> new ActionDomainEventDefaultFacetForDomainObjectAnnotation(
                holder, domainEvent))
        .ifPresent(FacetUtil::addFacet);

    }

    private void processDomainEventProperty(final Optional<DomainObject> domainObjectIfAny, final FacetHolder holder) {
        
        domainObjectIfAny
        .map(DomainObject::propertyDomainEvent)
        .filter(domainEvent -> domainEvent != PropertyDomainEvent.Default.class)
        .map(domainEvent -> new PropertyDomainEventDefaultFacetForDomainObjectAnnotation(
                holder, domainEvent))
        .ifPresent(FacetUtil::addFacet);
    }

    private void processDomainEventCollection(final Optional<DomainObject> domainObjectIfAny, final FacetHolder holder) {
        
        domainObjectIfAny
        .map(DomainObject::collectionDomainEvent)
        .filter(domainEvent -> domainEvent != CollectionDomainEvent.Default.class)
        .map(domainEvent -> new CollectionDomainEventDefaultFacetForDomainObjectAnnotation(
                holder, domainEvent))
        .ifPresent(FacetUtil::addFacet);
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {

        programmingModel.addValidator(new MetaModelValidatorVisiting.Visitor() {
            @Override
            public boolean visit(final ObjectSpecification thisSpec, final MetaModelValidator validator) {

                validate(thisSpec, validator);
                return true;
            }

            private void validate(final ObjectSpecification thisSpec, final MetaModelValidator validator) {
                if(!thisSpec.isEntityOrViewModel()) {
                    return;
                }

                final Map<ObjectSpecId, ObjectSpecification> specById = _Maps.newHashMap();
                for (final ObjectSpecification otherSpec : getSpecificationLoader().snapshotSpecifications()) {

                    if(thisSpec == otherSpec) {
                        continue;
                    }

                    if(!otherSpec.isEntityOrViewModel()) {
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
                    validator.onFailure(
                            existingSpec,
                            existingSpec.getIdentifier(),
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
                    final boolean isResolvableBean = getServiceRegistry().isResolvableBean(repositoryClass);
                    if(!isResolvableBean) {
                        validator.onFailure(
                                thisSpec,
                                thisSpec.getIdentifier(),
                                "@DomainObject annotation on %s specifies unknown repository '%s'",
                                thisSpec.getFullIdentifier(), repositoryClass.getName());
                    }

                }
            }

        });

        programmingModel.addValidator(autoCompleteMethodInvalid);
        programmingModel.addValidator(mixinTypeValidator);
    }

    // //////////////////////////////////////

    private final Map<Class<?>, Optional<Method>> postConstructMethods = _Maps.newHashMap();

    @Override
    public Method postConstructMethodFor(final Object pojo) {
        return MethodFinderUtils.findAnnotatedMethod(pojo, PostConstruct.class, postConstructMethods);
    }


}
