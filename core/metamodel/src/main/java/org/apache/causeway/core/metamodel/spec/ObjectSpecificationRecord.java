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
package org.apache.causeway.core.metamodel.spec;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.ObjectSupport.IconResource;
import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaFacet;
import org.apache.causeway.core.metamodel.facets.members.iconfa.FaLayersProvider;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet.Contributing;
import org.apache.causeway.core.metamodel.facets.object.navparent.NavigableParentFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionContainer;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spi.EntityTitleSubscriber;
import org.springframework.util.StringUtils;

//TODO WIP
public record ObjectSpecificationRecord(
		CausewayBeanMetaData typeMeta,
        FeatureType featureType,
        FacetHolder facetHolder,
        Hierarchical hierarchical,
        ObjectActionContainer actionContainer,
        ObjectAssociationContainer associationContainer,
        Can<EntityTitleSubscriber> titleSubscribers,
        IntrospectionPolicy introspectionPolicy,
        Can<LogicalType> aliases,
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
    	boolean isDomainService,
    	boolean isInjectable,
    	boolean isParented,
		boolean isImmutable,
		boolean isHidden,
		Map<ResolvedMethod, ObjectMember> membersByMethod)
implements
	ObjectSpecification {

    // -- SPECIFICATION

    @Override public FeatureType getFeatureType() { return featureType; }
    @Override public FacetHolder getFacetHolder() { return facetHolder; }

    // -- HIERARCHICAL

    @Override public Can<ObjectSpecification> interfaces() {
        return hierarchical.interfaces();
    }
    @Override public boolean isOfType(final ObjectSpecification other) {
        return hierarchical.isOfType(other);
    }
    @Override public boolean isOfTypeResolvePrimitive(final ObjectSpecification other) {
        return hierarchical.isOfTypeResolvePrimitive(other);
    }
    @Override public ObjectSpecification superclass() {
        return hierarchical.superclass();
    }

    // -- ACTION CONTAINER

    @Override public Optional<ObjectAction> getAction(final String id, final ImmutableEnumSet<ActionScope> actionScopes, final MixedIn mixedIn) {
        return actionContainer.getAction(id, actionScopes, mixedIn);
    }
    @Override public Optional<ObjectAction> getDeclaredAction(final String id, final ImmutableEnumSet<ActionScope> actionScopes, final MixedIn mixedIn) {
        return actionContainer.getDeclaredAction(id, actionScopes, mixedIn);
    }
    @Override public Stream<ObjectAction> streamActions(final ImmutableEnumSet<ActionScope> actionTypes, final MixedIn mixedIn, final Consumer<ObjectAction> onActionOverloaded) {
        return actionContainer.streamActions(actionTypes, mixedIn, onActionOverloaded);
    }
    @Override public Stream<ObjectAction> streamRuntimeActions(final MixedIn mixedIn) {
        return actionContainer.streamRuntimeActions(mixedIn);
    }
    @Override public Stream<ObjectAction> streamActionsForColumnRendering(final Where where) {
        return actionContainer.streamActionsForColumnRendering(where);
    }
    @Override public Stream<ObjectAction> streamDeclaredActions(final ImmutableEnumSet<ActionScope> actionTypes, final MixedIn mixedIn) {
        return actionContainer.streamDeclaredActions(actionTypes, mixedIn);
    }

    // -- ASSOCIATION CONTAINER

    @Override public Optional<ObjectAssociation> getAssociation(final String id, final MixedIn mixedIn) {
        return associationContainer.getAssociation(id, mixedIn);
    }
    @Override public Optional<ObjectAssociation> getDeclaredAssociation(final String id, final MixedIn mixedIn) {
        return associationContainer.getDeclaredAssociation(id, mixedIn);
    }
    @Override public Stream<ObjectAssociation> streamAssociations(final MixedIn mixedIn) {
        return associationContainer.streamAssociations(mixedIn);
    }
    @Override public Stream<ObjectAssociation> streamAssociationsForColumnRendering(final ColumnQuery columnQuery) {
    	return associationContainer.streamAssociationsForColumnRendering(columnQuery);
    }
    @Override public Stream<ObjectAssociation> streamDeclaredAssociations(final MixedIn mixedIn) {
        return associationContainer.streamDeclaredAssociations(mixedIn);
    }

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

    // -- COMPONENTS AND GETTERS

    @Override public BeanSort beanSort() { return typeMeta.beanSort(); }
    @Override public IntrospectionPolicy getIntrospectionPolicy() { return introspectionPolicy; }
    @Override public Class<?> getCorrespondingClass() { return typeMeta.getCorrespondingClass(); }
	@Override public LogicalType logicalType() { return typeMeta.logicalType(); }
	@Override public String getFullIdentifier() { return getCorrespondingClass().getName(); }
	@Override public String getShortIdentifier() { return logicalType().logicalSimpleName(); }

	@Override
	public Optional<? extends ObjectMember> getMember(final String memberId) {
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
	public Optional<? extends ObjectMember> getMember(final ResolvedMethod method) {
        return Optional.ofNullable(membersByMethod.get(method));
	}
	@Override
	public String getSingularName() {
		return objectNamedFacet
            .flatMap(ObjectNamedFacet::translated)
            // unexpected code reach, however keep for JUnit testing
            .orElseGet(()->"(%s has neither title- nor object-named-facet)"
            	.formatted(getFullIdentifier()));
	}
	@Override
	public String getDescription() {
		return objectDescribedFacet
            .map(ObjectDescribedFacet::translated)
            .orElse("");
	}
	@Override
	public String getTitle(final TitleRenderRequest titleRenderRequest) {
        if (titleFacet.isPresent()) {
            var titleString = titleFacet.get().title(titleRenderRequest);
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
        return iconFacet
            .flatMap(facet->facet.icon(domainObject, iconSize))
            .or(()->faLayers(domainObject)
                .map(ObjectSupport.FontAwesomeIconResource::new));
	}
	@Override
	public Object getNavigableParent(final Object object) {
		return navigableParentFacet
				.map(facet->facet.navigableParent(object))
				.orElse(null);
	}
	@Override
	public String getCssClass(final ManagedObject domainObject) {
		return cssClassFacet
			.map(facet->facet.cssClass(domainObject))
			.orElse(null);
	}
	@Override
	public Optional<ObjectSpecification> explicitElementSpec() {
		return typeOfFacet
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
		return Hierarchical.lookupFacet(facetType, facetHolder, this);
	}

	// -- HELPER

	private Optional<FontAwesomeLayers> faLayers(final ManagedObject domainObject){
        return faFacet
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
			.ifPresent(bookmark ->
			titleSubscribers
			.forEach(subscriber -> subscriber.entityTitleIs(bookmark, titleString)));
	}
}
