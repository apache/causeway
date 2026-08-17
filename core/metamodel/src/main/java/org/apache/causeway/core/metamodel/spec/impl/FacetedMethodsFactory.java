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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.exceptions.unrecoverable.MetaModelException;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.HasFacetedMethod;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.impl.IntrospectionStateHandler.IntrospectionRequest;
import org.apache.causeway.core.metamodel.specloader.typeextract.TypeExtractor;
import org.jspecify.annotations.Nullable;

import lombok.extern.slf4j.Slf4j;

// has side-effects: calls specloader, potentially modifies the class cache
@Slf4j
record FacetedMethodsFactory(
	    ObjectSpecificationInternal internalSpec,
	    FacetProcessor facetProcessor,
	    ClassSubstitutorRegistry classSubstitutorRegistry,
	    MethodRemover methodRemover,
	    Function<Class<?>, ObjectSpecification> loadSpecificationTypeOnlyFunction,
	    ConsistencyContext consistencyContext)
implements
    HasMetaModelContext {

    FacetedMethodsFactory(
            final ObjectSpecificationInternal internalSpec,
            final SpecificationLoaderInternal specLoaderInternal,
            final FacetProcessor facetProcessor,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {
    	this(internalSpec,
    			facetProcessor,
    			classSubstitutorRegistry,
    			MethodRemover.createMethodRemover(internalSpec.correspondingClass(), internalSpec.introspectionPolicy()),
    			specLoaderInternal::loadSpecificationTypeOnly,
    			internalSpec.consistencyContext());
    }

    FacetedMethodsFactory {
    	if (log.isDebugEnabled()) {
            log.debug("creating {} for {}", this.getClass().getSimpleName(), internalSpec.fullIdentifier());
        }
    }

    Class<?> introspectedClass() {
    	return internalSpec.correspondingClass();
    }

    public void introspectClass() {
        if (log.isDebugEnabled()) {
            log.debug("introspecting {}: class-level details", introspectedClass().getName());
        }

        // process facets at object level
        // this will also remove some methods, such as the superclass methods.
        facetProcessor
        	.process(introspectedClass(), introspectionPolicy(), methodRemover, internalSpec, loadSpecificationTypeOnlyFunction);
    }

    /**
     * Returns a {@link List} of {@link FacetedMethod}s representing object
     * actions, lazily creating them first if required.
     */
    public List<FacetedMethod> createActionFacetedMethods() {
        if (log.isDebugEnabled()) {
            log.debug("introspecting(policy={}) {}: actions", introspectionPolicy(), introspectedClass().getName());
        }
        return methodRemover.pickMethods(
        		this::findActionFacetedMethod);
    }

    /**
     * Returns a {@link List} of {@link FacetedMethod}s representing object
     * actions, lazily creating them first if required.
     */
    public List<FacetedMethod> createAssociationFacetedMethods() {
        if (log.isDebugEnabled()) {
            log.debug("introspecting(policy={}) {}: properties and collections", introspectionPolicy(), introspectedClass().getName());
        }
        // optimization, not strictly required
        if(methodRemover instanceof MethodRemover.ConcurrentMethodRemover concurrentMethodRemover
        		&& concurrentMethodRemover.methodsRemaining().isEmpty())
			return List.of();

        var associationCandidateMethods = new HashSet<ResolvedMethod>();
        facetProcessor.findAssociationCandidateGetters(
                methodRemover.streamRemaining(),
                associationCandidateMethods::add);

        // Ensure all return types are known
        TypeExtractor.streamMethodReturn(associationCandidateMethods)
            .filter(typeToLoad->typeToLoad!=introspectedClass())
            .forEach(loadSpecificationTypeOnlyFunction::apply);

        // now create FacetedMethods for collections and for properties
        var associationFacetedMethods = new ArrayList<FacetedMethod>();
        facetProcessor.findAndRemoveCollectionAccessors(methodRemover)
        	.forEach(accessorMethod->createCollectionFacetedMethodForAccessor(accessorMethod, associationFacetedMethods::add));
        facetProcessor.findAndRemovePropertyAccessors(methodRemover)
        	.forEach(accessorMethod->createPropertyFacetedMethodForAccessor(accessorMethod, associationFacetedMethods::add));

        return Collections.unmodifiableList(associationFacetedMethods);
    }

    @Override
    public String toString() {
        return "%s[class=%s]".formatted(this.getClass().getSimpleName(), introspectedClass().getName());
    }

    // -- HELPER

    private void createCollectionFacetedMethodForAccessor(
            final ResolvedMethod accessorMethod,
            final Consumer<FacetedMethod> onNewFacetMethod) {
        if (log.isDebugEnabled()) {
            log.debug("  identified accessor method representing collection: {}", accessorMethod);
        }

        var accessorMethodFacade = _MethodFacades.regular(accessorMethod);

        // create property and add facets
        var facetedMethod = FacetedMethod.createForCollection(facetProcessor().getMetaModelContext(), introspectedClass(), accessorMethod);
        facetProcessor
            .process(
                    introspectedClass(),
                    introspectionPolicy(),
                    accessorMethodFacade,
                    methodRemover,
                    facetedMethod,
                    FeatureType.COLLECTION,
                    isMixinMain(accessorMethodFacade),
                    specLoaderInternal()::loadSpecificationTypeOnly);

        // figure out what the type is
        final Class<?> elementType = facetedMethod.lookupFacet(TypeOfFacet.class)
                .<Class<?>>map(typeOfFacet->typeOfFacet.value().elementType())
                .orElse(Object.class);

        // skip if class substitutor says so
        if (classSubstitutorRegistry.getSubstitution(elementType).isNeverIntrospect())
			return;

        onNewFacetMethod.accept(facetedMethod.withElementType(elementType));
    }

    private void createPropertyFacetedMethodForAccessor(
            final ResolvedMethod accessorMethod,
            final Consumer<FacetedMethod> onNewFacetedMethod) throws MetaModelException {
        log.debug("  identified accessor method representing property: {}", accessorMethod);

        final Class<?> returnType = accessorMethod.returnType();

        // skip if class strategy says so.
        if (classSubstitutorRegistry.getSubstitution(returnType).isNeverIntrospect())
			return;

        // create a 1:1 association peer
        var facetedMethod = FacetedMethod
                .createForProperty(getMetaModelContext(), introspectedClass(), accessorMethod);

        var accessorMethodFacade = _MethodFacades.regular(accessorMethod);

        // process facets for the 1:1 association (eg. contributed properties)
        facetProcessor
        	.process(
                introspectedClass(),
                introspectionPolicy(),
                accessorMethodFacade,
                methodRemover,
                facetedMethod,
                FeatureType.PROPERTY,
                isMixinMain(accessorMethodFacade),
                specLoaderInternal()::loadSpecificationTypeOnly);

        onNewFacetedMethod.accept(facetedMethod);
    }

    private Optional<@Nullable FacetedMethod> findActionFacetedMethod(final ResolvedMethod actionMethod) {
        if (!representsAction(actionMethod))
        	return Optional.empty();
        // build action
        return Optional.of(actionMethod)
            .map(this::createActionFacetedMethod)
            .filter(Objects::nonNull);
    }

    @Nullable
    private FacetedMethod createActionFacetedMethod(final ResolvedMethod actionMethod) {

        var actionMethodFacade = _MethodFacadeAutodetect.autodetect(actionMethod, internalSpec);
        if (!isAllParamTypesValid(actionMethodFacade)) return null;

        final FacetedMethod action = FacetedMethod
                .createForAction(getMetaModelContext(), introspectedClass(), actionMethodFacade);

        // process facets on the action & parameters
        facetProcessor
        	.process(
                introspectedClass(),
                introspectionPolicy(),
                actionMethodFacade,
                methodRemover,
                action,
                FeatureType.ACTION,
                isMixinMain(actionMethodFacade),
                specLoaderInternal()::loadSpecificationTypeOnly);

        action.parameters()
            .forEach(actionParam->
            	facetProcessor
                    .processParams(introspectedClass(), introspectionPolicy(), actionMethodFacade, methodRemover, actionParam));

        return action;
    }

    private boolean isAllParamTypesValid(final MethodFacade actionMethod) {
        for (var paramType : actionMethod.getParameterTypes()) {
            var paramSpec = specLoaderInternal().loadSpecificationTypeOnly(paramType);
            if (paramSpec == null) return false;
        }
        return true;
    }

    private boolean representsAction(final ResolvedMethod actionMethod) {

        //[CAUSEWAY-3556] if this throws, we have a framework bug (synthetic methods should no longer appear here)
        _Reflect.guardAgainstSynthetic(actionMethod.method());

        // ensure we can load returned element type; otherwise ignore method
        var anyLoadedAsNull = TypeExtractor.streamMethodReturn(actionMethod)
            .map(typeToLoad->specLoaderInternal().loadSpecification(typeToLoad, IntrospectionRequest.TYPE_ONLY))
            .anyMatch(Objects::isNull);
        if (anyLoadedAsNull)
            return false;

        if(isMixinMain(actionMethod)) {
            // we are introspecting a mixin type and its main method,
            // so accept this method for further processing
            log.debug("  identified mixin-main action {}", actionMethod);
            return true;
        }

        var hasActionAnnotation = _Annotations
                .isPresent(actionMethod.method(), Action.class);
        if(hasActionAnnotation) {
            log.debug("  identified action {}", actionMethod);
            return true;
        }

        // exclude those that have eg. reserved prefixes
        if (facetProcessor.recognizes(actionMethod)) {
            // this is a potential orphan candidate, collect these, than use when validating
        	consistencyContext.potentialOrphans().add(actionMethod);
            return false;
        }

        if(introspectionPolicy().getMemberAnnotationPolicy().isMemberAnnotationsRequired()) {
            // we have no @Action, so dismiss
            log.debug("  dismissing non-action method {}", actionMethod);
            return false;
        }

        // we have a valid action candidate, so accept
        log.debug("  identified action {}", actionMethod);
        return true;
    }

    // -- Helpers for finding and removing methods.

    private boolean isMixinMain(final MethodFacade methodFacade) {
        return isMixinMain(methodFacade.asMethodForIntrospection());
    }

    /**
     * In case this inspected type is a mixin, returns whether given method can be identified
     * as this mixin's main method.
     *
     * @param method
     */
    private boolean isMixinMain(final ResolvedMethod method) {
        var mixinFacet = internalSpec.lookupNonFallbackFacet(MixinFacet.class)
                .orElse(null);
        if(mixinFacet==null) return false;

    	if(!internalSpec.isFullyIntrospected())
    		// members are not introspected yet, so make a guess
    		return mixinFacet.isCandidateForMain(method);

        return internalSpec
                .lookupMixedInAction(internalSpec)
                .map(HasFacetedMethod.class::cast)
                .map(HasFacetedMethod::getFacetedMethod)
                .map(FacetedMethod::methodFacade)
                .map(MethodFacade::asMethodForIntrospection)
                .map(method::equals)
                .orElse(false);
    }

    private IntrospectionPolicy introspectionPolicy() {
        return internalSpec.introspectionPolicy();
    }

    SpecificationLoaderInternal specLoaderInternal() {
    	return (SpecificationLoaderInternal) internalSpec().getSpecificationLoader();
    }

}
