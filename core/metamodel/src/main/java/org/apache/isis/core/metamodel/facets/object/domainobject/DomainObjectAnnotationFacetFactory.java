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
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Value;
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
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet.Precedence;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.PostConstructMethodCache;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemovingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdatingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.choices.ChoicesFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.CollectionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.EditingEnabledFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.ImmutableFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.entitychangepublishing.EntityChangePublishingFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.objectspecid.LogicalTypeFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.recreatable.RecreatableObjectFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.mixin.MetaModelValidatorForMixinTypes;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.methods.MethodByClassMap;
import org.apache.isis.core.metamodel.methods.MethodFinderUtils;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelVisitingValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.isis.core.metamodel.util.EventUtil;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DomainObjectAnnotationFacetFactory
extends FacetFactoryAbstract
implements
    MetaModelRefiner,
    PostConstructMethodCache,
    ObjectTypeFacetFactory {

    private final MetaModelValidatorForMixinTypes mixinTypeValidator =
            new MetaModelValidatorForMixinTypes("@DomainObject#nature=MIXIN");

    @Inject
    public DomainObjectAnnotationFacetFactory(
            final MetaModelContext mmc,
            final MethodByClassMap postConstructMethodsCache) {
        super(mmc, FeatureType.OBJECTS_ONLY);
        this.postConstructMethodsCache = postConstructMethodsCache;
    }

    @Override
    public void process(final ProcessObjectTypeContext processClassContext) {

        val domainObjectIfAny = processClassContext.synthesizeOnType(DomainObject.class);
        processLogicalTypeName(domainObjectIfAny, processClassContext);

        // conflicting type semantics validation ...
        validateConflictingTypeSemantics(domainObjectIfAny, processClassContext);
    }

    /*
     * on the fly validation ...
     * yet only considers annotations and falls short on other means
     * (eg. ValueTypeRegistry, configuration, ...)
     * TODO instead properly validate by implementing a validator that looks into the facets that are created
     */
    private void validateConflictingTypeSemantics(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessObjectTypeContext processClassContext) {

        if(!domainObjectIfAny.isPresent()) {
            return;
        }

        val domainObject = domainObjectIfAny.get();

        val facetHolder = processClassContext.getFacetHolder();
        val cls = processClassContext.getCls();

        if(processClassContext.synthesizeOnType(Value.class).isPresent()) {
            ValidationFailure.raiseFormatted(facetHolder,
                    "Cannot use @DomainObject and @Value on the same type: %s", cls.getName());
        }

        if(!domainObject.nature().isMixin()) {
            if(processClassContext.synthesizeOnType(Action.class).isPresent()) {
                ValidationFailure.raiseFormatted(facetHolder,
                        "Cannot use @DomainObject and @Action on the same type, "
                        + "unless nature is MIXIN: %s", cls.getName());
            }
            if(processClassContext.synthesizeOnType(Property.class).isPresent()) {
                ValidationFailure.raiseFormatted(facetHolder,
                        "Cannot use @DomainObject and @Property on the same type, "
                        + "unless nature is MIXIN: %s", cls.getName());
            }
            if(processClassContext.synthesizeOnType(Collection.class).isPresent()) {
                ValidationFailure.raiseFormatted(facetHolder,
                        "Cannot use @DomainObject and @Collection on the same type, "
                        + "unless nature is MIXIN: %s", cls.getName());
            }
        }

        if(domainObject.nature().isMixin()
                && _Strings.isNotEmpty(domainObject.logicalTypeName())) {
            // just a console warning, not decided yet whether we should be strict and fail MM validation
            log.warn("Mixins don't need a logicalTypeName, as was declared with {}.", cls.getName());
        }
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        val domainObjectIfAny = processClassContext.synthesizeOnType(DomainObject.class);

        processEntityChangePublishing(domainObjectIfAny, processClassContext);
        processAutoComplete(domainObjectIfAny, processClassContext);
        processBounded(domainObjectIfAny, processClassContext);
        processEditing(domainObjectIfAny, processClassContext);
        processNature(domainObjectIfAny, processClassContext);
        processLifecycleEvents(domainObjectIfAny, processClassContext);
        processDomainEvents(domainObjectIfAny, processClassContext);
    }


    void processEntityChangePublishing(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        //
        // this rule originally implemented only in AuditableFacetFromConfigurationFactory
        // but think should apply in general
        //
        if(HasInteractionId.class.isAssignableFrom(cls)) {
            // do not install on any implementation of HasInteractionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @DomainObject(entityChangePublishing=....)
        val entityChangePublishing = domainObjectIfAny
                .map(DomainObject::entityChangePublishing);
        addFacetIfPresent(
                EntityChangePublishingFacetForDomainObjectAnnotation
                .create(entityChangePublishing, getConfiguration(), facetHolder));
    }

    // -- AUTO COMPLETE

    void processAutoComplete(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        // check from @DomainObject(autoCompleteRepository=...)
        processClassContext.synthesizeOnType(DomainObject.class)
                .map(AnnotHelper::new)
                .filter(a -> a.autoCompleteRepository != Object.class)
                .filter(a -> {
                    a.repositoryMethod = findRepositoryMethod(
                            facetHolder,
                            cls,
                            "@DomainObject",
                            a.autoCompleteRepository,
                            a.autoCompleteMethod);

                    return a.repositoryMethod != null;
                })
                .map(a -> new AutoCompleteFacetForDomainObjectAnnotation(
                        facetHolder, a.autoCompleteRepository, a.repositoryMethod))
                .ifPresent(super::addFacetIfPresent);

    }

    private static final class AnnotHelper {
        AnnotHelper(final DomainObject domainObject) {
            this.autoCompleteRepository = domainObject.autoCompleteRepository();
            this.autoCompleteMethod = domainObject.autoCompleteMethod();
        }
        final Class<?> autoCompleteRepository;
        final String autoCompleteMethod;
        Method repositoryMethod;
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
        ValidationFailure.raise(
                facetHolder.getSpecificationLoader(),
                Identifier.classIdentifier(LogicalType.fqcn(cls)),
                String.format(
                        "%s annotation on %s specifies method '%s' that does not exist in repository '%s'",
                        annotationName,
                        cls.getName(),
                        methodName,
                        repositoryClass.getName())
                );
        return null;
    }

    // -- BOUNDED

    // check from @DomainObject(bounded=...)
    void processBounded(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessClassContext processClassContext) {
        val facetHolder = processClassContext.getFacetHolder();
        FacetUtil.addFacetIfPresent(
                ChoicesFacetForDomainObjectAnnotation
                .create(domainObjectIfAny, facetHolder));
    }

    // check from @DomainObject(editing=...)
    void processEditing(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessClassContext processClassContext) {
        val facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacetIfPresent(
                EditingEnabledFacetForDomainObjectAnnotation
                .create(domainObjectIfAny, facetHolder));

        FacetUtil.addFacetIfPresent(
                ImmutableFacetForDomainObjectAnnotation
                .create(domainObjectIfAny, getConfiguration(), facetHolder));
    }

    // check from @DomainObject(logicalTypeName=...)
    void processLogicalTypeName(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessObjectTypeContext processClassContext) {

        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacetIfPresent(
                LogicalTypeFacetForDomainObjectAnnotation
                .create(domainObjectIfAny, cls, facetHolder));
    }


    void processNature(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        if(!domainObjectIfAny.isPresent()) {
            return;
        }

        val postConstructMethodCache = this;

        // handle with least priority
        if(addFacetIfPresent(
                RecreatableObjectFacetForDomainObjectAnnotation
                .create(
                        domainObjectIfAny,
                        facetHolder,
                        postConstructMethodCache,
                        Precedence.LOW)
                )
                .isPresent()) {
            return;
        }

        if(cls.isInterface()
                || Modifier.isAbstract(cls.getModifiers())) {

            // entirely ignore abstract types
            // there is no reason for these to be recognized as mixins,
            // as only concrete mixins will ever contribute to the domain
            return;
        }

        val mixinDomainObjectIfAny =
                domainObjectIfAny
                .filter(domainObject -> domainObject.nature() == Nature.MIXIN)
                .filter(domainObject -> mixinTypeValidator.ensureMixinType(facetHolder, cls));

        addFacetIfPresent(
                MixinFacetForDomainObjectAnnotation
                .create(mixinDomainObjectIfAny, cls, facetHolder, getServiceInjector(), mixinTypeValidator));

    }


    private void processLifecycleEvents(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessClassContext processClassContext) {

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


    private void processDomainEvents(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessClassContext processClassContext) {
        if(!domainObjectIfAny.isPresent()) {
            return;
        }
        val facetHolder = processClassContext.getFacetHolder();

        processDomainEventAction(domainObjectIfAny, facetHolder);
        processDomainEventProperty(domainObjectIfAny, facetHolder);
        processDomainEventCollection(domainObjectIfAny, facetHolder);
    }

    private void processLifecycleEventCreated(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::createdLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectCreatedEvent.Noop.class,
                ObjectCreatedEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getCreatedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new CreatedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacetIfPresent);
    }

    private void processLifecycleEventLoaded(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::loadedLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectLoadedEvent.Noop.class,
                ObjectLoadedEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getLoadedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new LoadedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacetIfPresent);
    }

    private void processLifecycleEventPersisting(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::persistingLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectPersistingEvent.Noop.class,
                ObjectPersistingEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getPersistingLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new PersistingLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacetIfPresent);
    }

    private void processLifecycleEventPersisted(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::persistedLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectPersistedEvent.Noop.class,
                ObjectPersistedEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getPersistedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new PersistedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacetIfPresent);
    }

    private void processLifecycleEventRemoving(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::removingLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectRemovingEvent.Noop.class,
                ObjectRemovingEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getRemovingLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new RemovingLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacetIfPresent);
    }

    private void processLifecycleEventUpdated(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::updatedLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectUpdatedEvent.Noop.class,
                ObjectUpdatedEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getUpdatedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new UpdatedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacetIfPresent);
    }

    private void processLifecycleEventUpdating(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::updatingLifecycleEvent)
        .filter(lifecycleEvent ->
        EventUtil.eventTypeIsPostable(
                lifecycleEvent,
                ObjectUpdatingEvent.Noop.class,
                ObjectUpdatingEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getUpdatingLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new UpdatingLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacetIfPresent);
    }

    private void processDomainEventAction(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::actionDomainEvent)
        .filter(domainEvent -> domainEvent != ActionDomainEvent.Default.class)
        .map(domainEvent -> new ActionDomainEventDefaultFacetForDomainObjectAnnotation(
                holder, domainEvent))
        .ifPresent(super::addFacetIfPresent);

    }

    private void processDomainEventProperty(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::propertyDomainEvent)
        .filter(domainEvent -> domainEvent != PropertyDomainEvent.Default.class)
        .map(domainEvent -> new PropertyDomainEventDefaultFacetForDomainObjectAnnotation(
                holder, domainEvent))
        .ifPresent(super::addFacetIfPresent);
    }

    private void processDomainEventCollection(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::collectionDomainEvent)
        .filter(domainEvent -> domainEvent != CollectionDomainEvent.Default.class)
        .map(domainEvent -> new CollectionDomainEventDefaultFacetForDomainObjectAnnotation(
                holder, domainEvent))
        .ifPresent(super::addFacetIfPresent);
    }

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {
        addValidatorToEnsureUniqueLogicalTypeNames(programmingModel);
    }

    private void addValidatorToEnsureUniqueLogicalTypeNames(final ProgrammingModel pm) {

        final _Multimaps.ListMultimap<String, ObjectSpecification> collidingSpecsByLogicalTypeName =
                _Multimaps.newConcurrentListMultimap();

        final MetaModelVisitingValidatorAbstract ensureUniqueObjectIds =
                new MetaModelVisitingValidatorAbstract(){

                    @Override
                    public void validate(final ObjectSpecification objSpec) {

                        // @DomainObject(logicalTypeName=...) must be unique among non-abstract types
                        // Eg. having an ApplicationUser interface and a concrete ApplicationUser (JDO)
                        // that have the same @DomainObject(logicalTypeName=...) should be allowed.
                        // A hard constraint that applies, is that there cannot be multiple bookmark-able
                        // types that share the same @DomainObject(logicalTypeName=...).
                        // This must be guaranteed by MM validation.
                        // - see also LogicalTypeResolver.register(...)

                        if(objSpec.isAbstract()) {
                            return;
                        }
                        collidingSpecsByLogicalTypeName.putElement(objSpec.getLogicalTypeName(), objSpec);
                    }

                    @Override
                    public void summarize() {
                        for (val logicalTypeName : collidingSpecsByLogicalTypeName.keySet()) {
                            val collidingSpecs = collidingSpecsByLogicalTypeName.get(logicalTypeName);
                            if(isObjectTypeCollision(collidingSpecs)) {
                                val csv = asCsv(collidingSpecs);

                                collidingSpecs.forEach(spec->{
                                    ValidationFailure.raiseFormatted(
                                            spec,
                                            "Logical type name '%s' mapped to multiple non-abstract classes:\n"
                                            + "%s",
                                            logicalTypeName,
                                            csv);
                                });


                            }
                        }
                        // so can be revalidated again if necessary.
                        collidingSpecsByLogicalTypeName.clear();
                    }

                    private boolean isObjectTypeCollision(final List<ObjectSpecification> specs) {
                        return specs.size()>1;
                    }

                    private String asCsv(final List<ObjectSpecification> specList) {
                        return stream(specList)
                                .map(ObjectSpecification::getFullIdentifier)
                                .sorted()
                                .collect(Collectors.joining(", "));
                    }

                };

        pm.addValidator(ensureUniqueObjectIds);
    }


    // //////////////////////////////////////

    private final @NonNull MethodByClassMap postConstructMethodsCache;

    @Override
    public Method postConstructMethodFor(final Object pojo) {
        return MethodFinderUtils.findAnnotatedMethod(pojo, PostConstruct.class, postConstructMethodsCache);
    }


}
