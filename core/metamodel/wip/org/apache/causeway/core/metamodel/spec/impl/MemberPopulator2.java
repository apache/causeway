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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Oneshot;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacetAbstract;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.impl.MemberPopulator.ComputedMembers;
import org.apache.causeway.core.metamodel.spec.impl.MemberPopulator.IntrospectionState;
import org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationMutable.IntrospectionRequest;
import org.apache.causeway.core.metamodel.util.Facets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
final class MemberPopulator2 {
	private final ObjectSpecification2 spec;
	private IntrospectionState introspectionState = IntrospectionState.NOT_INTROSPECTED;
	private final _Oneshot mixedInMembersAdder = new _Oneshot();
	private ComputedMembers computedMembers = new ComputedMembers();

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

    boolean isFullyIntrospected() {
    	return introspectionState == IntrospectionState.FULLY_INTROSPECTED;
    }

    void includeMixedInMembers(final Supplier<String> introspectionContextProvider) {
    	introspectUpTo(IntrospectionState.FULLY_INTROSPECTED, introspectionContextProvider);
    	mixedInMembersAdder.trigger(()->{
    		var associationsInOrder = createMixedInAssociationsAndResort(computedMembers.associationsInOrder().toList());
    		var actionsInOrder = createMixedInActionsAndResort(computedMembers.actionsInOrder().toList());
    		
    		spec.replaceMembers(new ComputedMembers(Can.ofCollection(associationsInOrder), Can.ofCollection(actionsInOrder)));
    	});
    }

    /**
     * @param introspectionContextProvider keeps track of the causal chain of introspection requests
     */
    void introspectUpTo(final IntrospectionState upTo, final Supplier<String> introspectionContextProvider) {
        if(!isLessThan(upTo))
			return; // optimization

        if(log.isDebugEnabled()) {
            log.debug("introspectingUpTo: {}, {}", spec.getFullIdentifier(), upTo);
        }

        switch (introspectionState) {
            case NOT_INTROSPECTED->{
                if(isLessThan(upTo)) {
                    introspectType();
                }
                if(isLessThan(upTo)) {
                    introspectFully();
                    spec.specLoaderInternal().validateLater(spec, introspectionContextProvider);
                }
            }
            case TYPE_BEING_INTROSPECTED->{} // nothing to do (interim state during introspectType)
            case TYPE_INTROSPECTED->{
                if(isLessThan(upTo)) {
                    introspectFully();
                    spec.specLoaderInternal().validateLater(spec, introspectionContextProvider);
                }
            }
            case MEMBERS_BEING_INTROSPECTED->{}// nothing to do (interim state during introspect fully)
            case FULLY_INTROSPECTED->{}// nothing to do ... all done
        }
    }

    private boolean isLessThan(final IntrospectionState upTo) {
        return introspectionState.isLessThan(upTo);
    }

    private void introspectType() {
        // set to avoid infinite loops
        this.introspectionState = IntrospectionState.TYPE_BEING_INTROSPECTED;
        introspectTypeHierarchy();
        spec.invalidateCachedFacets();
        this.introspectionState = IntrospectionState.TYPE_INTROSPECTED;
    }

    private void introspectTypeHierarchy() {
        spec.facetedMethodsBuilder.introspectClass();

        // name
        spec.addNamedFacetIfRequired();

        // go no further if a value
        if(spec.isValue()) {
            if (log.isDebugEnabled()) {
                log.debug("skipping type hierarchy introspection for value type {}", spec.getFullIdentifier());
            }
            return;
        }

        spec.loadSpecOfSuperclass(spec.getCorrespondingClass().getSuperclass());
        spec.loadSpecOfInterfaces(spec.getCorrespondingClass().getInterfaces());
    }

    private void introspectMembers() {

        // yet this logic does not skip UNKNONW
        if(spec.getBeanSort().isCollection()
                || spec.getBeanSort().isVetoed()
                || spec.isValue()) {
            if (log.isDebugEnabled()) {
                log.debug("skipping full introspection for {} type {}", spec.getBeanSort(), spec.getFullIdentifier());
            }
            return;
        }

        // create associations and actions
        this.computedMembers = new ComputedMembers(
    		Stream.concat(
				createAssociations(spec.facetedMethodsBuilder),
				createMixedInAssociations()), 
    		Stream.concat(
				createActions(spec.facetedMethodsBuilder),
				createMixedInActions()));

        spec.replaceMembers(computedMembers);

        spec.postProcessor.postProcess(spec);
        spec.invalidateCachedFacets();
    }

    private void introspectFully() {
        // set to avoid infinite loops
        this.introspectionState = IntrospectionState.MEMBERS_BEING_INTROSPECTED;
        introspectMembers();
        this.introspectionState = IntrospectionState.FULLY_INTROSPECTED;
        // make sure we've loaded the facets from layout.xml also.
        Facets.gridPreload(spec, null);
    }

    // -- ASSOC CREATION

    private Stream<ObjectAssociation> createAssociations(final FacetedMethodsBuilder facetedMethodsBuilder) {
        return facetedMethodsBuilder.getAssociationFacetedMethods()
                .stream()
                .map(this::createAssociation)
                .filter(_NullSafe::isPresent);
    }

    private ObjectAssociation createAssociation(final FacetedMethod facetMethod) {
        if (facetMethod.featureType().isCollection())
			return OneToManyAssociationDefault.forMethod(facetMethod);
		else if (facetMethod.featureType().isProperty())
			return OneToOneAssociationDefault.forMethod(facetMethod);
		else
			return null;
    }

    // -- ACTION CREATION

    private Stream<ObjectAction> createActions(final FacetedMethodsBuilder facetedMethodsBuilder) {
        return facetedMethodsBuilder.getActionFacetedMethods()
                .stream()
                .map(this::createAction)
                .filter(_NullSafe::isPresent);
    }

    private ObjectAction createAction(final FacetedMethod facetedMethod) {
        if (facetedMethod.featureType().isAction()) {
            /* Assuming, that facetedMethod was already populated with ContributingFacet,
             * we copy the mixin-sort information from the FacetedMethod to the MixinFacet
             * that is held by the mixin's type spec. */
            spec.mixinFacet()
	            .flatMap(mixinFacet->_Casts.castTo(MixinFacetAbstract.class, mixinFacet))
	            .ifPresent(mixinFacetAbstract->
	                mixinFacetAbstract.initMixinSortFrom(facetedMethod));

            return spec.isMixin()
                ? ObjectActionDefault.forMixinMain(facetedMethod)
                : ObjectActionDefault.forMethod(facetedMethod);
        } else
			return null;
    }

    // -- MIXED IN MEMBERS

    /**
     * Creates all mixed in properties and collections for this spec.
     */
    private Stream<ObjectAssociation> createMixedInAssociations() {
    	if(true) return Stream.empty(); //FIXME 
        if (spec.isInjectable() || spec.isValue())
			return Stream.empty();
        return spec.getCausewayBeanTypeRegistry().streamMixinTypes()
                .flatMap(this::createMixedInAssociation);
    }

    private Stream<ObjectAssociation> createMixedInAssociation(final Class<?> mixinType) {
        var mixinSpec = spec.specLoaderInternal().loadSpecification(mixinType,
                IntrospectionRequest.FULL);
        if (mixinSpec == null
                || mixinSpec == spec)
			return Stream.empty();
        var mixinFacet = mixinSpec.mixinFacet().orElse(null);
        if(mixinFacet == null)
			// this shouldn't happen; to be covered by meta-model validation later
            return Stream.empty();
        if(!mixinFacet.isMixinFor(spec.getCorrespondingClass()))
			return Stream.empty();
        var mixinMethodName = mixinFacet.getMainMethodName();

        return mixinSpec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED)
	        .filter(_SpecPredicates::isMixedInAssociation)
	        .map(ObjectActionDefault.class::cast)
	        .map(_MixedInMemberFactory.mixedInAssociation(spec, mixinSpec, mixinMethodName));
    }

    // -- mixin actions
    /**
     * Creates all mixed in actions for this spec.
     */
    private Stream<ObjectActionMixedIn> createMixedInActions() {
    	if(true) return Stream.empty(); //FIXME
        return spec.getCausewayBeanTypeRegistry().streamMixinTypes()
            .flatMap(this::createMixedInAction);
    }

    private Stream<ObjectActionMixedIn> createMixedInAction(final Class<?> mixinType) {
        var mixinSpec = spec.specLoaderInternal().loadSpecification(mixinType,
                IntrospectionRequest.FULL);
        if (mixinSpec == null
                || mixinSpec == spec)
			return Stream.empty();
        var mixinFacet = mixinSpec.mixinFacet().orElse(null);
        if(mixinFacet == null)
			// this shouldn't happen; to be covered by meta-model validation later
            return Stream.empty();
        if(!mixinFacet.isMixinFor(spec.getCorrespondingClass()))
			return Stream.empty();
        // don't mixin Object_ mixins to domain services
        if(spec.getBeanSort().isManagedBeanContributing()
                && mixinFacet.isMixinFor(java.lang.Object.class))
			return Stream.empty();

        var mixinMethodName = mixinFacet.getMainMethodName();

        return mixinSpec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED)
	        // value types only support constructor mixins
	        .filter(this::whenIsValueThenIsAlsoConstructorMixin)
	        .filter(_SpecPredicates::isMixedInAction)
	        .map(ObjectActionDefault.class::cast)
	        .map(_MixedInMemberFactory.mixedInAction(spec, mixinSpec, mixinMethodName));
    }

    /**
     * Whether the mixin's main method returns an instance of type equal to the mixee's type.
     * <p>
     * Introduced to support constructor mixins for value-types and
     * also to support associated <i>Actions</i> for <i>Action Parameters</i>.
     */
    private boolean whenIsValueThenIsAlsoConstructorMixin(final ObjectAction act) {
        return spec.getBeanSort().isValue()
                ? Objects.equals(spec, act.getReturnType())
                : true;
    }

    // --

    /**
     * one-shot: must be no-op, if already created
     * @return
     */
    private List<ObjectAction> createMixedInActionsAndResort(final List<ObjectAction> regularActions) {
        var include = spec.isEntityOrViewModelOrAbstract()
                || spec.getBeanSort().isManagedBeanContributing()
                // in support of composite value-type constructor mixins
                || spec.getBeanSort().isValue();
        if(!include)
			return regularActions;
        var mixedInActions = createMixedInActions()
                .collect(Collectors.toList());
        if(mixedInActions.isEmpty())
        	return regularActions; // nothing to do (this spec has no mixed-in actions, regular actions have already been added)

        // note: we are doing this before any member sorting
        _MemberIdClashReporting.flagAnyMemberIdClashes(spec, regularActions, mixedInActions);

        return _MemberSortingUtils.sortActionsIntoList(Stream.concat(
                regularActions.stream(),
                mixedInActions.stream()));
    }

    /**
     * one-shot: must be no-op, if already created
     */
    private List<ObjectAssociation> createMixedInAssociationsAndResort(final List<ObjectAssociation> regularAssociations) {
        if(!spec.isEntityOrViewModelOrAbstract())
        	return regularAssociations;
        var mixedInAssociations = createMixedInAssociations()
                .collect(Collectors.toList());
        if(mixedInAssociations.isEmpty())
        	return regularAssociations; // nothing to do (this spec has no mixed-in associations, regular associations have already been added)

        // note: we are doing this before any member sorting
        _MemberIdClashReporting.flagAnyMemberIdClashes(spec, regularAssociations, mixedInAssociations);

        return _MemberSortingUtils.sortAssociationsIntoList(Stream.concat(
                regularAssociations.stream(),
                mixedInAssociations.stream()));
    }


//    private Map<ResolvedMethod, ObjectMember> catalogueMembers() {
//        var membersByMethod = _Maps.<ResolvedMethod, ObjectMember>newHashMap();
//        cataloguePropertiesAndCollections(membersByMethod::put);
//        catalogueActions(membersByMethod::put);
//        return membersByMethod;
//    }
//
//    private void cataloguePropertiesAndCollections(final BiConsumer<ResolvedMethod, ObjectMember> onMember) {
//        streamDeclaredAssociations(MixedIn.EXCLUDED)
//        .forEach(field->
//            field.streamFacets(ImperativeFacet.class)
//                .map(ImperativeFacet::getMethods)
//                .flatMap(Can::stream)
//                .map(MethodFacade::asMethodElseFail) // expected regular
//                .peek(method->_Reflect.guardAgainstSynthetic(method.method())) // expected non-synthetic
//                .forEach(imperativeFacetMethod->onMember.accept(imperativeFacetMethod, field)));
//    }
//
//    private void catalogueActions(final BiConsumer<ResolvedMethod, ObjectMember> onMember) {
//        streamDeclaredActions(MixedIn.INCLUDED)
//        .forEach(userAction->
//            userAction.streamFacets(ImperativeFacet.class)
//                .map(ImperativeFacet::getMethods)
//                .flatMap(Can::stream)
//                .map(MethodFacade::asMethodForIntrospection)
//                .peek(method->_Reflect.guardAgainstSynthetic(method.method())) // expected non-synthetic
//                .forEach(imperativeFacetMethod->
//                    onMember.accept(imperativeFacetMethod, userAction)));
//    }

}