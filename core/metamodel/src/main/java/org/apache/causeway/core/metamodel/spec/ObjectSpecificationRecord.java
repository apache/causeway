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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.annotation.ObjectSupport.IconResource;
import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.causeway.core.metamodel.facets.object.logicaltype.AliasedFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet.Contributing;
import org.apache.causeway.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.causeway.core.metamodel.interactions.acc.ObjectTitleContext;
import org.apache.causeway.core.metamodel.interactions.val.ObjectValidityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionContainer;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

//TODO[causeway-core-metamodel-CAUSEWAY-3834] WIP
public record ObjectSpecificationRecord(
		CausewayBeanMetaData typeMeta,
        FeatureType featureType,
        FacetHolder facetHolder,
        Hierarchical hierarchical,
        ObjectActionContainer actionContainer,
        ObjectAssociationContainer associationContainer,
        IntrospectionPolicy introspectionPolicy,
    	Optional<ValueFacet<?>> valueFacet,
    	Optional<EntityFacet> entityFacet,
    	Optional<ViewModelFacet> viewmodelFacet,
    	Optional<MixinFacet> mixinFacet,
    	_Lazy<Can<LogicalType>> aliases,
    	_Lazy<Boolean> isDomainServiceLazy,
    	_Lazy<Boolean> isInjectableLazy)
implements
	ObjectSpecification {

//	ObjectSpecificationRecord(
//			final CausewayBeanMetaData typeMeta,
//	        final FeatureType featureType,
//	        final FacetHolder facetHolder,
//	        final Hierarchical hierarchical,
//	        final ObjectActionContainer actionContainer,
//	        final ObjectAssociationContainer associationContainer,
//	        final IntrospectionPolicy introspectionPolicy) {
//		this(typeMeta, featureType, facetHolder, hierarchical, actionContainer, associationContainer, introspectionPolicy,
//				null, null, null, null,
//				_Lazy.threadSafe(()->Hierarchical.lookupFacet(AliasedFacet.class, facetHolder, hierarchical)
//						.map(AliasedFacet::getAliases)
//						.orElseGet(Can::empty)),
//				_Lazy.threadSafe(()->_ClassCache.getInstance()
//						.head(typeMeta.getCorrespondingClass())
//						.hasAnnotation(DomainService.class))
//				);
//	}

	public ObjectSpecificationRecord {
		aliases = _Lazy.threadSafe(()->Hierarchical.lookupFacet(AliasedFacet.class, facetHolder, hierarchical)
					.map(AliasedFacet::getAliases)
					.orElseGet(Can::empty));
		isDomainServiceLazy = _Lazy.threadSafe(()->_ClassCache.getInstance()
				.head(typeMeta.getCorrespondingClass())
				.hasAnnotation(DomainService.class));

		boolean isVetoedForInjection = switch (typeMeta.managedBy()) {
	        case NONE, CAUSEWAY, PERSISTENCE -> true;
	        case UNSPECIFIED, SPRING  -> false;
		};

		isInjectableLazy = _Lazy.threadSafe(()->
	        !isVetoedForInjection
	                && !typeMeta.beanSort().isAbstract()
	                && !typeMeta.beanSort().isValue()
	                && !typeMeta.beanSort().isEntity()
	                && !typeMeta.beanSort().isViewModel()
	                && !typeMeta.beanSort().isMixin()
	                && (typeMeta.beanSort().isManagedBeanAny()
	                        || getServiceRegistry()
	                                .lookupRegisteredBeanById(typeMeta.logicalType())
	                                .isPresent()));
	}


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
            .formatted(getFullIdentifier(), getBeanSort().name(), superclass() == null
                ? "Object"
                : superclass().getFullIdentifier());
    }

    // -- COMPONENTS AND GETTERS

    @Override public BeanSort getBeanSort() { return typeMeta.beanSort(); }
    @Override public IntrospectionPolicy getIntrospectionPolicy() { return introspectionPolicy; }
    @Override public Class<?> getCorrespondingClass() { return typeMeta.getCorrespondingClass(); }
	@Override public LogicalType logicalType() { return typeMeta.logicalType(); }
	@Override public String getFullIdentifier() { return getCorrespondingClass().getName(); }
	@Override public String getShortIdentifier() { return logicalType().logicalSimpleName(); }
	@Override public Can<LogicalType> getAliases() { return aliases().get(); }
	@Override public boolean isDomainService() { return isDomainServiceLazy.get(); }
	@Override public boolean isInjectable() { return isInjectableLazy.get(); }
	@Override public boolean isParented() { return containsFacet(ParentedCollectionFacet.class); }
	@Override public boolean isImmutable() { return containsFacet(ImmutableFacet.class); }
	@Override public boolean isHidden() { return containsFacet(HiddenFacet.class); }

	@Override
	public Optional<? extends ObjectMember> getMember(final String memberId) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
	@Override
	public Optional<? extends ObjectMember> getMember(final ResolvedMethod method) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
	@Override
	public String getSingularName() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getTitle(final TitleRenderRequest titleRenderRequest) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Optional<IconResource> getIcon(final ManagedObject object, final IconSize iconSize) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
	@Override
	public Object getNavigableParent(final Object object) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getCssClass(final ManagedObject domainObject) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Optional<ObjectSpecification> explicitElementSpec() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
	@Override
	public Optional<Contributing> contributing() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
	@Override
	public ObjectTitleContext createTitleInteractionContext(final ManagedObject targetObjectAdapter,
			final InteractionInitiatedBy invocationMethod) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ObjectValidityContext createValidityInteractionContext(final ManagedObject targetAdapter,
			final InteractionInitiatedBy interactionInitiatedBy) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Consent isValid(final ManagedObject targetAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public InteractionResult isValidResult(final ManagedObject targetAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
		// TODO Auto-generated method stub
		return null;
	}

	// -- FACET LOOKUP

	@Override
	public <T extends Facet> Optional<T> lookupFacet(final Class<T> facetType) {
		return Hierarchical.lookupFacet(facetType, facetHolder, this);
	}

}
