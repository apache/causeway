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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Oneshot;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.collections._Streams;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.help.HelpFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclassfa.CssClassFaFactory;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.causeway.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.causeway.core.metamodel.facets.object.logicaltype.AliasedFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet.Contributing;
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
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.causeway.core.metamodel.specloader.postprocessor.PostProcessor;

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@EqualsAndHashCode(of = "correspondingClass", callSuper = false)
@lombok.ToString(of = {"correspondingClass", "fullName", "beanSort"})
@Log4j2
public abstract class ObjectSpecificationAbstract
extends ObjectMemberContainer
implements ObjectSpecification {

    /**
     * @implNote thread-safe
     */
    private static class Subclasses {

        // List performs better compared to a Set, when the number of elements is low
        private Can<ObjectSpecification> classes = Can.empty();
        private final Object $lock = new Object();

        public void addSubclass(final ObjectSpecification subclass) {
            synchronized($lock) {
                classes = classes.addUnique(subclass);
            }
        }

        public boolean hasSubclasses() {
            synchronized($lock) {
                return classes.isNotEmpty();
            }
        }

        public Can<ObjectSpecification> snapshot() {
            synchronized($lock) {
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
    @Getter private final Set<Method> potentialOrphans = _Sets.newHashSet();

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


    // -- Constructor
    protected ObjectSpecificationAbstract(
            final Class<?> introspectedClass,
            final LogicalType logicalType,
            final String shortName,
            final BeanSort beanSort,
            final FacetProcessor facetProcessor,
            final PostProcessor postProcessor) {

        super(facetProcessor.getMetaModelContext(), Identifier.classIdentifier(logicalType));

        this.correspondingClass = introspectedClass;
        this.logicalType = logicalType;
        this.fullName = introspectedClass.getName();
        this.shortName = shortName;
        this.beanSort = beanSort;

        this.facetProcessor = facetProcessor;
        this.postProcessor = postProcessor;
    }

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

    public abstract IntrospectionPolicy getIntrospectionPolicy();

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
                // set to avoid infinite loops
                this.introspectionState = IntrospectionState.TYPE_BEING_INTROSPECTED;
                introspectTypeHierarchy();
                invalidateCachedFacets();
                this.introspectionState = IntrospectionState.TYPE_INTROSPECTED;
            }
            if(isLessThan(upTo)) {
                this.introspectionState = IntrospectionState.MEMBERS_BEING_INTROSPECTED;
                introspectMembers();
                this.introspectionState = IntrospectionState.FULLY_INTROSPECTED;
                revalidate = true;
            }
            // set to avoid infinite loops
            break;
        case TYPE_BEING_INTROSPECTED:
            // nothing to do
            break;
        case TYPE_INTROSPECTED:
            if(isLessThan(upTo)) {
                // set to avoid infinite loops
                this.introspectionState = IntrospectionState.MEMBERS_BEING_INTROSPECTED;
                introspectMembers();
                this.introspectionState = IntrospectionState.FULLY_INTROSPECTED;
                revalidate = true;
            }
            break;
        case MEMBERS_BEING_INTROSPECTED:
            // nothing to do
        case FULLY_INTROSPECTED:
            // nothing to do
            break;

        default:
            throw _Exceptions.unexpectedCodeReach();
        }

        if(revalidate) {
            getSpecificationLoader().validateLater(this);
        }
    }

    boolean isLessThan(final IntrospectionState upTo) {
        return this.introspectionState.compareTo(upTo) < 0;
    }

    protected abstract void introspectTypeHierarchy();
    protected abstract void introspectMembers();

    protected void loadSpecOfSuperclass(final Class<?> superclass) {
        if (superclass == null) {
            return;
        }
        superclassSpec = getSpecificationLoader().loadSpecification(superclass);
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
        if (!(supertypeSpec instanceof ObjectSpecificationAbstract)) {
            return;
        }
        // downcast required because addSubclass is (deliberately) not public
        // API
        val introspectableSpec = (ObjectSpecificationAbstract) supertypeSpec;
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
        val orderedAssociations = _MemberSortingUtils.sortAssociationsIntoList(associations);
        synchronized (unmodifiableAssociations) {
            this.associations.clear();
            this.associations.addAll(orderedAssociations);
            unmodifiableAssociations.clear(); // invalidate
        }
    }

    protected final void replaceActions(final Stream<ObjectAction> objectActions) {
        val orderedActions = _MemberSortingUtils.sortActionsIntoList(objectActions);
        synchronized (unmodifiableActions){
            this.objectActions.clear();
            this.objectActions.addAll(orderedActions);
            unmodifiableActions.clear(); // invalidate

            // rebuild objectActionsByType multi-map
            for (val actionType : ActionScope.values()) {
                val objectActionForType = objectActionsByType.getOrElseNew(actionType);
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
            val titleString = titleFacet.title(titleRenderRequest);
            if (!_Strings.isEmpty(titleString)) {
                return titleString;
            }
        }
        val prefix = this.isInjectable()
                ? ""
                : "Untitled ";
        return prefix + getSingularName();
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
        val iconNameModifier = getIconName(domainObject);
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
    public Can<LogicalType> getAliases() {
        return aliasedFacet != null
                ? aliasedFacet.getAliases()
                : Can.empty();
    }

    @Override
    public Optional<CssClassFaFactory> getCssClassFaFactory() {
        return lookupFacet(CssClassFaFacet.class)
        .map(CssClassFaFacet::getSpecialization)
        // assuming CssClassFaFacet on objects are always 'static' not 'imperative'
        .flatMap(either->either.left())
        .map(CssClassFaFactory.class::cast);
    }

    // -- HIERARCHICAL

    @Override
    public boolean isOfType(final ObjectSpecification other) {

        val thisClass = this.getCorrespondingClass();
        val otherClass = other.getCorrespondingClass();

        return thisClass == otherClass
                || otherClass.isAssignableFrom(thisClass);
    }

    @Override
    public boolean isOfTypeResolvePrimitive(final ObjectSpecification other) {

        val thisClass = ClassUtils.resolvePrimitiveIfNecessary(this.getCorrespondingClass());
        val otherClass = ClassUtils.resolvePrimitiveIfNecessary(other.getCorrespondingClass());

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
        val helpFacet = getFacet(HelpFacet.class);
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
            val facets1 = _NullSafe.streamNullable(super.getFacet(facetType));

            // lookup all interfaces
            val facets2 = _NullSafe.stream(interfaces())
                    .filter(_NullSafe::isPresent) // just in case
                    .map(interfaceSpec->interfaceSpec.getFacet(facetType));

            // search up the inheritance hierarchy
            val facets3 = _NullSafe.streamNullable(superclass())
                    .map(superSpec->superSpec.getFacet(facetType));

            val facetsCombined = _Streams.concat(facets1, facets2, facets3);

            val notANoopFacetFilter = new NotANoopFacetFilter<Q>();

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

        val directSubclasses = objectSpecification.subclasses(Depth.DIRECT);
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

        val objectAction = getAction(memberId);
        if(objectAction.isPresent()) {
            return objectAction;
        }
        val association = getAssociation(memberId);
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
        val actionScopes = ActionScope.forEnvironment(getMetaModelContext().getSystemEnvironment());
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

        val mixinSpec = getSpecificationLoader().loadSpecification(mixinType,
                IntrospectionState.FULLY_INTROSPECTED);
        if (mixinSpec == null
                || mixinSpec == this) {
            return Stream.empty();
        }
        val mixinFacet = mixinSpec.mixinFacet().orElse(null);
        if(mixinFacet == null) {
            // this shouldn't happen; to be covered by meta-model validation later
            return Stream.empty();
        }
        if(!mixinFacet.isMixinFor(getCorrespondingClass())) {
            return Stream.empty();
        }
        val mixinMethodName = mixinFacet.getMainMethodName();

        return mixinSpec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED)
        .filter(_SpecPredicates::isMixedInAssociation)
        .map(ObjectActionDefault.class::cast)
        .map(_MixedInMemberFactory.mixedInAssociation(this, mixinType, mixinMethodName))
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

        val mixinSpec = getSpecificationLoader().loadSpecification(mixinType,
                IntrospectionState.FULLY_INTROSPECTED);
        if (mixinSpec == null
                || mixinSpec == this) {
            return Stream.empty();
        }
        val mixinFacet = mixinSpec.mixinFacet().orElse(null);
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

        val mixinMethodName = mixinFacet.getMainMethodName();

        return mixinSpec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED)
        // value types only support constructor mixins
        .filter(this::whenIsValueThenIsAlsoConstructorMixin)
        .filter(_SpecPredicates::isMixedInAction)
        .map(ObjectActionDefault.class::cast)
        .map(_MixedInMemberFactory.mixedInAction(this, mixinType, mixinMethodName))
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
        val validityContext =
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
        val include = isEntityOrViewModelOrAbstract()
                || getBeanSort().isManagedBeanContributing()
                // in support of composite value-type constructor mixins
                || getBeanSort().isValue();
        if(!include) {
            return;
        }
        val mixedInActions = createMixedInActions()
                .collect(Collectors.toList());
        if(mixedInActions.isEmpty()) {
           return; // nothing to do (this spec has no mixed-in actions, regular actions have already been added)
        }

        val regularActions = _Lists.newArrayList(objectActions); // defensive copy

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
        val mixedInAssociations = createMixedInAssociations()
                .collect(Collectors.toList());
        if(mixedInAssociations.isEmpty()) {
           return; // nothing to do (this spec has no mixed-in associations, regular associations have already been added)
        }

        val regularAssociations = _Lists.newArrayList(associations); // defensive copy

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

}
