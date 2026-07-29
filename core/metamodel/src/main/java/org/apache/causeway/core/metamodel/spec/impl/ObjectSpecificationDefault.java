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

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._Oneshot;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ParentedCollectionNavigationFacet;
import org.apache.causeway.core.metamodel.facets.actions.synthetic.ScalarReferenceNavigationFacet;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacetForStaticMemberName;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaLayersProvider;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.causeway.core.metamodel.facets.object.introspection.IntrospectionPolicyFacet;
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
import org.apache.causeway.core.metamodel.interactions.acc.ObjectTitleContext;
import org.apache.causeway.core.metamodel.interactions.val.ObjectValidityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.Hierarchical;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.impl.MemberPopulator.IntrospectionState;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.core.metamodel.spi.EntityTitleSubscriber;
import org.apache.causeway.core.metamodel.util.Facets;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.util.ClassUtils;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ObjectSpecificationDefault
implements ObjectMemberContainer, ObjectSpecificationMutable, HasSpecificationLoaderInternal {

    // -- CONSTRUCTION

    /**
     * Lazily built by {@link #getMember(ResolvedMethod)}.
     */
    private Map<ResolvedMethod, ObjectMember> membersByMethod = null;

    private final FacetedMethodsBuilder facetedMethodsBuilder;
    private final ClassSubstitutorRegistry classSubstitutorRegistry;
    private final _MembersAsColumns columnHelper;
    private final _Lazy<Boolean> isInjectableLazy;
    private final _Lazy<Boolean> isDomainServiceLazy;

    @Getter(onMethod_={@Override})
    private final IntrospectionPolicy introspectionPolicy;

    @Getter @Accessors(fluent = true)
    private final CausewayBeanMetaData typeMeta;

    public ObjectSpecificationDefault(
            final @NonNull CausewayBeanMetaData typeMeta,
            final @NonNull MetaModelContext mmc,
            final @NonNull FacetProcessor facetProcessor,
            final @NonNull PostProcessor postProcessor,
            final @NonNull ClassSubstitutorRegistry classSubstitutorRegistry) {

    	this.typeMeta = typeMeta;
    	this.isInjectableLazy = _Lazy.threadSafe(()->typeMeta.isInjectable(getServiceRegistry()));
    	this.isDomainServiceLazy = _Lazy.threadSafe(()->
        	_ClassCache.getInstance().head(getCorrespondingClass()).hasAnnotation(DomainService.class));

        this.facetHolder = FacetHolder.simple(
            facetProcessor.getMetaModelContext(),
            Identifier.classIdentifier(logicalType()));

        this.postProcessor = postProcessor;
        this.classSubstitutorRegistry = classSubstitutorRegistry;

        // must install EncapsulationFacet (if any) and MemberAnnotationPolicyFacet (if any)
        facetProcessor.processObjectType(typeMeta.getCorrespondingClass(), this);

        // naturally supports attribute inheritance from the type's hierarchy
        this.introspectionPolicy = lookupFacet(IntrospectionPolicyFacet.class)
                .map(IntrospectionPolicyFacet::getIntrospectionPolicy)
                .orElseGet(()->mmc.getConfiguration().core().metaModel().introspector().policy());

        this.facetedMethodsBuilder =
                new FacetedMethodsBuilder(this, facetProcessor, classSubstitutorRegistry);

        this.columnHelper = new _MembersAsColumns(mmc);
    }

    @Override public BeanSort beanSort() { return typeMeta.beanSort(); }
    @Override public Class<?> getCorrespondingClass() { return typeMeta.getCorrespondingClass(); }
	@Override public LogicalType logicalType() { return typeMeta.logicalType(); }
	@Override public String getFullIdentifier() { return getCorrespondingClass().getName(); }
	@Override public String getShortIdentifier() { return logicalType().logicalSimpleName(); }
//	@Override public Can<LogicalType> getAliases() { return aliases().get(); }
	@Override public boolean isDomainService() { return isDomainServiceLazy.get(); }
	@Override public boolean isInjectable() { return isInjectableLazy.get(); }
	@Override public boolean isParented() { return containsFacet(ParentedCollectionFacet.class); }
	@Override public boolean isImmutable() { return containsFacet(ImmutableFacet.class); }
	@Override public boolean isHidden() { return containsFacet(HiddenFacet.class); }

    // -- CONTRACT

    @Override
    public int hashCode() {
        return getCorrespondingClass().hashCode();
    }
    @Override
    public boolean equals(final Object o) {
        return (o instanceof ObjectSpecification other)
            ? Objects.equals(this.getCorrespondingClass(), other.getCorrespondingClass())
            : false;
    }
    @Override
    public String toString() {
        return "ObjSpec[class=%s, sort=%s, super=%s]"
            .formatted(getFullIdentifier(), beanSort().name(), superclass() == null
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

        loadSpecOfSuperclass(getCorrespondingClass().getSuperclass());
        loadSpecOfInterfaces(getCorrespondingClass().getInterfaces());
    }

    private void introspectMembers() {

        // yet this logic does not skip UNKNONW
        if(this.beanSort().isCollection()
                || this.beanSort().isVetoed()
                || this.isValue()) {
            if (log.isDebugEnabled()) {
                log.debug("skipping full introspection for {} type {}", this.beanSort(), getFullIdentifier());
            }
            return;
        }

        var memberFactory = new RegularMemberFactory(this, facetedMethodsBuilder);

        // create associations and actions
        replaceAssociations(memberFactory.createAssociations());
        replaceActions(memberFactory.createActions());

        postProcessor.postProcess(this);
        invalidateCachedFacets();
    }

    @Override
    public void synthesizeNavigationActions() {
        if (!getMetaModelContext().getConfiguration()
                .extensions().commandLog().recordingSupport().isEnabled()) {
            return;
        }

        mixedInAssociationAdder.trigger(this::createMixedInAssociationsAndResort);
        var existingActionIds = objectActions.stream()
                .map(ObjectAction::getId)
                .collect(Collectors.toSet());
        var existingSyntheticActionIds = objectActions.stream()
                .filter(action -> action.lookupFacet(ParentedCollectionNavigationFacet.class).isPresent()
                        || action.lookupFacet(ScalarReferenceNavigationFacet.class).isPresent())
                .map(ObjectAction::getId)
                .collect(Collectors.toSet());
        var syntheticActions = SyntheticNavigationActionFactory.createFor(
                        getMetaModelContext(),
                        this,
                        associations.stream(),
                        existingActionIds,
                        existingSyntheticActionIds)
                .toList();
        if (syntheticActions.isEmpty()) {
            return;
        }

        replaceActions(Stream.concat(objectActions.stream(), syntheticActions.stream()));
        membersByMethod = null;
    }

    private void addNamedFacetIfRequired() {
        if (getFacet(MemberNamedFacet.class) == null) {
            addFacet(new MemberNamedFacetForStaticMemberName(
                    _Strings.asNaturalName.apply(getShortIdentifier()),
                    this));
        }
    }

    // -- getObjectAction

    @Override
    public Optional<ObjectAction> getDeclaredAction(
            final @Nullable String id,
            final ImmutableEnumSet<ActionScope> actionScopes,
            final MixedIn mixedIn) {

        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED,
                ()->"getDeclaredAction %s on %s".formatted(id, this.getFeatureIdentifier()));

        return _Strings.isEmpty(id)
            ? Optional.empty()
            : streamDeclaredActions(actionScopes, mixedIn)
                .filter(action->
                    id.equals(action.getFeatureIdentifier().getMemberNameAndParameterClassNamesIdentityString())
                            || id.equals(action.getFeatureIdentifier().memberLogicalName())
                )
                .findFirst();
    }

    @Override
    public Optional<? extends ObjectMember> getMember(final ResolvedMethod method) {
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED,
                ()->"getMember %s on %s".formatted(method.name(), this.getFeatureIdentifier()));

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
                    .map(TypeOfFacet::elementSpec));

    @Override
    public Optional<ObjectSpecification> explicitElementSpec() {
        return elementSpecification.get();
    }

    // -- TABLE COLUMN RENDERING

    @Override public Stream<ObjectAssociation> streamAssociationsForColumnRendering(final ColumnQuery columnQuery) {
	   return columnHelper.streamAssociationsForColumnRendering(this, columnQuery);
    }

    @Override
    public Stream<ObjectAction> streamActionsForColumnRendering(final Where where) {
        return columnHelper.streamActionsForColumnRendering(this, where);
    }



    //-----------------------------------------------------------------------------------------------------------------
    // MERGED FROM FORMER ObjectSpecificationAbstract
    //-----------------------------------------------------------------------------------------------------------------

    // -- FIELDS

    private final PostProcessor postProcessor;

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

    private ObjectSpecification superclassSpec;

    private ValueFacet<?> valueFacet;
    private EntityFacet entityFacet;
    private ViewModelFacet viewmodelFacet;
    private MixinFacet mixinFacet;
    private TitleFacet titleFacet;
    private IconFacet iconFacet;
    private NavigableParentFacet navigableParentFacet;
    private AliasedFacet aliasedFacet;
    private CssClassFacet cssClassFacet;

    private IntrospectionState introspectionState = IntrospectionState.NOT_INTROSPECTED;

    @Getter(onMethod_ = {@Override}) private final FacetHolder facetHolder;

    // -- Stuff immediately derivable from class
    @Override
    public final FeatureType getFeatureType() {
        return FeatureType.OBJECT;
    }

    @Override
    public void introspect(final IntrospectionRequest request) {
        switch (request) {
            case REGISTER -> introspectUpTo(IntrospectionState.NOT_INTROSPECTED,
                ()->"introspect(%s)".formatted(request));
            case TYPE_ONLY -> introspectUpTo(IntrospectionState.TYPE_INTROSPECTED,
                ()->"introspect(%s)".formatted(request));
            case FULL -> introspectUpTo(IntrospectionState.FULLY_INTROSPECTED,
                ()->"introspect(%s)".formatted(request));
        }
    }

    /**
     * @param introspectionContextProvider keeps track of the causal chain of introspection requests
     */
    private void introspectUpTo(final IntrospectionState upTo, final Supplier<String> introspectionContextProvider) {
        if(!isLessThan(upTo))
			return; // optimization

        if(log.isDebugEnabled()) {
            log.debug("introspectingUpTo: {}, {}", getFullIdentifier(), upTo);
        }

        switch (introspectionState) {
            case NOT_INTROSPECTED->{
                if(isLessThan(upTo)) {
                    introspectType();
                }
                if(isLessThan(upTo)) {
                    introspectFully();
                    specLoaderInternal().validateLater(this, introspectionContextProvider);
                }
            }
            case TYPE_BEING_INTROSPECTED->{} // nothing to do (interim state during introspectType)
            case TYPE_INTROSPECTED->{
                if(isLessThan(upTo)) {
                    introspectFully();
                    specLoaderInternal().validateLater(this, introspectionContextProvider);
                }
            }
            case MEMBERS_BEING_INTROSPECTED->{}// nothing to do (interim state during introspect fully)
            case FULLY_INTROSPECTED->{}// nothing to do ... all done
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

    private boolean isLessThan(final IntrospectionState upTo) {
        return this.introspectionState.compareTo(upTo) < 0;
    }

    protected void loadSpecOfSuperclass(final Class<?> superclass) {
        if (superclass == null)
			return;

        this.superclassSpec = specLoaderInternal().loadSpecification(superclass);
        if (superclassSpec != null
        		&& log.isDebugEnabled()) {
            log.debug("  Superclass {}", superclass.getName());
        }
    }

    protected void loadSpecOfInterfaces(final Class<?>[] interfaces) {
    	if(interfaces==null)
			return;

    	var classCache = _ClassCache.getInstance();

        final List<ObjectSpecification> interfaceSpecList = Stream.of(interfaces)
    		// pre-filter common interfaces (performance)
        	.filter(interfaceType->!interfaceType.getName().startsWith("java."))
        	//--
        	.map(interfaceType->{
        		var substitution = classSubstitutorRegistry.getSubstitution(interfaceType);
                return substitution.isReplace()
                		? substitution.replacement()
        				: substitution.isNeverIntrospect()
    	    				? null
    	    				: interfaceType;
        	})
        	.filter(Objects::nonNull)
        	.filter(interfaceType->classCache.head(interfaceType).hasAnnotation(DomainObject.class))
        	.map(specLoaderInternal()::loadSpecification)
        	.filter(Objects::nonNull)
        	.toList();

        if(!interfaceSpecList.isEmpty()) {
        	if(interfaceSpecList.size()>1) {
              ValidationFailure.raiseFormatted(facetHolder,
            		  "Cannot use @DomainObject on more than one interface, as inherited by: %s",
            		  getCorrespondingClass().getName());
        	}
        	if (superclassSpec != null) {
        		var superType = superclassSpec.getCorrespondingClass();
        		if(classCache.head(superType).hasAnnotation(DomainObject.class)) {
        			ValidationFailure.raiseFormatted(facetHolder,
                  		  "Cannot use @DomainObject on both, abstract super class and one interface, as inherited by: %s",
                  		  getCorrespondingClass().getName());
        		}
        	}

//debug
//        	System.err.println("%s".formatted(getCorrespondingClass().getName()));
//        	interfaceSpecList.forEach(i->{
//        		System.err.println("- %s".formatted(i.getCorrespondingClass().getName()));
//        	});
        	synchronized(unmodifiableInterfaces) {
                this.interfaces.clear();
                this.interfaces.addAll(interfaceSpecList);
                unmodifiableInterfaces.clear();
            }
        }
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

    void invalidateCachedFacets() {
        this.valueFacet = getFacet(ValueFacet.class);
        this.titleFacet = lookupNonFallbackFacet(TitleFacet.class).orElse(null);
        this.iconFacet = getFacet(IconFacet.class);
        this.navigableParentFacet = getFacet(NavigableParentFacet.class);
        this.cssClassFacet = getFacet(CssClassFacet.class);
        this.aliasedFacet = getFacet(AliasedFacet.class);
    }

    @Override
    public final Optional<ValueFacet<?>> valueFacet() {
        if(valueFacet == null
                && beanSort().isValue()) {
            invalidateCachedFacets();
        }
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
        if (!isEntity())
			return;

        var managedObject = titleRenderRequest.object();
        managedObject.getBookmark().ifPresent(bookmark -> {
            getTitleSubscribers().stream().forEach(x -> x.entityTitleIs(bookmark, titleString));
        });
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

    // -- ICON

    @Override
    public Optional<ObjectSupport.IconResource> getIcon(final ManagedObject domainObject, final ObjectSupport.IconSize iconSize) {
        if(ManagedObjects.isSpecified(domainObject)) {
            _Assert.assertEquals(domainObject.objSpec(), this);
        }
        return Optional.ofNullable(iconFacet)
            .flatMap(facet->facet.icon(domainObject, iconSize))
            .or(()->faLayers(domainObject)
                .map(ObjectSupport.FontAwesomeIconResource::new));
    }

    private Optional<FontAwesomeLayers> faLayers(final ManagedObject domainObject){
        return lookupFacet(FaFacet.class)
            .map(FaFacet::getSpecialization)
            .map(either->either.fold(
                faStaticFacet->(FaLayersProvider)faStaticFacet,
                faImperativeFacet->faImperativeFacet.getFaLayersProvider(domainObject)))
            .map(FaLayersProvider::getLayers);
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
            .flatMap(ObjectNamedFacet::translated)
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

    @Override
    public final Optional<Contributing> contributing() {
        return mixinFacet()
                .map(MixinFacet::contributing);
    }

    // -- FACET HANDLING

    @Override
    public <Q extends Facet> Optional<Q> lookupFacet(final Class<Q> facetType) {
        synchronized(unmodifiableInterfaces) {
        	return Hierarchical.lookupFacet(facetType, facetHolder, this);
        }
    }

    @Override //FIXME separation of concerns
    public ObjectTitleContext createTitleInteractionContext(
            final ManagedObject targetObjectAdapter,
            final InteractionInitiatedBy interactionMethod) {

        return new ObjectTitleContext(targetObjectAdapter, getFeatureIdentifier(),
                targetObjectAdapter.getTitle(),
                interactionMethod);
    }

    // -- INHERITED

    @Override
    public ObjectSpecification superclass() {
        return superclassSpec;
    }

    @Override
    public Can<ObjectSpecification> interfaces() {
        return unmodifiableInterfaces.get();
    }

    // -- ASSOCIATIONS

    @Override
    public Stream<ObjectAssociation> streamDeclaredAssociations(final MixedIn mixedIn) {
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED,
                ()->"streamDeclaredAssociations of %s".formatted(this.getFeatureIdentifier()));

        mixedInMemberAdder.trigger(this::createMixedInMembersAndResort); // only if not already

        synchronized(unmodifiableAssociations) {
            return stream(unmodifiableAssociations.get())
                    .filter(mixedIn.toFilter());
        }
    }

    @Override
    public Optional<? extends ObjectMember> getMember(final String memberId) {
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED,
                ()->"getMember %s of %s".formatted(memberId, this.getFeatureIdentifier()));

        if(_Strings.isEmpty(memberId))
			return Optional.empty();

        var objectAction = getAction(memberId);
        if(objectAction.isPresent())
			return objectAction;

        var association = getAssociation(memberId);
        if(association.isPresent())
			return association;

        return Optional.empty();
    }

    @Override
    public Optional<ObjectAssociation> getDeclaredAssociation(final String id, final MixedIn mixedIn) {
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED,
                ()->"getDeclaredAssociation %s of %s".formatted(id, this.getFeatureIdentifier()));

        if(_Strings.isEmpty(id))
			return Optional.empty();

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
        introspectUpTo(IntrospectionState.FULLY_INTROSPECTED,
                ()->"streamDeclaredActions of %s".formatted(this.getFeatureIdentifier()));

        mixedInMemberAdder.trigger(this::createMixedInMembersAndResort);

        return actionScopes.stream()
                .flatMap(actionScope->stream(objectActionsByType.get(actionScope)))
                .filter(mixedIn.toFilter());
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

    // -- MIXIN ADDER ONESHOTs

    private final _Oneshot mixedInMemberAdder = new _Oneshot();

    /**
     * one-shot: must be no-op, if already created
     */
    private void createMixedInMembersAndResort() {
    	var memberFactory = new MixedInMemberFactory(this, specLoaderInternal());
    	createMixedInActionsAndResort(memberFactory);
    	createMixedInAssociationsAndResort(memberFactory);
    }

    private void createMixedInActionsAndResort(final MixedInMemberFactory memberFactory) {
        var mixedInActions = memberFactory.createMixedInActions();
        if(mixedInActions.isEmpty())
			return; // nothing to do (this spec has no mixed-in actions, regular actions have already been added)

        var regularActions = new ArrayList<>(objectActions); // defensive copy

        // note: we are doing this before any member sorting
        _MemberIdClashReporting.flagAnyMemberIdClashes(this, regularActions, mixedInActions);

        replaceActions(Stream.concat(
                regularActions.stream(),
                mixedInActions.stream()));
    }

    private void createMixedInAssociationsAndResort(final MixedInMemberFactory memberFactory) {
        var mixedInAssociations = memberFactory.createMixedInAssociations();
        if(mixedInAssociations.isEmpty())
			return; // nothing to do (this spec has no mixed-in associations, regular associations have already been added)

        var regularAssociations = new ArrayList<>(associations); // defensive copy

        // note: we are doing this before any member sorting
        _MemberIdClashReporting.flagAnyMemberIdClashes(this, regularAssociations, mixedInAssociations);

        replaceAssociations(Stream.concat(
                regularAssociations.stream(),
                mixedInAssociations.stream()));
    }

    @Getter(lazy = true)
    private final Can<EntityTitleSubscriber> titleSubscribers =
        getServiceRegistry().select(EntityTitleSubscriber.class);

    boolean isFullyIntrospected() {
        return this.introspectionState == IntrospectionState.FULLY_INTROSPECTED;
    }

}
