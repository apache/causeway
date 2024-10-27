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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.exceptions.unrecoverable.MetaModelException;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.metamodel.commons.MethodUtil;
import org.apache.causeway.core.metamodel.commons.ToString;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.IntrospectionState;
import org.apache.causeway.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.causeway.core.metamodel.specloader.typeextract.TypeExtractor;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FacetedMethodsBuilder
implements HasMetaModelContext {

    /* thread-safety ... make sure every methodsRemaining access is synchronized! */
    private static final class ConcurrentMethodRemover implements MethodRemover {

        private final Set<ResolvedMethod> methodsRemaining;

        private ConcurrentMethodRemover(final Class<?> introspectedClass, final Stream<ResolvedMethod> methodStream) {
            this.methodsRemaining = methodStream
                    .collect(Collectors.toCollection(_Sets::newConcurrentHashSet));
        }

        @Override
        public void removeMethods(final Predicate<ResolvedMethod> removeIf, final Consumer<ResolvedMethod> onRemoval) {
            methodsRemaining.removeIf(method -> {
                var doRemove = removeIf.test(method);
                if(doRemove) {
                    onRemoval.accept(method);
                }
                return doRemove;
            });
        }

        @Override
        public void removeMethod(final ResolvedMethod method) {
            if(method==null) {
                return;
            }
            methodsRemaining.remove(method);
        }

        Stream<ResolvedMethod> streamRemaining() {
            return methodsRemaining.stream();
        }

        @Override
        public Can<ResolvedMethod> snapshotMethodsRemaining() {
            return Can.ofCollection(methodsRemaining);
        }

    }

    private final ObjectSpecificationAbstract inspectedTypeSpec;

    @Getter private final Class<?> introspectedClass;

    private List<FacetedMethod> associationFacetMethods;
    private List<FacetedMethod> actionFacetedMethods;

    private final ConcurrentMethodRemover methodRemover;

    @Getter private final FacetProcessor facetProcessor;

    private final ClassSubstitutorRegistry classSubstitutorRegistry;

    // -- CONSTRUCTOR

    public FacetedMethodsBuilder(
            final ObjectSpecificationAbstract inspectedTypeSpec,
            final FacetProcessor facetProcessor,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {

        if (log.isDebugEnabled()) {
            log.debug("creating JavaIntrospector for {}", inspectedTypeSpec.getFullIdentifier());
        }

        this.facetProcessor = facetProcessor;
        this.classSubstitutorRegistry = classSubstitutorRegistry;
        this.inspectedTypeSpec = inspectedTypeSpec;
        this.introspectedClass = inspectedTypeSpec.getCorrespondingClass();

        var classCache = _ClassCache.getInstance();
        var methodsRemaining = introspectionPolicy().getEncapsulationPolicy().isEncapsulatedMembersSupported()
                ? classCache.streamResolvedMethods(introspectedClass)
                : classCache.streamPublicMethods(introspectedClass);
        this.methodRemover = new ConcurrentMethodRemover(introspectedClass, methodsRemaining);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Class and stuff immediately derived from class
    // ////////////////////////////////////////////////////////////////////////////

    private String getClassName() {
        return introspectedClass.getName();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // introspect class
    // ////////////////////////////////////////////////////////////////////////////

    public void introspectClass() {
        if (log.isDebugEnabled()) {
            log.debug("introspecting {}: class-level details", getClassName());
        }

        // process facets at object level
        // this will also remove some methods, such as the superclass methods.
        getFacetProcessor()
        .process(introspectedClass, introspectionPolicy(), methodRemover, inspectedTypeSpec);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // introspect associations
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a {@link List} of {@link FacetedMethod}s representing object
     * actions, lazily creating them first if required.
     */
    public List<FacetedMethod> getAssociationFacetedMethods() {
        if (associationFacetMethods == null) {
            associationFacetMethods = createAssociationFacetedMethods();
        }
        return associationFacetMethods;
    }

    private List<FacetedMethod> createAssociationFacetedMethods() {
        if (log.isDebugEnabled()) {
            log.debug("introspecting(policy={}) {}: properties and collections", introspectionPolicy(), getClassName());
        }

        var specLoader = getSpecificationLoader();

        var associationCandidateMethods = new HashSet<ResolvedMethod>();

        getFacetProcessor()
        .findAssociationCandidateGetters(
                    methodRemover.streamRemaining(),
                    associationCandidateMethods::add);

        // Ensure all return types are known

        TypeExtractor.streamMethodReturn(associationCandidateMethods)
        .filter(typeToLoad->typeToLoad!=introspectedClass)
        .forEach(typeToLoad->specLoader.loadSpecification(typeToLoad, IntrospectionState.TYPE_INTROSPECTED));

        // now create FacetedMethods for collections and for properties
        var associationFacetedMethods = _Lists.<FacetedMethod>newArrayList();

        findAndRemoveCollectionAccessorsAndCreateCorrespondingFacetedMethods(associationFacetedMethods::add);
        findAndRemovePropertyAccessorsAndCreateCorrespondingFacetedMethods(associationFacetedMethods::add);

        return Collections.unmodifiableList(associationFacetedMethods);
    }

    private void findAndRemoveCollectionAccessorsAndCreateCorrespondingFacetedMethods(
            final Consumer<FacetedMethod> onNewAssociationPeer) {
        var collectionAccessors = _Lists.<ResolvedMethod>newArrayList();
        getFacetProcessor().findAndRemoveCollectionAccessors(methodRemover, collectionAccessors);
        createCollectionFacetedMethodsFromAccessors(
                getMetaModelContext(), collectionAccessors, onNewAssociationPeer);
    }

    /**
     * Since the value properties and collections have already been processed,
     * this will pick up the remaining reference properties.
     */
    private void findAndRemovePropertyAccessorsAndCreateCorrespondingFacetedMethods(final Consumer<FacetedMethod> onNewField) {
        var propertyAccessors = _Lists.<ResolvedMethod>newArrayList();
        getFacetProcessor().findAndRemovePropertyAccessors(methodRemover, propertyAccessors);

        methodRemover.removeMethods(MethodUtil.Predicates.nonBooleanGetter(Object.class), propertyAccessors::add);
        methodRemover.removeMethods(MethodUtil.Predicates.booleanGetter(), propertyAccessors::add);

        createPropertyFacetedMethodsFromAccessors(propertyAccessors, onNewField);
    }

    private void createCollectionFacetedMethodsFromAccessors(
            final MetaModelContext mmc,
            final List<ResolvedMethod> accessorMethods,
            final Consumer<FacetedMethod> onNewFacetMethod) {

        for (final ResolvedMethod accessorMethod : accessorMethods) {
            if (log.isDebugEnabled()) {
                log.debug("  identified accessor method representing collection: {}", accessorMethod);
            }

            var accessorMethodFacade = _MethodFacades.regular(accessorMethod);

            // create property and add facets
            var facetedMethod = FacetedMethod.createForCollection(mmc, introspectedClass, accessorMethod);
            getFacetProcessor()
            .process(
                    introspectedClass,
                    introspectionPolicy(),
                    accessorMethodFacade,
                    methodRemover,
                    facetedMethod,
                    FeatureType.COLLECTION,
                    isMixinMain(accessorMethodFacade));

            // figure out what the type is
            final Class<?> elementType = facetedMethod.lookupFacet(TypeOfFacet.class)
                    .<Class<?>>map(typeOfFacet->typeOfFacet.value().elementType())
                    .orElse(Object.class);

            // skip if class substitutor says so.
            if (classSubstitutorRegistry.getSubstitution(elementType).isNeverIntrospect()) {
                continue;
            }

            onNewFacetMethod.accept(facetedMethod.withType(elementType));
        }
    }

    private void createPropertyFacetedMethodsFromAccessors(
            final List<ResolvedMethod> accessorMethods,
            final Consumer<FacetedMethod> onNewFacetedMethod) throws MetaModelException {

        for (final ResolvedMethod accessorMethod : accessorMethods) {
            log.debug("  identified accessor method representing property: {}", accessorMethod);

            final Class<?> returnType = accessorMethod.returnType();

            // skip if class strategy says so.
            if (classSubstitutorRegistry.getSubstitution(returnType).isNeverIntrospect()) {
                continue;
            }

            // create a 1:1 association peer
            var facetedMethod = FacetedMethod
                    .createForProperty(getMetaModelContext(), introspectedClass, accessorMethod);

            var accessorMethodFacade = _MethodFacades.regular(accessorMethod);

            // process facets for the 1:1 association (eg. contributed properties)
            getFacetProcessor()
            .process(
                    introspectedClass,
                    introspectionPolicy(),
                    accessorMethodFacade,
                    methodRemover,
                    facetedMethod,
                    FeatureType.PROPERTY,
                    isMixinMain(accessorMethodFacade));

            onNewFacetedMethod.accept(facetedMethod);
        }
    }

    // ////////////////////////////////////////////////////////////////////////////
    // introspect actions
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a {@link List} of {@link FacetedMethod}s representing object
     * actions, lazily creating them first if required.
     */
    public List<FacetedMethod> getActionFacetedMethods() {
        if (actionFacetedMethods == null) {
            actionFacetedMethods = findActionFacetedMethods();
        }
        return actionFacetedMethods;
    }

    private List<FacetedMethod> findActionFacetedMethods() {
        if (log.isDebugEnabled()) {
            log.debug("introspecting(policy={}) {}: actions", introspectionPolicy(), getClassName());
        }
        var actionFacetedMethods = _Lists.<FacetedMethod>newArrayList();
        collectActionFacetedMethods(actionFacetedMethods::add);
        return actionFacetedMethods;
    }

    private void collectActionFacetedMethods(
            final Consumer<FacetedMethod> onActionFacetedMethod) {

        if (log.isDebugEnabled()) {
            log.debug("  looking for action methods");
        }

        methodRemover.removeMethods(method->{

            var actionPeer = findActionFacetedMethod(method);

            if (actionPeer != null) {
                onActionFacetedMethod.accept(actionPeer);
                return true;
            }
            return false;
        });

    }

    private FacetedMethod findActionFacetedMethod(
            final ResolvedMethod actionMethod) {

        if (!representsAction(actionMethod)) {
            return null;
        }

        // build action

        return Optional.of(actionMethod)
            .map(this::createActionFacetedMethod)
            .filter(_NullSafe::isPresent)
            .orElse(null);
    }

    @Nullable
    private FacetedMethod createActionFacetedMethod(
            final ResolvedMethod actionMethod) {

        var actionMethodFacade = _MethodFacadeAutodetect.autodetect(actionMethod, inspectedTypeSpec);

        if (!isAllParamTypesValid(actionMethodFacade)) {
            return null;
        }

        final FacetedMethod action = FacetedMethod
                .createForAction(getMetaModelContext(), introspectedClass, actionMethodFacade);

        // process facets on the action & parameters
        getFacetProcessor()
        .process(
                introspectedClass,
                introspectionPolicy(),
                actionMethodFacade,
                methodRemover,
                action,
                FeatureType.ACTION,
                isMixinMain(actionMethodFacade));

        action.getParameters()
        .forEach(actionParam->{
            getFacetProcessor()
            .processParams(introspectedClass, introspectionPolicy(), actionMethodFacade, methodRemover, actionParam);

        });

        return action;
    }

    private boolean isAllParamTypesValid(final MethodFacade actionMethod) {
        for (var paramType : actionMethod.getParameterTypes()) {
            var paramSpec = getSpecificationLoader().loadSpecification(paramType);
            if (paramSpec == null) {
                return false;
            }
        }
        return true;
    }

    private boolean representsAction(final ResolvedMethod actionMethod) {

        //[CAUSEWAY-3556] if this throws, we have a framework bug (synthetic methods should no longer appear here)
        _Reflect.guardAgainstSynthetic(actionMethod.method());

        // ensure we can load returned element type; otherwise ignore method
        var anyLoadedAsNull = TypeExtractor.streamMethodReturn(actionMethod)
        .map(typeToLoad->getSpecificationLoader().loadSpecification(typeToLoad, IntrospectionState.TYPE_INTROSPECTED))
        .anyMatch(Objects::isNull);
        if (anyLoadedAsNull) {
            return false;
        }

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
        if (getFacetProcessor().recognizes(actionMethod)) {
            // this is a potential orphan candidate, collect these, than use when validating
            inspectedTypeSpec.getPotentialOrphans().add(actionMethod);
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

    // ////////////////////////////////////////////////////////////////////////////
    // Helpers for finding and removing methods.
    // ////////////////////////////////////////////////////////////////////////////

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
        var mixinFacet = inspectedTypeSpec.lookupNonFallbackFacet(MixinFacet.class)
                .orElse(null);
        if(mixinFacet==null) {
            return false;
        }
        if(inspectedTypeSpec.isLessThan(IntrospectionState.FULLY_INTROSPECTED)) {
            // members are not introspected yet, so make a guess
            return mixinFacet.isCandidateForMain(method);
        }

        return inspectedTypeSpec
                .lookupMixedInAction(inspectedTypeSpec)
                .map(ObjectActionMixedIn::getFacetedMethod)
                .map(FacetedMethod::getMethod)
                .map(MethodFacade::asMethodForIntrospection)
                .map(method::equals)
                .orElse(false);
    }

    private IntrospectionPolicy introspectionPolicy() {
        return inspectedTypeSpec.getIntrospectionPolicy();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // toString
    // ////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", getClassName());
        return str.toString();
    }

    /**
     * exposed for debugging purposes
     */
    public Can<ResolvedMethod> snapshotMethodsRemaining() {
        return methodRemover.snapshotMethodsRemaining();
    }

}
