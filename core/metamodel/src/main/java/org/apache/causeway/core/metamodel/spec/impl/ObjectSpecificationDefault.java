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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
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
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
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
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.Hierarchical;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spi.EntityTitleSubscriber;
import org.apache.causeway.core.metamodel.util.Facets;
import org.jspecify.annotations.NonNull;
import org.springframework.util.Assert;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ObjectSpecificationDefault
implements
	ObjectSpecificationInternal,
	HasObjectActionContainer,
	HasObjectAssociationContainer {

    // -- CONSTRUCTION

    private final FacetedMethodsFactory facetedMethodsFactory; //TODO in support of reloading, this factory should be recreated, as it holds a MethodRemover that is stateful
    private final _Lazy<Boolean> isInjectableLazy;
    private final _Lazy<Boolean> isDomainServiceLazy;
    private final Can<EntityTitleSubscriber> titleSubscribers;

    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true)
    private final FacetHolder facetHolder;

    @Getter @Accessors(fluent = true)
	private final IntrospectionStateHandler introspectionStateHandler;

    @Getter(onMethod_={@Override}) @Accessors(fluent = true)
    private final IntrospectionPolicy introspectionPolicy;

    @Getter @Accessors(fluent = true)
    private final CausewayBeanMetaData typeMeta;

    @Getter @Accessors(fluent = true)
    private AssociationContainer objectAssociationContainer = AssociationContainer.EMPTY;
    @Getter @Accessors(fluent = true)
    private ActionContainer objectActionContainer = ActionContainer.EMPTY;
    @Getter @Accessors(fluent = true)
    private MemberCatalog memberCatalog = MemberCatalog.EMPTY;
    @Getter @Accessors(fluent = true)
    private Hierarchical hierarchical = Hierarchical.EMPTY;
    @Getter @Accessors(fluent = true)
    private final ConsistencyContext consistencyContext;

    private final _Lazy<Optional<ObjectSpecification>> elementSpecification =
    		_Lazy.threadSafe(()->lookupFacet(TypeOfFacet.class)
    				.map(TypeOfFacet::elementSpec));

    private final SpecificationLoaderInternal specLoaderInternal;
    private final PostProcessor postProcessor;

    private ValueFacet<?> valueFacet;
    private EntityFacet entityFacet;
    private ViewModelFacet viewmodelFacet;
    private MixinFacet mixinFacet;
    private TitleFacet titleFacet;
    private IconFacet iconFacet;
    private NavigableParentFacet navigableParentFacet;
    private AliasedFacet aliasedFacet;
    private CssClassFacet cssClassFacet;

    public ObjectSpecificationDefault(
            final @NonNull CausewayBeanMetaData typeMeta,
            final @NonNull FacetProcessor facetProcessor,
            final @NonNull PostProcessor postProcessor,
            final @NonNull ClassSubstitutorRegistry classSubstitutorRegistry,
            final @NonNull Can<EntityTitleSubscriber> titleSubscribers,
            final @NonNull Supplier<MixinSpecStreamer> mixinSpecStreamerSupplier) {

        final MetaModelContext mmc = facetProcessor.getMetaModelContext();
        this.specLoaderInternal = (SpecificationLoaderInternal) mmc.getSpecificationLoader();

    	this.typeMeta = typeMeta;
    	this.isInjectableLazy = _Lazy.threadSafe(()->typeMeta.isInjectable(mmc.getServiceRegistry()));
    	this.isDomainServiceLazy = _Lazy.threadSafe(()->
        	_ClassCache.getInstance().head(correspondingClass()).hasAnnotation(DomainService.class));
    	this.titleSubscribers = titleSubscribers;
    	this.consistencyContext = new ConsistencyContext(typeMeta);

        this.facetHolder = FacetHolder.simple(
            mmc,
            Identifier.classIdentifier(logicalType()));

        this.postProcessor = postProcessor;

        // must install EncapsulationFacet (if any) and MemberAnnotationPolicyFacet (if any)
        facetProcessor.processObjectType(typeMeta.correspondingClass(), this);

        // naturally supports attribute inheritance from the type's hierarchy
        this.introspectionPolicy = lookupFacet(IntrospectionPolicyFacet.class)
                .map(IntrospectionPolicyFacet::introspectionPolicy)
                .orElseGet(()->mmc.getConfiguration().core().metaModel().introspector().policy());

        this.facetedMethodsFactory =
                new FacetedMethodsFactory(this, specLoaderInternal, facetProcessor, classSubstitutorRegistry);

        final var hierarchicalFactory =
        		new HierarchicalFactory(specLoaderInternal, classSubstitutorRegistry, facetHolder);

        this.introspectionStateHandler = new IntrospectionStateHandlerThreadSafe(
        		()->{
        			introspectTypeHierarchy(hierarchicalFactory);
        	        invalidateCachedFacets();
        		},
        		()->{
        			introspectMembers(mixinSpecStreamerSupplier.get());
//        	        // make sure we've loaded the facets from layout.xml also.
        	        Facets.gridPreload(this, null);
        	        specLoaderInternal.validateLater(this);
        		});
    }

    // --

	@Override public FeatureType featureType() { return FeatureType.OBJECT; }
    @Override public BeanSort beanSort() { return typeMeta.beanSort(); }
    @Override public Class<?> correspondingClass() { return typeMeta.correspondingClass(); }
	@Override public LogicalType logicalType() { return typeMeta.logicalType(); }
	@Override public String fullIdentifier() { return correspondingClass().getName(); }
	@Override public String shortIdentifier() { return logicalType().logicalSimpleName(); }
//	@Override public Can<LogicalType> getAliases() { return aliases().get(); }
	@Override public boolean isDomainService() { return isDomainServiceLazy.get(); }
	@Override public boolean isInjectable() { return isInjectableLazy.get(); }
	@Override public boolean isParented() { return containsFacet(ParentedCollectionFacet.class); }
	@Override public boolean isImmutable() { return containsFacet(ImmutableFacet.class); }
	@Override public boolean isHidden() { return containsFacet(HiddenFacet.class); }
	@Override public Optional<ObjectSpecification> superSpec() { return hierarchical.superSpec(); }
    @Override public Can<ObjectSpecification> interfaceSpecs() { return hierarchical.interfaceSpecs(); }
    // state handling
    @Override public boolean isFullyIntrospected() { return introspectionStateHandler.isFullyIntrospected(); }

    // -- CONTRACT

    @Override
    public int hashCode() {
        return correspondingClass().hashCode();
    }
    @Override
    public boolean equals(final Object o) {
        return (o instanceof ObjectSpecification other)
            ? Objects.equals(this.correspondingClass(), other.correspondingClass())
            : false;
    }
    @Override
    public String toString() {
        return "ObjSpec[class=%s, sort=%s, super=%s]"
            .formatted(fullIdentifier(), beanSort().name(), superSpec().isEmpty()
                ? "Object"
                : superSpec().get().fullIdentifier());
    }

    private void introspectTypeHierarchy(final HierarchicalFactory hierarchicalFactory) {
        facetedMethodsFactory.introspectClass();

        // name
        addNamedFacetIfRequired();

        // go no further if a value
        if(this.isValue())
			return;

        this.hierarchical = hierarchicalFactory.createHierarchical(correspondingClass());
    }

    private void introspectMembers(final MixinSpecStreamer mixinSpecStreamer) {

        // yet this logic does not skip UNKNONW
        if(this.beanSort().isCollection()
                || this.beanSort().isVetoed()
                || this.isValue()) {
            if (log.isDebugEnabled()) {
                log.debug("skipping full introspection for {} type {}", this.beanSort(), fullIdentifier());
            }
            return;
        }
        Assert.isTrue(!isFullyIntrospected(), ()->"object spec for '%s' is in lockdown, because postprocessing already had run (cannot run twice)"
        		.formatted(fullIdentifier()));

        // fully introspect up the type hierarchy including interfaces
        // because members creation depends on presence of inherited members
        // (don't include self)
        Stream.concat(
        		hierarchical().streamSuperTypeHierarchy(),
        		hierarchical().interfaceSpecs().stream())
    		.map(ObjectSpecificationDefault.class::cast)
    		.forEach(spec->spec.introspectionStateHandler.introspectFully());

        // create associations and actions

    	var view = toView();

        var regularMemberFactory = new RegularMemberFactory(mixinFacet(), facetedMethodsFactory);
        var regularAssociations = regularMemberFactory.createAssociations().toList();
        var regularActions = regularMemberFactory.createActions().toList();

        var mixedInMemberFactory = new MixedInMemberFactory(this, isMixin()
        		? MixinSpecStreamer.EMPTY
				: mixinSpecStreamer);
        var mixedInAssociations = mixedInMemberFactory.createMixedInAssociations();
        var mixedInActions = mixedInMemberFactory.createMixedInActions();

        var syntheticActions = getConfiguration().extensions().commandLog().recordingSupport().isEnabled()
    		? new SyntheticNavigationActionFactory(this, regularAssociations, mixedInAssociations, regularActions, mixedInActions).synthesizeNavigationActions()
    		: List.<ObjectAction>of();

        this.objectAssociationContainer = new AssociationContainer(
        		_MemberSortingUtils.associationsInOrder(typeMeta, regularAssociations, mixedInAssociations),
        		superSpec().orElse(null),
        		view);
        this.objectActionContainer = new ActionContainer(
        		_MemberSortingUtils.actionsInOrder(typeMeta, regularActions, mixedInActions, syntheticActions),
        		ActionScope.forEnvironment(getMetaModelContext().getSystemEnvironment()),
        		superSpec().orElse(null));

        //TODO? can we run mixin creation without triggering full introspection of other types ... if(!isMixin()) {
		postProcessor.postProcess(this);
		//}
		this.memberCatalog = new MemberCatalog(objectAssociationContainer, objectActionContainer);

        invalidateCachedFacets();
    }

    //TODO this is a facet factory responsibility, should be done when the facet processor runs on the type ... see constructor
    @Deprecated
    private void addNamedFacetIfRequired() {
        if (lookupFacet(MemberNamedFacet.class).isEmpty()) {
            new MemberNamedFacetForStaticMemberName(
                    _Strings.asNaturalName.apply(shortIdentifier()),
                    this);
        }
    }

    @Override
    public Optional<? extends ObjectMember> lookupMember(final ResolvedMethod method) {
    	introspectionStateHandler.introspectFully();
    	return memberCatalog.lookupMember(method);
    }

    // -- ELEMENT SPECIFICATION

    @Override
    public Optional<ObjectSpecification> explicitElementSpec() {
        return elementSpecification.get();
    }

    private void invalidateCachedFacets() {
        this.valueFacet = lookupFacet(ValueFacet.class).orElse(null);
        this.titleFacet = lookupNonFallbackFacet(TitleFacet.class).orElse(null);
        this.iconFacet = lookupFacet(IconFacet.class).orElse(null);
        this.navigableParentFacet = lookupFacet(NavigableParentFacet.class).orElse(null);
        this.cssClassFacet = lookupFacet(CssClassFacet.class).orElse(null);
        this.aliasedFacet = lookupFacet(AliasedFacet.class).orElse(null);
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
            mixinFacet = lookupFacet(MixinFacet.class).orElse(null);
        }
        return Optional.ofNullable(mixinFacet);
    }

    @Override
    public final Optional<EntityFacet> entityFacet() {
        // deliberately don't memoize lookup misses, because could be too early
        if(entityFacet==null) {
            entityFacet = lookupFacet(EntityFacet.class).orElse(null);
        }
        return Optional.ofNullable(entityFacet);
    }

    @Override
    public final Optional<ViewModelFacet> viewmodelFacet() {
        // deliberately don't memoize lookup misses, because could be too early
        if(viewmodelFacet==null) {
            viewmodelFacet = lookupFacet(ViewModelFacet.class).orElse(null);
        }
        return Optional.ofNullable(viewmodelFacet);
    }

    @Override
    public String getTitle(final TitleRenderRequest titleRenderRequest) {
        if (titleFacet != null) {
            var titleString = titleFacet.title(titleRenderRequest);
            if (!_Strings.isEmpty(titleString)) {
                notifyAnyTitleSubscribers(titleRenderRequest, titleString);
                return titleString;
            }
        }
        var prefix = this.isInjectable()
                ? ""
                : "Untitled ";
        return prefix + getSingularName();
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

    // -- NAME, DESCRIPTION, PERSISTABILITY

    @Override
    public String getSingularName() {
        return lookupFacet(ObjectNamedFacet.class)
            .flatMap(ObjectNamedFacet::translated)
            // unexpected code reach, however keep for JUnit testing
            .orElseGet(()->"(%s has neither title- nor object-named-facet)"
            	.formatted(fullIdentifier()));
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
        return Hierarchical.lookupFacet(facetType, facetHolder, this);
    }

    // -- INHERITED

    @Override
    public Optional<? extends ObjectMember> lookupMember(final String memberId) {
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

    // -- SHALLOW IMMUTABLE / EXPERIMENTAL

	private ObjectMetaDataView toView() {
		return new ObjectMetaDataView() {
			@Override public CausewayBeanMetaData typeMeta() {
				return typeMeta;
			}
			@Override public <T extends Facet> Optional<T> lookupFacet(@NonNull final Class<T> facetType) {
				return ObjectSpecificationDefault.this.lookupFacet(facetType);
			}
			@Override public Identifier featureIdentifier() {
				return ObjectSpecificationDefault.this.getFeatureIdentifier();
			}
			@Override
			public ObjectAssociationContainer associationContainer() {
				return ObjectSpecificationDefault.this.objectAssociationContainer();
			}
		};

	}

    // -- HELPER

    private void notifyAnyTitleSubscribers(
    		final TitleRenderRequest titleRenderRequest,
    		final String titleString) {
    	if (!isEntity())
    		return;

    	var managedObject = titleRenderRequest.object();
    	managedObject.getBookmark().ifPresent(bookmark -> {
    		titleSubscribers.stream().forEach(x -> x.entityTitleIs(bookmark, titleString));
    	});
    }

}
