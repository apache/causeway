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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.debug._Debug.Profiler;
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
import org.apache.causeway.core.metamodel.spec.ObjectSpecificationRecord;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.core.metamodel.spi.EntityTitleSubscriber;
import org.apache.causeway.core.metamodel.util.Facets;
import org.jspecify.annotations.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ObjectSpecificationDefault
implements
	ObjectSpecificationBuilder,
	HasIntrospectionStateHandler,
	HasObjectActionContainer,
	HasObjectAssociationContainer {

    // -- CONSTRUCTION

    /**
     * Lazily built by {@link #getMember(ResolvedMethod)}.
     */
    private Map<ResolvedMethod, ObjectMember> membersByMethod = null;

    private final FacetedMethodsFactory facetedMethodsFactory;
    private final ClassSubstitutorRegistry classSubstitutorRegistry;
    private final _Lazy<Boolean> isInjectableLazy;
    private final _Lazy<Boolean> isDomainServiceLazy;

    @Getter @Accessors(fluent = true)
	private final IntrospectionStateHandler introspectionStateHandler;

    @Getter(onMethod_={@Override})
    private final IntrospectionPolicy introspectionPolicy;

    @Getter @Accessors(fluent = true)
    private final CausewayBeanMetaData typeMeta;

    @Getter @Accessors(fluent = true)
    private AssociationContainer objectAssociationContainer = AssociationContainer.EMPTY;
    @Getter @Accessors(fluent = true)
    private ActionContainer objectActionContainer = ActionContainer.EMPTY;

    public ObjectSpecificationDefault(
    		final Profiler profiler,
            final @NonNull CausewayBeanMetaData typeMeta,
            final @NonNull FacetProcessor facetProcessor,
            final @NonNull PostProcessor postProcessor,
            final @NonNull ClassSubstitutorRegistry classSubstitutorRegistry,
            final @NonNull MixinSpecStreamer mixinSpecStreamer) {

        final MetaModelContext mmc = facetProcessor.getMetaModelContext();

    	this.typeMeta = typeMeta;
    	this.isInjectableLazy = _Lazy.threadSafe(()->typeMeta.isInjectable(mmc.getServiceRegistry()));
    	this.isDomainServiceLazy = _Lazy.threadSafe(()->
        	_ClassCache.getInstance().head(getCorrespondingClass()).hasAnnotation(DomainService.class));

        this.facetHolder = FacetHolder.simple(
            mmc,
            Identifier.classIdentifier(logicalType()));

        this.postProcessor = postProcessor;
        this.classSubstitutorRegistry = classSubstitutorRegistry;

        // must install EncapsulationFacet (if any) and MemberAnnotationPolicyFacet (if any)
        facetProcessor.processObjectType(typeMeta.getCorrespondingClass(), this);

        // naturally supports attribute inheritance from the type's hierarchy
        this.introspectionPolicy = lookupFacet(IntrospectionPolicyFacet.class)
                .map(IntrospectionPolicyFacet::getIntrospectionPolicy)
                .orElseGet(()->mmc.getConfiguration().core().metaModel().introspector().policy());

        this.facetedMethodsFactory =
                new FacetedMethodsFactory(this, facetProcessor, classSubstitutorRegistry);

        this.introspectionStateHandler = new IntrospectionStateHandlerThreadSafe(
        		()->{
        			profiler.measure("types", this::introspectTypeHierarchy);
        			//introspectTypeHierarchy();
        	        invalidateCachedFacets();
        		},
        		()->{
        			profiler.measure("members", ()->introspectMembers(mixinSpecStreamer, profiler));
        	        //introspectMembers();
//        	        // make sure we've loaded the facets from layout.xml also.
        	        //Facets.gridPreload(this, null);
        			profiler.measure("gridPreload", ()->Facets.gridPreload(this, null));
        	        specLoaderInternal().validateLater(this);
        		});
    }

    // -- SHALLOW IMMUTABLE

	@Override
	public ObjectSpecificationRecord build() {
		return new ObjectSpecificationRecord(
				typeMeta,
				getFeatureType(),
				facetHolder,
				this,//Hierarchical,
				objectActionContainer,
				objectAssociationContainer,
				getServiceRegistry().select(EntityTitleSubscriber.class),
				introspectionPolicy,
				aliases(),
				valueFacet(),
		    	entityFacet(),
		    	viewmodelFacet(),
		    	mixinFacet(),
		    	lookupFacet(ObjectNamedFacet.class),
		    	lookupFacet(ObjectDescribedFacet.class),
		    	lookupFacet(TypeOfFacet.class),
		    	lookupNonFallbackFacet(TitleFacet.class),
		    	lookupFacet(IconFacet.class),
		    	lookupFacet(FaFacet.class),
		        lookupFacet(NavigableParentFacet.class),
		        lookupFacet(CssClassFacet.class),
				isDomainService(),
				isInjectable(),
				isParented(),
				isImmutable(),
				isHidden(),
				catalogueMembers());
	}

    // --

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

    private void introspectTypeHierarchy() {

        facetedMethodsFactory.introspectClass();

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

    private final AtomicBoolean isLockedDown = new AtomicBoolean(); //TODO temporary
    private void introspectMembers(final MixinSpecStreamer mixinSpecStreamer, final Profiler profiler) {

        // yet this logic does not skip UNKNONW
        if(this.beanSort().isCollection()
                || this.beanSort().isVetoed()
                || this.isValue()) {
            if (log.isDebugEnabled()) {
                log.debug("skipping full introspection for {} type {}", this.beanSort(), getFullIdentifier());
            }
            return;
        }
        Assert.isTrue(!isLockedDown.get(), ()->"object spec for '%s' is in lockdown, because postprocessing already had run (cannot run twice)"
        		.formatted(getCorrespondingClass().getName()));

        // fully introspect up the type hierarchy including interfaces
        // because members creation depends on presence of inherited members

        profiler.measure("hierarchy", ()->{
        	streamTypeHierarchyAndInterfaces()
	    		.forEach(it->((IntrospectionStateHandler)it)
	    			.introspectFully());
        });

        // create associations and actions

        var regularMemberFactory = new RegularMemberFactory(this, facetedMethodsFactory);
        var regularAssociations = profiler.measure("members.regularAssociations", ()->regularMemberFactory.createAssociations().toList());
        var regularActions = profiler.measure("members.regularActions", ()->regularMemberFactory.createActions().toList());

        var mixinSpecStreamerX = new MixinSpecStreamerOnTheFly(
        		specLoaderInternal(), getServiceRegistry().lookupServiceElseFail(CausewayBeanTypeRegistry.class));
        var mixedInMemberFactory = new MixedInMemberFactory(this, mixinSpecStreamerX);
        //XXX takes 50% of time
        var mixedInAssociations = profiler.measure("members.mixedInAssociations", ()->mixedInMemberFactory.createMixedInAssociations(profiler));
        var mixedInActions = profiler.measure("members.mixedInActions", ()->mixedInMemberFactory.createMixedInActions());

        this.objectAssociationContainer = new AssociationContainer(
        		associationsInOrder(regularAssociations, mixedInAssociations),
        		superclass(),
        		this);
        this.objectActionContainer = new ActionContainer(
        		actionsInOrder(regularActions, mixedInActions),
        		ActionScope.forEnvironment(getMetaModelContext().getSystemEnvironment()),
        		superclass());

        profiler.measure("members.postProcessor", ()->{
        	//XXX takes 50% of time
        	postProcessor.postProcess(this);
        });

        invalidateCachedFacets();

        isLockedDown.set(true);
    }

<<<<<<< Upstream, based on origin/main
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

=======
    //TODO this is a facet factory responsibility
    @Deprecated
>>>>>>> df432ff CAUSEWAY-4044: thread-safe IntrospectionStateHandler
    private void addNamedFacetIfRequired() {
        if (getFacet(MemberNamedFacet.class) == null) {
            addFacet(new MemberNamedFacetForStaticMemberName(
                    _Strings.asNaturalName.apply(getShortIdentifier()),
                    this));
        }
    }

    // -- getObjectAction

    @Override
    public Optional<? extends ObjectMember> getMember(final ResolvedMethod method) {
    	introspectFully();

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

    // -- FIELDS

    private final PostProcessor postProcessor;

    // -- ACTIONS

    /** not API, used for validation */
    @Getter private final Set<ResolvedMethod> potentialOrphans = _Sets.newHashSet();

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

    @Getter(onMethod_ = {@Override}) private final FacetHolder facetHolder;

    // -- Stuff immediately derivable from class
    @Override
    public final FeatureType getFeatureType() {
        return FeatureType.OBJECT;
    }

    private void loadSpecOfSuperclass(final Class<?> superclass) {
        if (superclass == null)
			return;

        this.superclassSpec = specLoaderInternal().loadSpecification(superclass);
        if (superclassSpec != null
        		&& log.isDebugEnabled()) {
            log.debug("  Superclass {}", superclass.getName());
        }
    }

    private void loadSpecOfInterfaces(final Class<?>[] interfaces) {
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

    private List<ObjectAssociation> associationsInOrder(
    		final List<ObjectAssociation> regularAssociations,
            final List<? extends ObjectAssociation> mixedInAssociations) {
    	_MemberIdClashReporting.flagAnyMemberIdClashes(this, regularAssociations, mixedInAssociations); // do before sorting
        return _MemberSortingUtils.sortAssociationsIntoList(Stream.concat(
                regularAssociations.stream(),
                mixedInAssociations.stream()));
    }

    private List<ObjectAction> actionsInOrder(
    		final List<ObjectAction> regularActions,
            final List<? extends ObjectAction> mixedInActions) {
    	_MemberIdClashReporting.flagAnyMemberIdClashes(this, regularActions, mixedInActions); // do before sorting
        return _MemberSortingUtils.sortActionsIntoList(Stream.concat(
        		regularActions.stream(),
        		mixedInActions.stream()));
    }

    private void invalidateCachedFacets() {
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
    public Can<LogicalType> aliases() {
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

    @Override //TODO separation of concerns ?
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

    @Override
    public Optional<? extends ObjectMember> getMember(final String memberId) {
    	introspectionStateHandler.introspectFully();

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

    @Getter(lazy = true)
    private final Can<EntityTitleSubscriber> titleSubscribers =
        getServiceRegistry().select(EntityTitleSubscriber.class);

}
