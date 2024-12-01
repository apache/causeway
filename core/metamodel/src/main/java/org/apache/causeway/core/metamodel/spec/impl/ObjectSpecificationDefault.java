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
package org.apache.causeway.core.metamodel.spec.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Oneshot;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.collections._Streams;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.help.HelpFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaLayersProvider;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.causeway.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.causeway.core.metamodel.facets.object.introspection.IntrospectionPolicyFacet;
import org.apache.causeway.core.metamodel.facets.object.logicaltype.AliasedFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet.Contributing;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacetAbstract;
import org.apache.causeway.core.metamodel.facets.object.navparent.NavigableParentFacet;
import org.apache.causeway.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionContext;
import org.apache.causeway.core.metamodel.interactions.InteractionUtils;
import org.apache.causeway.core.metamodel.interactions.ObjectTitleContext;
import org.apache.causeway.core.metamodel.interactions.ObjectValidityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.causeway.core.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.causeway.core.metamodel.spi.EntityTitleSubscriber;
import org.apache.causeway.core.metamodel.util.Facets;

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class ObjectSpecificationDefault
implements ObjectMemberContainer, ObjectSpecificationMutable, HasSpecificationLoaderInternal {

    // -- CONSTRUCTION

    /**
     * Lazily built by {@link #getMember(Method)}.
     */
    private Map<ResolvedMethod, ObjectMember> membersByMethod = null;

    private final FacetedMethodsBuilder facetedMethodsBuilder;
    private final ClassSubstitutorRegistry classSubstitutorRegistry;

    @Getter
    private final IntrospectionPolicy introspectionPolicy;

    public ObjectSpecificationDefault(
            final CausewayBeanMetaData typeMeta,
            final MetaModelContext mmc,
            final FacetProcessor facetProcessor,
            final PostProcessor postProcessor,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {

        this.correspondingClass = typeMeta.getCorrespondingClass();
        this.logicalType = typeMeta.logicalType();
        this.fullName = correspondingClass.getName();
        this.shortName = typeMeta.logicalType().getLogicalTypeSimpleName();
        this.beanSort = typeMeta.beanSort();

        this.facetHolder = FacetHolder.simple(
            facetProcessor.getMetaModelContext(),
            Identifier.classIdentifier(logicalType));

        this.facetProcessor = facetProcessor;
        this.postProcessor = postProcessor;

        this.isVetoedForInjection = switch (typeMeta.managedBy()) {
            case NONE, CAUSEWAY, PERSISTENCE -> true;
            case UNSPECIFIED, SPRING  -> false;
        };
        this.classSubstitutorRegistry = classSubstitutorRegistry;

        // must install EncapsulationFacet (if any) and MemberAnnotationPolicyFacet (if any)
        facetProcessor.processObjectType(typeMeta.getCorrespondingClass(), this);

        // naturally supports attribute inheritance from the type's hierarchy
        final IntrospectionPolicy introspectionPolicy =
                this.lookupFacet(IntrospectionPolicyFacet.class)
                .map(introspectionPolicyFacet->
                        introspectionPolicyFacet
                        .getIntrospectionPolicy(mmc.getConfiguration()))
                .orElseGet(()->mmc.getConfiguration().getCore().getMetaModel().getIntrospector().getPolicy());

        this.introspectionPolicy = introspectionPolicy;

        this.facetedMethodsBuilder =
                new FacetedMethodsBuilder(this, facetProcessor, classSubstitutorRegistry);
    }

    // -- CONTRACT

    @Override
    public int hashCode() {
        return correspondingClass.hashCode();
    }
    @Override
    public boolean equals(final Object o) {
        return (o instanceof ObjectSpecification other)
            ? Objects.equals(this.correspondingClass, other.getCorrespondingClass())
            : false;
    }
    @Override
    public String toString() {
        return "ObjSpec[class=%s, sort=%s, super=%s]"
            .formatted(getFullIdentifier(), getBeanSort().name(), superclass() == null
                ? "Object"
                : superclass().getFullIdentifier());
    }

    protected void introspectTypeHierarchy() {

        facetedMethodsBuilder.introspectClass();

        // name
        addNamedFacetIfRequired();

        // go no further if a value
        if(this.isValue()) {
            if (log.isDebugEnabled()) {
                log.debug("skipping type hierarchy introspection for value type {}", getFullIdentifier());
            }
            return;
        }

        // superclass
        final Class<?> superclass = getCorrespondingClass().getSuperclass();
        loadSpecOfSuperclass(superclass);

        // walk superinterfaces

        //
        // REVIEW: the processing here isn't quite the same as with
        // superclasses, in that with superclasses the superclass adds this type as its
        // subclass, whereas here this type defines itself as the subtype.
        //
        // it'd be nice to push the responsibility for adding subclasses to
        // the interface type... needs some tests around it, though, before
        // making that refactoring.
        //
        final Class<?>[] interfaceTypes = getCorrespondingClass().getInterfaces();
        final List<ObjectSpecification> interfaceSpecList = _Lists.newArrayList();
        for (var interfaceType : interfaceTypes) {
            var interfaceSubstitute = classSubstitutorRegistry.getSubstitution(interfaceType);
            if (interfaceSubstitute.isReplace()) {
                var interfaceSpec = specLoaderInternal().loadSpecification(interfaceSubstitute.getReplacement());
                interfaceSpecList.add(interfaceSpec);
            }
        }

        updateAsSubclassTo(interfaceSpecList);
        updateInterfaces(interfaceSpecList);
    }

    protected void introspectMembers() {

        // yet this logic does not skip UNKNONW
        if(this.getBeanSort().isCollection()
                || this.getBeanSort().isVetoed()
                || this.isValue()) {
            if (log.isDebugEnabled()) {
                log.debug("skipping full introspection for {} type {}", this.getBeanSort(), getFullIdentifier());
            }
            return;
        }

        // create associations and actions
        replaceAssociations(createAssociations());
        replaceActions(createActions());

        postProcess();
    }

    private void addNamedFacetIfRequired() {
        if (getFacet(MemberNamedFacet.class) == null) {
            addFacet(new MemberNamedFacetForStaticMemberName(
                    _Strings.asNaturalName.apply(getShortIdentifier()),
                    this));
        }
    }

    // -- create associations and actions
    private Stream<ObjectAssociation> createAssociations() {
        return facetedMethodsBuilder.getAssociationFacetedMethods()
                .stream()
                .map(this::createAssociation)
                .filter(_NullSafe::isPresent);
    }

    private ObjectAssociation createAssociation(final FacetedMethod facetMethod) {
        if (facetMethod.getFeatureType().isCollection()) {
            return OneToManyAssociationDefault.forMethod(facetMethod);
        } else if (facetMethod.getFeatureType().isProperty()) {
            return OneToOneAssociationDefault.forMethod(facetMethod);
        } else {
            return null;
        }
    }

    private Stream<ObjectAction> createActions() {
        return facetedMethodsBuilder.getActionFacetedMethods()
                .stream()
                .map(this::createAction)
                .filter(_NullSafe::isPresent);
    }

    private ObjectAction createAction(final FacetedMethod facetedMethod) {
        if (facetedMethod.getFeatureType().isAction()) {
            /* Assuming, that facetedMethod was already populated with ContributingFacet,
             * we copy the mixin-sort information from the FacetedMethod to the MixinFacet
             * that is held by the mixin's type spec. */
            mixinFacet()
            .flatMap(mixinFacet->_Casts.castTo(MixinFacetAbstract.class, mixinFacet))
            .ifPresent(mixinFacetAbstract->
                mixinFacetAbstract.initMixinSortFrom(facetedMethod));

            return this.isMixin()
                    ? ObjectActionDefault.forMixinMain(facetedMethod)
                    : ObjectActionDefault.forMethod(facetedMethod);
        } else {
            return null;
        }
    }

    // -- getObjectAction

    @Override
    public Optional<ObjectAction> getDeclaredAction(
            final @Nullable String id,
            final ImmutableEnumSet<ActionScope> actionScopes,
            final MixedIn mixedIn) {

        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED);

        return _Strings.isEmpty(id)
            ? Optional.empty()
            : streamDeclaredActions(actionScopes, mixedIn)
                .filter(action->
                    id.equals(action.getFeatureIdentifier().getMemberNameAndParameterClassNamesIdentityString())
                            || id.equals(action.getFeatureIdentifier().getMemberLogicalName())
                )
                .findFirst();
    }

    @Override
    public Optional<? extends ObjectMember> getMember(final ResolvedMethod method) {
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED);

        if (membersByMethod == null) {
            this.membersByMethod = catalogueMembers();
        }

        var member = membersByMethod.get(method);
        return Optional.ofNullable(member);
    }

    private Map<ResolvedMethod, ObjectMember> catalogueMembers() {
        var membersByMethod = _Maps.<ResolvedMethod, ObjectMember>newHashMap();
        cataloguePropertiesAndCollections(membersByMethod::put);
        catalogueActions(membersByMethod::put);
        return membersByMethod;
    }

    private void cataloguePropertiesAndCollections(final BiConsumer<ResolvedMethod, ObjectMember> onMember) {
        streamDeclaredAssociations(MixedIn.EXCLUDED)
        .forEach(field->
            field.streamFacets(ImperativeFacet.class)
                .map(ImperativeFacet::getMethods)
                .flatMap(Can::stream)
                .map(MethodFacade::asMethodElseFail) // expected regular
                .peek(method->_Reflect.guardAgainstSynthetic(method.method())) // expected non-synthetic
                .forEach(imperativeFacetMethod->onMember.accept(imperativeFacetMethod, field)));
    }

    private void catalogueActions(final BiConsumer<ResolvedMethod, ObjectMember> onMember) {
        streamDeclaredActions(MixedIn.INCLUDED)
        .forEach(userAction->
            userAction.streamFacets(ImperativeFacet.class)
                .map(ImperativeFacet::getMethods)
                .flatMap(Can::stream)
                .map(MethodFacade::asMethodForIntrospection)
                .peek(method->_Reflect.guardAgainstSynthetic(method.method())) // expected non-synthetic
                .forEach(imperativeFacetMethod->
                    onMember.accept(imperativeFacetMethod, userAction)));
    }

    // -- ELEMENT SPECIFICATION

    private final _Lazy<Optional<ObjectSpecification>> elementSpecification =
            _Lazy.threadSafe(()->lookupFacet(TypeOfFacet.class)
                    .map(typeOfFacet -> typeOfFacet.elementSpec()));

    @Override
    public Optional<ObjectSpecification> getElementSpecification() {
        return elementSpecification.get();
    }

    // -- TABLE COLUMN RENDERING

    @Override
    public final Stream<ObjectAssociation> streamAssociationsForColumnRendering(
            final Identifier memberIdentifier,
            final ManagedObject parentObject) {

        return new _MembersAsColumns(getMetaModelContext())
            .streamAssociationsForColumnRendering(this, memberIdentifier, parentObject);
    }

    @Override
    public Stream<ObjectAction> streamActionsForColumnRendering(
            final Identifier memberIdentifier) {
        return new _MembersAsColumns(getMetaModelContext())
                .streamActionsForColumnRendering(this, memberIdentifier);
    }

    // -- DETERMINE INJECTABILITY

    private boolean isVetoedForInjection;

    private _Lazy<Boolean> isInjectableLazy = _Lazy.threadSafe(()->
        !isVetoedForInjection
                && !getBeanSort().isAbstract()
                && !getBeanSort().isValue()
                && !getBeanSort().isEntity()
                && !getBeanSort().isViewModel()
                && !getBeanSort().isMixin()
                && (getBeanSort().isManagedBeanAny()
                        || getServiceRegistry()
                                .lookupRegisteredBeanById(getLogicalType())
                                .isPresent())
                );

    @Override
    public boolean isInjectable() {
        return isInjectableLazy.get();
    }

    private _Lazy<Boolean> isDomainServiceLazy = _Lazy.threadSafe(()->
        _ClassCache.getInstance().head(getCorrespondingClass()).hasAnnotation(DomainService.class));

    @Override
    public boolean isDomainService() {
        return isDomainServiceLazy.get();
    }

    //-----------------------------------------------------------------------------------------------------------------
    // MERGED FROM FORMER ObjectSpecificationAbstract
    //-----------------------------------------------------------------------------------------------------------------

    /**
     * @implNote thread-safe
     */
    private static class Subclasses {

        // List performs better compared to a Set, when the number of elements is low
        private Can<ObjectSpecification> classes = Can.empty();

        public void addSubclass(final ObjectSpecification subclass) {
            synchronized(classes) {
                classes = classes.addUnique(subclass);
            }
        }

        public boolean hasSubclasses() {
            synchronized(classes) {
                return classes.isNotEmpty();
            }
        }

        public Can<ObjectSpecification> snapshot() {
            synchronized(classes) {
                return classes;
            }
        }
    }

    // -- FIELDS

    private final PostProcessor postProcessor;
    private final FacetProcessor facetProcessor;

    @Getter private final BeanSort beanSort;

    // -- ASSOCIATIONS

    private final List<ObjectAssociation> associations = _Lists.newArrayList();

    // defensive immutable lazy copy of associations
    private final _Lazy<Can<ObjectAssociation>> unmodifiableAssociations =
            _Lazy.threadSafe(()->Can.ofCollection(associations));

    // -- ACTIONS

    private final List<ObjectAction> objectActions = _Lists.newArrayList();

    /** not API, used for validation */
    @Getter private final Set<ResolvedMethod> potentialOrphans = _Sets.newHashSet();

    // defensive immutable lazy copy of objectActions
    private final _Lazy<Can<ObjectAction>> unmodifiableActions =
            _Lazy.threadSafe(()->Can.ofCollection(objectActions));

    // partitions and caches objectActions by type; updated in sortCacheAndUpdateActions()
    private final ListMultimap<ActionScope, ObjectAction> objectActionsByType =
            _Multimaps.newConcurrentListMultimap();

    // -- INTERFACES

    private final List<ObjectSpecification> interfaces = _Lists.newArrayList();

    // defensive immutable lazy copy of interfaces
    private final _Lazy<Can<ObjectSpecification>> unmodifiableInterfaces =
            _Lazy.threadSafe(()->Can.ofCollection(interfaces));

    private final Subclasses directSubclasses = new Subclasses();
    // built lazily
    private Subclasses transitiveSubclasses;

    private final Class<?> correspondingClass;
    private final String fullName;
    private final String shortName;

    private final LogicalType logicalType;

    private ObjectSpecification superclassSpec;

    private ValueFacet valueFacet;
    private EntityFacet entityFacet;
    private ViewModelFacet viewmodelFacet;
    private MixinFacet mixinFacet;
    private TitleFacet titleFacet;
    private IconFacet iconFacet;
    private NavigableParentFacet navigableParentFacet;
    private AliasedFacet aliasedFacet;
    private CssClassFacet cssClassFacet;

    private IntrospectionState introspectionState = IntrospectionState.NOT_INTROSPECTED;

    @Getter(onMethod_ = {@Override}) private FacetHolder facetHolder;



    // -- Stuff immediately derivable from class
    @Override
    public final FeatureType getFeatureType() {
        return FeatureType.OBJECT;
    }

    @Override
    public final LogicalType getLogicalType() {
        return logicalType;
    }

    @Override
    public final Class<?> getCorrespondingClass() {
        return correspondingClass;
    }

    @Override
    public final String getShortIdentifier() {
        return shortName;
    }

    /**
     * The {@link Class#getName() (full) name} of the
     * {@link #getCorrespondingClass() class}.
     */
    @Override
    public final String getFullIdentifier() {
        return fullName;
    }

    @Override
    public void introspectUpTo(final IntrospectionState upTo) {

        if(!isLessThan(upTo)) {
            return; // optimization
        }

        if(log.isDebugEnabled()) {
            log.debug("introspectingUpTo: {}, {}", getFullIdentifier(), upTo);
        }

        boolean revalidate = false;

        switch (introspectionState) {
        case NOT_INTROSPECTED:
            if(isLessThan(upTo)) {
                introspectType();
            }
            if(isLessThan(upTo)) {
                introspectFully();
                revalidate = true;
            }
            // set to avoid infinite loops
            break;

        case TYPE_BEING_INTROSPECTED:
            // nothing to do (interim state during introspectType)
            break;

        case TYPE_INTROSPECTED:
            if(isLessThan(upTo)) {
                introspectFully();
                revalidate = true;
            }
            break;

        case MEMBERS_BEING_INTROSPECTED:
            // nothing to do (interim state during introspectully)
            break;

        case FULLY_INTROSPECTED:
            // nothing to do ... all done
            break;

        default:
            throw _Exceptions.unexpectedCodeReach();
        }

        if(revalidate) {
            getSpecificationLoader().validateLater(this);
        }
    }

    private void introspectType() {

        // set to avoid infinite loops
        this.introspectionState = IntrospectionState.TYPE_BEING_INTROSPECTED;
        introspectTypeHierarchy();
        invalidateCachedFacets();
        this.introspectionState = IntrospectionState.TYPE_INTROSPECTED;
    }

    private void introspectFully() {

        // set to avoid infinite loops
        this.introspectionState = IntrospectionState.MEMBERS_BEING_INTROSPECTED;
        introspectMembers();
        this.introspectionState = IntrospectionState.FULLY_INTROSPECTED;

        // make sure we've loaded the facets from layout.xml also.
        Facets.gridPreload(this, null);
    }

    boolean isLessThan(final IntrospectionState upTo) {
        return this.introspectionState.compareTo(upTo) < 0;
    }

    protected void loadSpecOfSuperclass(final Class<?> superclass) {
        if (superclass == null) {
            return;
        }
        superclassSpec = specLoaderInternal().loadSpecification(superclass);
        if (superclassSpec != null) {
            if (log.isDebugEnabled()) {
                log.debug("  Superclass {}", superclass.getName());
            }
            updateAsSubclassTo(superclassSpec);
        }
    }

    protected void updateInterfaces(final List<ObjectSpecification> interfaces) {
        synchronized(unmodifiableInterfaces) {
            this.interfaces.clear();
            this.interfaces.addAll(interfaces);
            unmodifiableInterfaces.clear();
        }
    }

    private void updateAsSubclassTo(final ObjectSpecification supertypeSpec) {
        // API
        var introspectableSpec = (ObjectSpecificationDefault) supertypeSpec;
        introspectableSpec.updateSubclasses(this);
    }

    protected void updateAsSubclassTo(final List<ObjectSpecification> supertypeSpecs) {
        for (final ObjectSpecification supertypeSpec : supertypeSpecs) {
            updateAsSubclassTo(supertypeSpec);
        }
    }

    private void updateSubclasses(final ObjectSpecification subclass) {
        this.directSubclasses.addSubclass(subclass);
    }

    protected final void replaceAssociations(final Stream<ObjectAssociation> associations) {
        var orderedAssociations = _MemberSortingUtils.sortAssociationsIntoList(associations);
        synchronized (unmodifiableAssociations) {
            this.associations.clear();
            this.associations.addAll(orderedAssociations);
            unmodifiableAssociations.clear(); // invalidate
        }
    }

    protected final void replaceActions(final Stream<ObjectAction> objectActions) {
        var orderedActions = _MemberSortingUtils.sortActionsIntoList(objectActions);
        synchronized (unmodifiableActions){
            this.objectActions.clear();
            this.objectActions.addAll(orderedActions);
            unmodifiableActions.clear(); // invalidate

            // rebuild objectActionsByType multi-map
            for (var actionType : ActionScope.values()) {
                var objectActionForType = objectActionsByType.getOrElseNew(actionType);
                objectActionForType.clear();
                orderedActions.stream()
                .filter(ObjectAction.Predicates.ofActionType(actionType))
                .forEach(objectActionForType::add);
            }
        }
    }

    public void invalidateCachedFacets() {
        valueFacet = getFacet(ValueFacet.class);
        titleFacet = lookupNonFallbackFacet(TitleFacet.class).orElse(null);
        iconFacet = getFacet(IconFacet.class);
        navigableParentFacet = getFacet(NavigableParentFacet.class);
        cssClassFacet = getFacet(CssClassFacet.class);
        aliasedFacet = getFacet(AliasedFacet.class);
    }

    protected void postProcess() {
        postProcessor.postProcess(this);
        invalidateCachedFacets();
    }

    @Override
    public final Optional<ValueFacet> valueFacet() {
        return Optional.ofNullable(valueFacet);
    }

    @Override
    public final Optional<MixinFacet> mixinFacet() {
        // deliberately don't memoize lookup misses, because could be too early
        if(mixinFacet==null) {
            mixinFacet = getFacet(MixinFacet.class);
        }
        return Optional.ofNullable(mixinFacet);
    }

    @Override
    public final Optional<EntityFacet> entityFacet() {
        // deliberately don't memoize lookup misses, because could be too early
        if(entityFacet==null) {
            entityFacet = getFacet(EntityFacet.class);
        }
        return Optional.ofNullable(entityFacet);
    }

    @Override
    public final Optional<ViewModelFacet> viewmodelFacet() {
        // deliberately don't memoize lookup misses, because could be too early
        if(viewmodelFacet==null) {
            viewmodelFacet = getFacet(ViewModelFacet.class);
        }
        return Optional.ofNullable(viewmodelFacet);
    }

    @Override
    public String getTitle(final TitleRenderRequest titleRenderRequest) {
        if (titleFacet != null) {
            var titleString = titleFacet.title(titleRenderRequest);
            if (!_Strings.isEmpty(titleString)) {
                notifySubscribersIfEntity(titleRenderRequest, titleString);
                return titleString;
            }
        }
        var prefix = this.isInjectable()
                ? ""
                : "Untitled ";
        return prefix + getSingularName();
    }

    private void notifySubscribersIfEntity(
            final TitleRenderRequest titleRenderRequest,
            final String titleString) {
        if (!isEntity()) {
            return;
        }
        var managedObject = titleRenderRequest.getObject();
        managedObject.getBookmark().ifPresent(bookmark -> {
            getTitleSubscribers().stream().forEach(x -> x.entityTitleIs(bookmark, titleString));
        });
    }

    @Override
    public String getIconName(final ManagedObject domainObject) {
        if(ManagedObjects.isSpecified(domainObject)) {
            _Assert.assertEquals(domainObject.getSpecification(), this);
        }
        return iconFacet != null
                ? iconFacet.iconName(domainObject)
                : null;
    }

    @Override
    public ObjectIcon getIcon(final ManagedObject domainObject) {
        var iconNameModifier = getIconName(domainObject);
        return getObjectIconService().getObjectIcon(this, iconNameModifier);
    }

    @Override
    public Object getNavigableParent(final Object object) {
        return navigableParentFacet != null
                ? navigableParentFacet.navigableParent(object)
                : null;
    }

    @Override
    public String getCssClass(final ManagedObject reference) {
        return cssClassFacet != null
                ? cssClassFacet.cssClass(reference)
                : null;
    }

    @Override
    public Optional<FontAwesomeLayers> getFaLayers(final ManagedObject reference){
        return lookupFacet(FaFacet.class)
                .map(FaFacet::getSpecialization)
                .map(either->either.fold(
                        faStaticFacet->(FaLayersProvider)faStaticFacet,
                        faImperativeFacet->faImperativeFacet.getFaLayersProvider(reference)))
                .map(FaLayersProvider::getLayers);
    }

    @Override
    public Can<LogicalType> getAliases() {
        return aliasedFacet != null
                ? aliasedFacet.getAliases()
                : Can.empty();
    }

    // -- HIERARCHICAL

    @Override
    public boolean isOfType(final ObjectSpecification other) {

        var thisClass = this.getCorrespondingClass();
        var otherClass = other.getCorrespondingClass();

        return thisClass == otherClass
                || otherClass.isAssignableFrom(thisClass);
    }

    @Override
    public boolean isOfTypeResolvePrimitive(final ObjectSpecification other) {

        var thisClass = ClassUtils.resolvePrimitiveIfNecessary(this.getCorrespondingClass());
        var otherClass = ClassUtils.resolvePrimitiveIfNecessary(other.getCorrespondingClass());

        return thisClass == otherClass
                || otherClass.isAssignableFrom(thisClass);
    }

    // -- NAME, DESCRIPTION, PERSISTABILITY

    @Override
    public String getSingularName() {
        return lookupFacet(ObjectNamedFacet.class)
            .flatMap(textFacet->textFacet.translated())
            // unexpected code reach, however keep for JUnit testing
            .orElseGet(()->String.format(
                    "(%s has neither title- nor object-named-facet)",
                    getFullIdentifier()));
    }

    /**
     * The translated description according to any available {@link ObjectDescribedFacet},
     * else empty string (<tt>""</tt>).
     */
    @Override
    public String getDescription() {
        return lookupFacet(ObjectDescribedFacet.class)
                .map(ObjectDescribedFacet::translated)
                .orElse("");
    }

    /*
     * help is typically a reference (eg a URL) and so should not default to a
     * textual value if not set up
     */
    @Override
    public String getHelp() {
        var helpFacet = getFacet(HelpFacet.class);
        return helpFacet == null ? null : helpFacet.value();
    }

    @Override
    public final Optional<Contributing> contributing() {
        return mixinFacet()
                .map(MixinFacet::contributing);
    }

    // -- FACET HANDLING

    @Override
    public <Q extends Facet> Q getFacet(final Class<Q> facetType) {

        synchronized(unmodifiableInterfaces) {

            // lookup facet holder's facet
            var facets1 = _NullSafe.streamNullable(facetHolder.getFacet(facetType));

            // lookup all interfaces
            var facets2 = _NullSafe.stream(interfaces())
                    .filter(_NullSafe::isPresent) // just in case
                    .map(interfaceSpec->interfaceSpec.getFacet(facetType));

            // search up the inheritance hierarchy
            var facets3 = _NullSafe.streamNullable(superclass())
                    .map(superSpec->superSpec.getFacet(facetType));

            var facetsCombined = _Streams.concat(facets1, facets2, facets3);

            var notANoopFacetFilter = new NotANoopFacetFilter<Q>();

            return facetsCombined
                    .filter(notANoopFacetFilter)
                    .findFirst()
                    .orElse(notANoopFacetFilter.noopFacet);

        }
    }

    @Domain.Exclude
    private static class NotANoopFacetFilter<Q extends Facet> implements Predicate<Q> {
        Q noopFacet;

        @Override
        public boolean test(final Q facet) {
            if(facet==null) {
                return false;
            }
            if(!facet.getPrecedence().isFallback()) {
                return true;
            }
            if(noopFacet == null) {
                noopFacet = facet;
            }
            return false;
        }
    }

    @Override
    public ObjectTitleContext createTitleInteractionContext(
            final ManagedObject targetObjectAdapter,
            final InteractionInitiatedBy interactionMethod) {

        return new ObjectTitleContext(targetObjectAdapter, getFeatureIdentifier(),
                targetObjectAdapter.getTitle(),
                interactionMethod);
    }

    // -- SUPERCLASS, INTERFACES, SUBCLASSES, IS-ABSTRACT

    @Override
    public ObjectSpecification superclass() {
        return superclassSpec;
    }

    @Override
    public Can<ObjectSpecification> interfaces() {
        return unmodifiableInterfaces.get();
    }

    @Override
    public Can<ObjectSpecification> subclasses(final Depth depth) {
        if (depth == Depth.DIRECT) {
            return directSubclasses.snapshot();
        }

        // depth == Depth.TRANSITIVE)
        if (transitiveSubclasses == null) {
            transitiveSubclasses = transitiveSubclasses();
        }

        return transitiveSubclasses.snapshot();
    }

    private synchronized Subclasses transitiveSubclasses() {
        final Subclasses appendTo = new Subclasses();
        appendSubclasses(this, appendTo);
        transitiveSubclasses = appendTo;
        return transitiveSubclasses;
    }

    private void appendSubclasses(
            final ObjectSpecification objectSpecification,
            final Subclasses appendTo) {

        var directSubclasses = objectSpecification.subclasses(Depth.DIRECT);
        for (ObjectSpecification subclass : directSubclasses) {
            appendTo.addSubclass(subclass);
            appendSubclasses(subclass, appendTo);
        }

    }

    @Override
    public boolean hasSubclasses() {
        return directSubclasses.hasSubclasses();
    }

    // -- ASSOCIATIONS

    @Override
    public Stream<ObjectAssociation> streamDeclaredAssociations(final MixedIn mixedIn) {
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED);

        mixedInAssociationAdder.trigger(this::createMixedInAssociationsAndResort); // only if not already

        synchronized(unmodifiableAssociations) {
            return stream(unmodifiableAssociations.get())
                    .filter(mixedIn.toFilter());
        }
    }

    @Override
    public Optional<? extends ObjectMember> getMember(final String memberId) {
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED);

        if(_Strings.isEmpty(memberId)) {
            return Optional.empty();
        }

        var objectAction = getAction(memberId);
        if(objectAction.isPresent()) {
            return objectAction;
        }
        var association = getAssociation(memberId);
        if(association.isPresent()) {
            return association;
        }
        return Optional.empty();
    }

    @Override
    public Optional<ObjectAssociation> getDeclaredAssociation(final String id, final MixedIn mixedIn) {
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED);

        if(_Strings.isEmpty(id)) {
            return Optional.empty();
        }

        return streamDeclaredAssociations(mixedIn)
                .filter(objectAssociation->objectAssociation.getId().equals(id))
                .findFirst();
    }

    @Override
    public Stream<ObjectAction> streamRuntimeActions(final MixedIn mixedIn) {
        var actionScopes = ActionScope.forEnvironment(getMetaModelContext().getSystemEnvironment());
        return streamActions(actionScopes, mixedIn);
    }

    @Override
    public Stream<ObjectAction> streamDeclaredActions(
            final ImmutableEnumSet<ActionScope> actionScopes,
            final MixedIn mixedIn) {
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED);

        mixedInActionAdder.trigger(this::createMixedInActionsAndResort);

        return actionScopes.stream()
                .flatMap(actionScope->stream(objectActionsByType.get(actionScope)))
                .filter(mixedIn.toFilter());
    }

    // -- mixin associations (properties and collections)
    /**
     * Creates all mixed in properties and collections for this spec.
     */
    private Stream<ObjectAssociation> createMixedInAssociations() {
        if (isInjectable() || isValue()) {
            return Stream.empty();
        }
        return getCausewayBeanTypeRegistry().streamMixinTypes()
                .flatMap(this::createMixedInAssociation);
    }

    private Stream<ObjectAssociation> createMixedInAssociation(final Class<?> mixinType) {

        var mixinSpec = specLoaderInternal().loadSpecification(mixinType,
                IntrospectionState.FULLY_INTROSPECTED);
        if (mixinSpec == null
                || mixinSpec == this) {
            return Stream.empty();
        }
        var mixinFacet = mixinSpec.mixinFacet().orElse(null);
        if(mixinFacet == null) {
            // this shouldn't happen; to be covered by meta-model validation later
            return Stream.empty();
        }
        if(!mixinFacet.isMixinFor(getCorrespondingClass())) {
            return Stream.empty();
        }
        var mixinMethodName = mixinFacet.getMainMethodName();

        return mixinSpec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED)
        .filter(_SpecPredicates::isMixedInAssociation)
        .map(ObjectActionDefault.class::cast)
        .map(_MixedInMemberFactory.mixedInAssociation(this, mixinSpec, mixinMethodName))
        .peek(facetProcessor::processMemberOrder);
    }

    // -- mixin actions
    /**
     * Creates all mixed in actions for this spec.
     */
    private Stream<ObjectActionMixedIn> createMixedInActions() {
        return getCausewayBeanTypeRegistry().streamMixinTypes()
            .flatMap(this::createMixedInAction);
    }

    private Stream<ObjectActionMixedIn> createMixedInAction(final Class<?> mixinType) {

        var mixinSpec = specLoaderInternal().loadSpecification(mixinType,
                IntrospectionState.FULLY_INTROSPECTED);
        if (mixinSpec == null
                || mixinSpec == this) {
            return Stream.empty();
        }
        var mixinFacet = mixinSpec.mixinFacet().orElse(null);
        if(mixinFacet == null) {
            // this shouldn't happen; to be covered by meta-model validation later
            return Stream.empty();
        }
        if(!mixinFacet.isMixinFor(getCorrespondingClass())) {
            return Stream.empty();
        }
        // don't mixin Object_ mixins to domain services
        if(getBeanSort().isManagedBeanContributing()
                && mixinFacet.isMixinFor(java.lang.Object.class)) {
            return Stream.empty();
        }

        var mixinMethodName = mixinFacet.getMainMethodName();

        return mixinSpec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED)
        // value types only support constructor mixins
        .filter(this::whenIsValueThenIsAlsoConstructorMixin)
        .filter(_SpecPredicates::isMixedInAction)
        .map(ObjectActionDefault.class::cast)
        .map(_MixedInMemberFactory.mixedInAction(this, mixinSpec, mixinMethodName))
        .peek(facetProcessor::processMemberOrder);
    }

    /**
     * Whether the mixin's main method returns an instance of type equal to the mixee's type.
     * <p>
     * Introduced to support constructor mixins for value-types and
     * also to support associated <i>Actions</i> for <i>Action Parameters</i>.
     */
    private boolean whenIsValueThenIsAlsoConstructorMixin(final ObjectAction act) {
        return getBeanSort().isValue()
                ? Objects.equals(this, act.getReturnType())
                : true;
    }

    // -- VALIDITY

    @Override
    public Consent isValid(
            final ManagedObject targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return isValidResult(targetAdapter, interactionInitiatedBy).createConsent();
    }

    @Override
    public InteractionResult isValidResult(
            final ManagedObject targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        var validityContext =
                createValidityInteractionContext(
                        targetAdapter, interactionInitiatedBy);
        return InteractionUtils.isValidResult(this, validityContext);
    }

    /**
     * Create an {@link InteractionContext} representing an attempt to save the
     * object.
     */
    @Override
    public ObjectValidityContext createValidityInteractionContext(
            final ManagedObject targetAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        return new ObjectValidityContext(targetAdapter, getFeatureIdentifier(), interactionInitiatedBy);
    }

    // -- convenience isXxx (looked up from facets)
    @Override
    public boolean isImmutable() {
        return containsFacet(ImmutableFacet.class);
    }

    @Override
    public boolean isHidden() {
        return containsFacet(HiddenFacet.class);
    }

    @Override
    public boolean isParented() {
        return containsFacet(ParentedCollectionFacet.class);
    }

    // -- MIXIN ADDER ONESHOTs

    private final _Oneshot mixedInActionAdder = new _Oneshot();
    private final _Oneshot mixedInAssociationAdder = new _Oneshot();

    /**
     * one-shot: must be no-op, if already created
     */
    private void createMixedInActionsAndResort() {
        var include = isEntityOrViewModelOrAbstract()
                || getBeanSort().isManagedBeanContributing()
                // in support of composite value-type constructor mixins
                || getBeanSort().isValue();
        if(!include) {
            return;
        }
        var mixedInActions = createMixedInActions()
                .collect(Collectors.toList());
        if(mixedInActions.isEmpty()) {
           return; // nothing to do (this spec has no mixed-in actions, regular actions have already been added)
        }

        var regularActions = _Lists.newArrayList(objectActions); // defensive copy

        // note: we are doing this before any member sorting
        _MemberIdClashReporting.flagAnyMemberIdClashes(this, regularActions, mixedInActions);

        replaceActions(Stream.concat(
                regularActions.stream(),
                mixedInActions.stream()));
    }

    /**
     * one-shot: must be no-op, if already created
     */
    private void createMixedInAssociationsAndResort() {
        if(!isEntityOrViewModelOrAbstract()) {
            return;
        }
        var mixedInAssociations = createMixedInAssociations()
                .collect(Collectors.toList());
        if(mixedInAssociations.isEmpty()) {
           return; // nothing to do (this spec has no mixed-in associations, regular associations have already been added)
        }

        var regularAssociations = _Lists.newArrayList(associations); // defensive copy

        // note: we are doing this before any member sorting
        _MemberIdClashReporting.flagAnyMemberIdClashes(this, regularAssociations, mixedInAssociations);

        replaceAssociations(Stream.concat(
                regularAssociations.stream(),
                mixedInAssociations.stream()));
    }

    @Getter(lazy = true)
    private final CausewayBeanTypeRegistry causewayBeanTypeRegistry =
        getServiceRegistry()
                .lookupServiceElseFail(CausewayBeanTypeRegistry.class);

    @Getter(lazy = true)
    private final Can<EntityTitleSubscriber> titleSubscribers =
        getServiceRegistry().select(EntityTitleSubscriber.class);

}
