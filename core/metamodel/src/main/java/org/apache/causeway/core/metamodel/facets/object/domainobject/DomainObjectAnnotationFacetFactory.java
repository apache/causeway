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
package org.apache.causeway.core.metamodel.facets.object.domainobject;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.applib.events.lifecycle.ObjectCreatedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectLoadedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectPersistedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectPersistingEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectRemovingEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectUpdatedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectUpdatingEvent;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.causeway.core.metamodel.facets.object.callbacks.CreatedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.callbacks.LoadedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.callbacks.PersistedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.callbacks.PersistingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.callbacks.RemovingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.callbacks.UpdatedLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.callbacks.UpdatingLifecycleEventFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.autocomplete.AutoCompleteFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.choices.ChoicesFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.domainevents.CollectionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.editing.EditingEnabledFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.editing.ImmutableFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.entitychangepublishing.EntityChangePublishingFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.domainobject.introspection.IntrospectionPolicyFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.mixin.MetaModelValidatorForMixinTypes;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacetForDomainObjectAnnotation;
import org.apache.causeway.core.metamodel.object.MmEventUtils;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DomainObjectAnnotationFacetFactory
extends FacetFactoryAbstract
implements
    MetaModelRefiner,
    ObjectTypeFacetFactory {

    private final MetaModelValidatorForMixinTypes mixinTypeValidator =
            new MetaModelValidatorForMixinTypes("@DomainObject#nature=MIXIN");

    @Inject
    public DomainObjectAnnotationFacetFactory(
            final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessObjectTypeContext processClassContext) {

        var domainObjectIfAny = processClassContext.synthesizeOnType(DomainObject.class);
        processAliased(domainObjectIfAny, processClassContext);
        processIntrospecion(domainObjectIfAny, processClassContext);

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

        if(domainObjectIfAny.isEmpty()) {
            return;
        }

        var domainObject = domainObjectIfAny.get();

        var facetHolder = processClassContext.getFacetHolder();
        var cls = processClassContext.getCls();

        if(cls.isInterface()) {
            ValidationFailure.raiseFormatted(facetHolder,
                    "Cannot use @DomainObject on interface: %s", cls.getName());
        }

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
                && processClassContext.synthesizeOnType(Named.class).isPresent()) {
            // just a console warning, not decided yet whether we should be strict and fail MM validation
            log.warn("Mixins don't need a logicalTypeName, as was declared with {}.", cls.getName());
        }
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        var domainObjectIfAny = processClassContext.synthesizeOnType(DomainObject.class);

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
        //var cls = processClassContext.getCls();
        var facetHolder = processClassContext.getFacetHolder();

        // check for @DomainObject(entityChangePublishing=....)
        var entityChangePublishing = domainObjectIfAny
                .map(DomainObject::entityChangePublishing);
        addFacetIfPresent(
                EntityChangePublishingFacetForDomainObjectAnnotation
                .create(entityChangePublishing, getConfiguration(), facetHolder));
    }

    // -- AUTO COMPLETE

    void processAutoComplete(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessClassContext processClassContext) {
        var cls = processClassContext.getCls();
        var facetHolder = processClassContext.getFacetHolder();

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
                .ifPresent(super::addFacet);

    }

    private static final class AnnotHelper {
        AnnotHelper(final DomainObject domainObject) {
            this.autoCompleteRepository = domainObject.autoCompleteRepository();
            this.autoCompleteMethod = domainObject.autoCompleteMethod();
        }
        final Class<?> autoCompleteRepository;
        final String autoCompleteMethod;
        ResolvedMethod repositoryMethod;
    }

    private ResolvedMethod findRepositoryMethod(
            final FacetHolder facetHolder,
            final Class<?> cls,
            final String annotationName,
            final Class<?> repositoryClass,
            final String methodName) {

        var repoMethod = getClassCache()
            .streamPublicMethods(repositoryClass)
            .filter(method->method.name().equals(methodName))
            .filter(method->method.isSingleArg())
            .filter(method->method.paramType(0).equals(String.class))
            .findFirst()
            .orElse(null);

        if(repoMethod!=null) {
            return repoMethod;
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
        var facetHolder = processClassContext.getFacetHolder();
        FacetUtil.addFacetIfPresent(
                ChoicesFacetForDomainObjectAnnotation
                .create(domainObjectIfAny, facetHolder));
    }

    // check from @DomainObject(editing=...)
    void processEditing(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessClassContext processClassContext) {
        var facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacetIfPresent(
                EditingEnabledFacetForDomainObjectAnnotation
                .create(domainObjectIfAny, facetHolder));

        FacetUtil.addFacetIfPresent(
                ImmutableFacetForDomainObjectAnnotation
                .create(domainObjectIfAny, getConfiguration(), facetHolder));
    }

    // check from @DomainObject(aliased=...)
    void processAliased(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessObjectTypeContext processClassContext) {

        var cls = processClassContext.getCls();
        var facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacetIfPresent(
                AliasedFacetForDomainObjectAnnotation
                .create(domainObjectIfAny, cls, facetHolder));
    }

    // check from @DomainObject(introspection=...)
    void processIntrospecion(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessObjectTypeContext processClassContext) {

        var cls = processClassContext.getCls();
        var facetHolder = processClassContext.getFacetHolder();

        FacetUtil.addFacetIfPresent(
                IntrospectionPolicyFacetForDomainObjectAnnotation
                .create(domainObjectIfAny, cls, facetHolder));
    }

    void processNature(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessClassContext processClassContext) {
        var cls = processClassContext.getCls();
        var facetHolder = processClassContext.getFacetHolder();

        if(!domainObjectIfAny.isPresent()) {
            return;
        }

        // handle with least priority
        if(addFacetIfPresent(
                ViewModelFacetForDomainObjectAnnotation
                .create(
                        domainObjectIfAny,
                        facetHolder))
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

        var mixinDomainObjectIfAny =
                domainObjectIfAny
                .filter(domainObject -> domainObject.nature() == Nature.MIXIN)
                .filter(domainObject -> mixinTypeValidator.ensureMixinType(facetHolder, cls));

        addFacetIfPresent(
                MixinFacetForDomainObjectAnnotation
                .create(mixinDomainObjectIfAny, cls, facetHolder, mixinTypeValidator));

    }

    private void processLifecycleEvents(
            final Optional<DomainObject> domainObjectIfAny,
            final ProcessClassContext processClassContext) {

        if(!domainObjectIfAny.isPresent()) {
            return;
        }
        var facetHolder = processClassContext.getFacetHolder();

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
        var facetHolder = processClassContext.getFacetHolder();

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
        MmEventUtils.eventTypeIsPostable(
                lifecycleEvent,
                ObjectCreatedEvent.Noop.class,
                ObjectCreatedEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getCreatedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new CreatedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacet);
    }

    private void processLifecycleEventLoaded(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::loadedLifecycleEvent)
        .filter(lifecycleEvent ->
        MmEventUtils.eventTypeIsPostable(
                lifecycleEvent,
                ObjectLoadedEvent.Noop.class,
                ObjectLoadedEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getLoadedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new LoadedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacet);
    }

    private void processLifecycleEventPersisting(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::persistingLifecycleEvent)
        .filter(lifecycleEvent ->
        MmEventUtils.eventTypeIsPostable(
                lifecycleEvent,
                ObjectPersistingEvent.Noop.class,
                ObjectPersistingEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getPersistingLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new PersistingLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacet);
    }

    private void processLifecycleEventPersisted(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::persistedLifecycleEvent)
        .filter(lifecycleEvent ->
        MmEventUtils.eventTypeIsPostable(
                lifecycleEvent,
                ObjectPersistedEvent.Noop.class,
                ObjectPersistedEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getPersistedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new PersistedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacet);
    }

    private void processLifecycleEventRemoving(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::removingLifecycleEvent)
        .filter(lifecycleEvent ->
        MmEventUtils.eventTypeIsPostable(
                lifecycleEvent,
                ObjectRemovingEvent.Noop.class,
                ObjectRemovingEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getRemovingLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new RemovingLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacet);
    }

    private void processLifecycleEventUpdated(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::updatedLifecycleEvent)
        .filter(lifecycleEvent ->
        MmEventUtils.eventTypeIsPostable(
                lifecycleEvent,
                ObjectUpdatedEvent.Noop.class,
                ObjectUpdatedEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getUpdatedLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new UpdatedLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacet);
    }

    private void processLifecycleEventUpdating(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::updatingLifecycleEvent)
        .filter(lifecycleEvent ->
        MmEventUtils.eventTypeIsPostable(
                lifecycleEvent,
                ObjectUpdatingEvent.Noop.class,
                ObjectUpdatingEvent.Default.class,
                getConfiguration().getApplib().getAnnotation().getDomainObject().getUpdatingLifecycleEvent().isPostForDefault())
                )
        .map(lifecycleEvent -> new UpdatingLifecycleEventFacetForDomainObjectAnnotation(
                holder, lifecycleEvent))
        .ifPresent(super::addFacet);
    }

    private void processDomainEventAction(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::actionDomainEvent)
        .flatMap(domainEvent -> ActionDomainEventDefaultFacetForDomainObjectAnnotation
                .create(domainEvent, holder))
        .ifPresent(super::addFacet);

    }

    private void processDomainEventProperty(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::propertyDomainEvent)
        .flatMap(domainEvent -> PropertyDomainEventDefaultFacetForDomainObjectAnnotation
                .create(domainEvent, holder))
        .ifPresent(super::addFacet);
    }

    private void processDomainEventCollection(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

        domainObjectIfAny
        .map(DomainObject::collectionDomainEvent)
        .flatMap(domainEvent -> CollectionDomainEventDefaultFacetForDomainObjectAnnotation
                .create(domainEvent, holder))
        .ifPresent(super::addFacet);
    }

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {
        addValidatorToEnsureUniqueLogicalTypeNames(programmingModel);
    }

    private void addValidatorToEnsureUniqueLogicalTypeNames(final ProgrammingModel programmingModel) {

        programmingModel
        .addValidator(
            new MetaModelValidatorAbstract(getMetaModelContext(), spec->!spec.isAbstract()){

                final _Multimaps.ListMultimap<String, ObjectSpecification> specsByLogicalTypeName =
                        _Multimaps.newConcurrentListMultimap();

                @Override
                public void validateObjectEnter(final ObjectSpecification objSpec) {

                    // @DomainObject(logicalTypeName=...) must be unique among non-abstract types
                    // Eg. having an ApplicationUser interface and a concrete ApplicationUser (JDO)
                    // that have the same @DomainObject(logicalTypeName=...) should be allowed.
                    // A hard constraint that applies, is that there cannot be multiple bookmark-able
                    // types that share the same @DomainObject(logicalTypeName=...).
                    // This must be guaranteed by MM validation.
                    // - see also LogicalTypeResolver.register(...)

                    specsByLogicalTypeName.putElement(objSpec.getLogicalTypeName(), objSpec);

                    // also adding aliases to the multi-map
                    objSpec.getAliases()
                    .forEach(alias->
                        specsByLogicalTypeName.putElement(alias.getLogicalTypeName(), objSpec));
                }

                @Override
                public void validateExit() {

                    specsByLogicalTypeName.forEach((logicalTypeName, collidingSpecs)->{
                        if(isObjectTypeCollision(collidingSpecs)) {

                            // assuming, a check for proxies is only required when there is also a bean name collision
                            // where the plain class and the proxied class collide having the same logical-type-name
                            var proxies = proxiesIn(collidingSpecs);
                            if(proxies.isNotEmpty()) {

                                proxies.forEach(spec->{
                                    ValidationFailure.raiseFormatted(spec,
                                        ProgrammingModelConstants.MessageTemplate.PROXIED_SERVICE_BEAN_NOT_ALLOWED_TO_CONTRIBUTE
                                            .builder()
                                            .addVariable("logicalTypeName", spec.getLogicalTypeName())
                                            .addVariable("csv", asCsv(proxies.toList()))
                                            .buildMessage());
                                });

                            } else {

                                collidingSpecs.stream()
                                .filter(this::logicalTypeNameIsNotIncludedInAliased)
                                .forEach(spec->{
                                    ValidationFailure.raiseFormatted(spec,
                                        ProgrammingModelConstants.MessageTemplate.NON_UNIQUE_LOGICAL_TYPE_NAME_OR_ALIAS
                                            .builder()
                                            .addVariable("logicalTypeName", spec.getLogicalTypeName())
                                            .addVariable("csv", asCsv(collidingSpecs))
                                            .buildMessage());
                                });

                            }
                        }
                    });

                    // clean-up
                    specsByLogicalTypeName.clear();
                }

                private boolean isProxy(final @NonNull ObjectSpecification spec) {
                    var cls = spec.getCorrespondingClass();
                    return !ClassUtils.getUserClass(cls).equals(cls);
                }

                private Can<ObjectSpecification> proxiesIn(final @Nullable List<ObjectSpecification> specs) {
                    return stream(specs).filter(this::isProxy).collect(Can.toCan());
                }

                private boolean logicalTypeNameIsNotIncludedInAliased(final ObjectSpecification objectSpecification) {
                    if (getConfiguration().getCore().getMetaModel().getValidator().isAllowLogicalTypeNameAsAlias()) {
                        return objectSpecification.getAliases()
                                .map(LogicalType::getLogicalTypeName).stream()
                                .noneMatch(name -> objectSpecification.getLogicalTypeName().equals(name));
                    }
                    return true;
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

            });
    }

}
