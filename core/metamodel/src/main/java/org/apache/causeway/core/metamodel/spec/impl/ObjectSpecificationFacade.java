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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.ObjectSupport.IconResource;
import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaLayersProvider;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
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
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.Hierarchical;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionContainer;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spi.EntityTitleSubscriber;
import org.springframework.util.StringUtils;

/**
 * A facade around mutable {@link ObjectMetaData}.
 */
record ObjectSpecificationFacade(
		CausewayBeanMetaData typeMeta,
        boolean isDomainService,
    	boolean isInjectable,
    	Can<EntityTitleSubscriber> titleSubscribers,
        /** mutable meta data */
        AtomicReference<ObjectMetaData> objectMetaDataRef)
implements
	ObjectSpecificationInternal,
	HasObjectActionContainer,
	HasObjectAssociationContainer {

	sealed interface ObjectMetaData
	permits ObjectMetaDataInitial, ObjectMetaDataTypeOnly, ObjectMetaDataFull {
		CausewayBeanMetaData typeMeta();
		IntrospectionPolicy introspectionPolicy();
		FacetHolder facetHolder();
		Can<LogicalType> aliases();
		Hierarchical hierarchical();
		ObjectActionContainer actionContainer();
        ObjectAssociationContainer associationContainer();
    	Optional<ValueFacet<?>> valueFacet();
    	Optional<EntityFacet> entityFacet();
    	Optional<ViewModelFacet> viewmodelFacet();
    	Optional<MixinFacet> mixinFacet();
    	Optional<ObjectNamedFacet> objectNamedFacet();
    	Optional<ObjectDescribedFacet> objectDescribedFacet();
    	Optional<TypeOfFacet> typeOfFacet(); // explicit element type
    	Optional<TitleFacet> titleFacet();
    	Optional<IconFacet> iconFacet();
    	Optional<FaFacet> faFacet();
    	Optional<NavigableParentFacet> navigableParentFacet();
    	Optional<CssClassFacet> cssClassFacet();
    	boolean isParented();
		boolean isImmutable();
		boolean isHidden();
    	Map<ResolvedMethod, ObjectMember> membersByMethod();
	}

	/**
	 * TODO WIP
	 */
	record ObjectMetaDataInitial(
			CausewayBeanMetaData typeMeta,
			IntrospectionPolicy introspectionPolicy,
			FacetHolder facetHolder)
	implements ObjectMetaData {
		@Override public Can<LogicalType> aliases() {
			throw new UnsupportedOperationException();
		}
		@Override public Hierarchical hierarchical() {
			throw new UnsupportedOperationException();
		}
		@Override public ObjectActionContainer actionContainer() {
			throw new UnsupportedOperationException();
		}
		@Override public ObjectAssociationContainer associationContainer() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<ValueFacet<?>> valueFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<EntityFacet> entityFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<ViewModelFacet> viewmodelFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<MixinFacet> mixinFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<ObjectNamedFacet> objectNamedFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<ObjectDescribedFacet> objectDescribedFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<TypeOfFacet> typeOfFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<TitleFacet> titleFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<IconFacet> iconFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<FaFacet> faFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<NavigableParentFacet> navigableParentFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<CssClassFacet> cssClassFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public boolean isParented() {
			throw new UnsupportedOperationException();
		}
		@Override public boolean isImmutable() {
			throw new UnsupportedOperationException();
		}
		@Override public boolean isHidden() {
			throw new UnsupportedOperationException();
		}
		@Override public Map<ResolvedMethod, ObjectMember> membersByMethod() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * TODO WIP
	 */
	record ObjectMetaDataTypeOnly(
			CausewayBeanMetaData typeMeta,
			IntrospectionPolicy introspectionPolicy,
			FacetHolder facetHolder,
			Can<LogicalType> aliases, //TODO too early?
			Hierarchical hierarchical)
	implements ObjectMetaData {
		@Override public ObjectActionContainer actionContainer() {
			throw new UnsupportedOperationException();
		}
		@Override public ObjectAssociationContainer associationContainer() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<ValueFacet<?>> valueFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<EntityFacet> entityFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<ViewModelFacet> viewmodelFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<MixinFacet> mixinFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<ObjectNamedFacet> objectNamedFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<ObjectDescribedFacet> objectDescribedFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<TypeOfFacet> typeOfFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<TitleFacet> titleFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<IconFacet> iconFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<FaFacet> faFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<NavigableParentFacet> navigableParentFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public Optional<CssClassFacet> cssClassFacet() {
			throw new UnsupportedOperationException();
		}
		@Override public boolean isParented() {
			throw new UnsupportedOperationException();
		}
		@Override public boolean isImmutable() {
			throw new UnsupportedOperationException();
		}
		@Override public boolean isHidden() {
			throw new UnsupportedOperationException();
		}
		@Override public Map<ResolvedMethod, ObjectMember> membersByMethod() {
			throw new UnsupportedOperationException();
		}
	}

	record ObjectMetaDataFull(
			CausewayBeanMetaData typeMeta,
			IntrospectionPolicy introspectionPolicy,
			FacetHolder facetHolder,
			Can<LogicalType> aliases,
			Hierarchical hierarchical,
			ObjectActionContainer actionContainer,
	        ObjectAssociationContainer associationContainer,
	    	Optional<ValueFacet<?>> valueFacet,
	    	Optional<EntityFacet> entityFacet,
	    	Optional<ViewModelFacet> viewmodelFacet,
	    	Optional<MixinFacet> mixinFacet,
	    	Optional<ObjectNamedFacet> objectNamedFacet,
	    	Optional<ObjectDescribedFacet> objectDescribedFacet,
	    	Optional<TypeOfFacet> typeOfFacet, // explicit element type
	    	Optional<TitleFacet> titleFacet,
	    	Optional<IconFacet> iconFacet,
	    	Optional<FaFacet> faFacet,
	    	Optional<NavigableParentFacet> navigableParentFacet,
	    	Optional<CssClassFacet> cssClassFacet,
	    	boolean isParented,
			boolean isImmutable,
			boolean isHidden,
	    	Map<ResolvedMethod, ObjectMember> membersByMethod) implements ObjectMetaData {

		ObjectMetaDataFull(
				final CausewayBeanMetaData typeMeta,
				final IntrospectionPolicy introspectionPolicy,
				final FacetHolder facetHolder,
				final Hierarchical hierarchical,
				final ObjectActionContainer actionContainer,
		        final ObjectAssociationContainer associationContainer,
		    	final Map<ResolvedMethod, ObjectMember> membersByMethod) {
			this(typeMeta, introspectionPolicy, facetHolder,
					facetHolder.lookupFacet(AliasedFacet.class)
						.map(AliasedFacet::getAliases)
						.orElseGet(Can::empty),
					hierarchical, actionContainer, associationContainer,
					_Casts.uncheckedCast(facetHolder.lookupFacet(ValueFacet.class)),
			    	facetHolder.lookupFacet(EntityFacet.class),
			    	facetHolder.lookupFacet(ViewModelFacet.class),
			    	facetHolder.lookupFacet(MixinFacet.class),
			    	facetHolder.lookupFacet(ObjectNamedFacet.class),
			    	facetHolder.lookupFacet(ObjectDescribedFacet.class),
			    	facetHolder.lookupFacet(TypeOfFacet.class),
			    	facetHolder.lookupNonFallbackFacet(TitleFacet.class),
			    	facetHolder.lookupFacet(IconFacet.class),
			    	facetHolder.lookupFacet(FaFacet.class),
			    	facetHolder.lookupFacet(NavigableParentFacet.class),
			    	facetHolder.lookupFacet(CssClassFacet.class),
			    	facetHolder.containsFacet(ParentedCollectionFacet.class),
					facetHolder.containsFacet(ImmutableFacet.class),
					facetHolder.containsFacet(HiddenFacet.class),
					membersByMethod);
		}
	}

	ObjectSpecificationFacade(
			final CausewayBeanMetaData typeMeta,
			final boolean isDomainService,
	    	final boolean isInjectable,
	        final Can<EntityTitleSubscriber> titleSubscribers,
	        final ObjectMetaData objectMetaData) {
		this(typeMeta, isDomainService, isInjectable,
				titleSubscribers, new AtomicReference<>(objectMetaData));
	}

	ObjectMetaData objectMetaData() { return objectMetaDataRef.get(); }

    // -- SPECIFICATION

    @Override public FacetHolder facetHolder() { return objectMetaData().facetHolder(); }

    // -- HIERARCHICAL

    @Override public Optional<ObjectSpecification> superSpec() { return hierarchical().superSpec(); }
    @Override public Can<ObjectSpecification> interfaceSpecs() { return hierarchical().interfaceSpecs(); }

    // -- ACTION CONTAINER

    @Override public ObjectActionContainer objectActionContainer() { return objectMetaData().actionContainer(); }

    // -- ASSOCIATION CONTAINER

    @Override public ObjectAssociationContainer objectAssociationContainer() { return objectMetaData().associationContainer(); }

    // -- COMPONENTS AND GETTERS

    @Override public BeanSort beanSort() { return typeMeta.beanSort(); }
    @Override public IntrospectionPolicy introspectionPolicy() { return objectMetaData().introspectionPolicy(); }
    @Override public Class<?> correspondingClass() { return typeMeta.correspondingClass(); }
	@Override public LogicalType logicalType() { return typeMeta.logicalType(); }
	@Override public String fullIdentifier() { return correspondingClass().getName(); }
	@Override public String shortIdentifier() { return logicalType().logicalSimpleName(); }
	@Override public Can<LogicalType> aliases() { return objectMetaData().aliases(); }

	@Override public Optional<ValueFacet<?>> valueFacet() { return objectMetaData().valueFacet(); }
	@Override public Optional<EntityFacet> entityFacet() { return objectMetaData().entityFacet(); }
	@Override public Optional<ViewModelFacet> viewmodelFacet() { return objectMetaData().viewmodelFacet(); }
	@Override public Optional<MixinFacet> mixinFacet() { return objectMetaData().mixinFacet(); }

	@Override public boolean isParented() { return objectMetaData().isParented(); }
	@Override public boolean isImmutable() { return objectMetaData().isImmutable(); }
	@Override public boolean isHidden() { return objectMetaData().isHidden(); }
	@Override public boolean isFullyIntrospected() { return objectMetaData() instanceof ObjectMetaDataFull; }

	@Override
	public ConsistencyContext consistencyContext() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	// -- MEMBER LOOKUP

	@Override
	public Optional<? extends ObjectMember> lookupMember(final String memberId) {
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
	public Optional<? extends ObjectMember> lookupMember(final ResolvedMethod method) {
		return Optional.ofNullable(objectMetaData().membersByMethod().get(method));
	}

	// -- INFERRED FROM FACETS

	@Override
	public String getSingularName() {
		return objectMetaData().objectNamedFacet()
            .flatMap(ObjectNamedFacet::translated)
            // unexpected code reach, however keep for JUnit testing
            .orElseGet(()->"(%s has neither title- nor object-named-facet)"
            	.formatted(fullIdentifier()));
	}
	@Override
	public String getDescription() {
		return objectMetaData().objectDescribedFacet()
            .map(ObjectDescribedFacet::translated)
            .orElse("");
	}
	@Override
	public String getTitle(final TitleRenderRequest titleRenderRequest) {
        if (objectMetaData().titleFacet().isPresent()) {
            var titleString = objectMetaData().titleFacet().get().title(titleRenderRequest);
            if(StringUtils.hasLength(titleString)) {
	            notifyTitleSubscribers(titleRenderRequest, titleString);
	            return titleString;
            }
        }
        return "%s%s"
    		.formatted(isInjectable
	                ? ""
	                : "Untitled ",
                getSingularName());
	}
	@Override
	public Optional<IconResource> getIcon(final ManagedObject domainObject, final IconSize iconSize) {
        if(ManagedObjects.isSpecified(domainObject)) {
            _Assert.assertEquals(domainObject.objSpec(), this);
        }
        return objectMetaData().iconFacet()
            .flatMap(facet->facet.icon(domainObject, iconSize))
            .or(()->faLayers(domainObject)
                .map(ObjectSupport.FontAwesomeIconResource::new));
	}
	@Override
	public Object getNavigableParent(final Object object) {
		return objectMetaData().navigableParentFacet()
				.map(facet->facet.navigableParent(object))
				.orElse(null);
	}
	@Override
	public String getCssClass(final ManagedObject domainObject) {
		return objectMetaData().cssClassFacet()
			.map(facet->facet.cssClass(domainObject))
			.orElse(null);
	}
	@Override
	public Optional<ObjectSpecification> explicitElementSpec() {
		return objectMetaData().typeOfFacet()
            .map(TypeOfFacet::elementSpec);
	}
	@Override
	public Optional<Contributing> contributing() {
		return mixinFacet()
            .map(MixinFacet::contributing);
	}

	// -- FACET LOOKUP

	@Override
	public <T extends Facet> Optional<T> lookupFacet(final Class<T> facetType) {
		return Hierarchical.lookupFacet(facetType, facetHolder(), this);
	}

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

    // -- HELPER

    private Hierarchical hierarchical() { return objectMetaData().hierarchical(); }

	private Optional<FontAwesomeLayers> faLayers(final ManagedObject domainObject){
        return objectMetaData().faFacet()
            .map(FaFacet::getSpecialization)
            .map(either->either.fold(
                faStaticFacet->(FaLayersProvider)faStaticFacet,
                faImperativeFacet->faImperativeFacet.getFaLayersProvider(domainObject)))
            .map(FaLayersProvider::getLayers);
    }

	private void notifyTitleSubscribers(final TitleRenderRequest titleRenderRequest, final String titleString) {
		if(!isEntity()
				|| titleSubscribers.isEmpty())
			return;
		titleRenderRequest
			.object()
			.getBookmark()
			.ifPresent(bookmark -> titleSubscribers
				.forEach(subscriber -> subscriber.entityTitleIs(bookmark, titleString)));
	}

}
