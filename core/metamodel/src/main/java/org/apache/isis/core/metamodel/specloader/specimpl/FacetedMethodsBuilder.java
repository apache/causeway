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

package org.apache.isis.core.metamodel.specloader.specimpl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.applib.exceptions.unrecoverable.MetaModelException;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.commons.CanBeVoid;
import org.apache.isis.core.metamodel.commons.MethodUtil;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.object.facets.FacetsFacet;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.typeextract.TypeExtractor;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FacetedMethodsBuilder {

    private static final String GET_PREFIX = "get";
    private static final String IS_PREFIX = "is";

    /* thread-safety ... make sure every methodsRemaining access is synchronized! */
    private static final class FacetedMethodsMethodRemover implements MethodRemover {

        private final Set<Method> methodsRemaining;

        private FacetedMethodsMethodRemover(final Class<?> introspectedClass, Method[] methods) {
            this.methodsRemaining = Stream.of(methods)
                    .filter(_NullSafe::isPresent)
                    .collect(Collectors.toCollection(_Sets::newConcurrentHashSet));
        }

        @Override
        public void removeMethods(Predicate<Method> removeIf, Consumer<Method> onRemoval) {
            methodsRemaining.removeIf(method -> {
                val doRemove = removeIf.test(method);
                if(doRemove) {
                    onRemoval.accept(method);
                }
                return doRemove;
            });
        }

        @Override
        public void removeMethod(Method method) {
            if(method==null) {
                return;
            }
            methodsRemaining.remove(method);
        }

        Stream<Method> streamRemaining() {
            return methodsRemaining.stream();
        }

        @Override
        public Can<Method> snapshot() {
            return Can.ofCollection(methodsRemaining);
        }

    }

    private final ObjectSpecificationAbstract inspectedTypeSpec;

    private final Class<?> introspectedClass;
   // private final Set<Method> methodsRemaining;

    private List<FacetedMethod> associationFacetMethods;
    private List<FacetedMethod> actionFacetedMethods;

    private final FacetedMethodsMethodRemover methodRemover;

    private final FacetProcessor facetProcessor;

    private final ClassSubstitutorRegistry classSubstitutorRegistry;

    private final SpecificationLoader specificationLoader;


    private final boolean explicitAnnotationsForActions;

    // ////////////////////////////////////////////////////////////////////////////
    // Constructor & finalize
    // ////////////////////////////////////////////////////////////////////////////

    public FacetedMethodsBuilder(
            final ObjectSpecificationAbstract inspectedTypeSpec,
            final FacetProcessor facetProcessor,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {

        if (log.isDebugEnabled()) {
            log.debug("creating JavaIntrospector for {}", inspectedTypeSpec.getFullIdentifier());
        }

        this.facetProcessor = facetProcessor;
        this.classSubstitutorRegistry = classSubstitutorRegistry;

        val mmContext = inspectedTypeSpec.getMetaModelContext();

        this.inspectedTypeSpec = inspectedTypeSpec;
        this.introspectedClass = inspectedTypeSpec.getCorrespondingClass();

        val methodsRemaining = introspectedClass.getMethods();
        this.methodRemover = new FacetedMethodsMethodRemover(introspectedClass, methodsRemaining);

        this.specificationLoader = mmContext.getSpecificationLoader();

        val isisConfiguration = mmContext.getConfiguration();

        this.explicitAnnotationsForActions = isisConfiguration.getApplib().getAnnotation().getAction().isExplicit();

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
        getFacetProcessor().process(introspectedClass, methodRemover, inspectedTypeSpec);

        // if this class has additional facets (as per @Facets), then process
        // them.
        final FacetsFacet facetsFacet = inspectedTypeSpec.getFacet(FacetsFacet.class);
        if (facetsFacet != null) {
            final Class<? extends FacetFactory>[] facetFactories = facetsFacet.facetFactories();
            for (final Class<? extends FacetFactory> facetFactorie : facetFactories) {
                FacetFactory facetFactory;
                try {
                    facetFactory = facetFactorie.newInstance();
                } catch (final InstantiationException | IllegalAccessException e) {
                    throw new UnrecoverableException(e);
                }
                getFacetProcessor().injectDependenciesInto(facetFactory);
                facetFactory.process(new ProcessClassContext(introspectedClass, methodRemover, inspectedTypeSpec));
            }
        }

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
            log.debug("introspecting {}: properties and collections", getClassName());
        }

        val specLoader = getSpecificationLoader();

        val associationCandidateMethods = new HashSet<Method>();


        getFacetProcessor().findAssociationCandidateAccessors(
                    methodRemover.streamRemaining(),
                    associationCandidateMethods::add);


        // Ensure all return types are known

        TypeExtractor.streamMethodReturn(associationCandidateMethods)
        .filter(typeToLoad->typeToLoad!=introspectedClass)
        .forEach(typeToLoad->specLoader.loadSpecification(typeToLoad, IntrospectionState.TYPE_INTROSPECTED));

        // now create FacetedMethods for collections and for properties
        val associationFacetedMethods = _Lists.<FacetedMethod>newArrayList();

        findAndRemoveCollectionAccessorsAndCreateCorrespondingFacetedMethods(associationFacetedMethods::add);
        findAndRemovePropertyAccessorsAndCreateCorrespondingFacetedMethods(associationFacetedMethods::add);

        return Collections.unmodifiableList(associationFacetedMethods);
    }

    private void findAndRemoveCollectionAccessorsAndCreateCorrespondingFacetedMethods(Consumer<FacetedMethod> onNewAssociationPeer) {
        val collectionAccessors = _Lists.<Method>newArrayList();
        getFacetProcessor().findAndRemoveCollectionAccessors(methodRemover, collectionAccessors);
        createCollectionFacetedMethodsFromAccessors(collectionAccessors, onNewAssociationPeer);
    }

    /**
     * Since the value properties and collections have already been processed,
     * this will pick up the remaining reference properties.
     */
    private void findAndRemovePropertyAccessorsAndCreateCorrespondingFacetedMethods(Consumer<FacetedMethod> onNewField) {
        val propertyAccessors = _Lists.<Method>newArrayList();
        getFacetProcessor().findAndRemovePropertyAccessors(methodRemover, propertyAccessors);

        findAndRemovePrefixedNonVoidMethods(GET_PREFIX, Object.class, 0, propertyAccessors::add);
        findAndRemovePrefixedNonVoidMethods(IS_PREFIX, Boolean.class, 0, propertyAccessors::add);

        createPropertyFacetedMethodsFromAccessors(propertyAccessors, onNewField);
    }

    private void createCollectionFacetedMethodsFromAccessors(
            final List<Method> accessorMethods,
            final Consumer<FacetedMethod> onNewFacetMethod) {

        for (final Method accessorMethod : accessorMethods) {
            if (log.isDebugEnabled()) {
                log.debug("  identified accessor method representing collection: {}", accessorMethod);
            }

            // create property and add facets
            val facetedMethod = FacetedMethod.createForCollection(introspectedClass, accessorMethod);
            getFacetProcessor().process(
                    introspectedClass,
                    accessorMethod,
                    methodRemover,
                    facetedMethod,
                    FeatureType.COLLECTION,
                    isMixinMain(accessorMethod));

            // figure out what the type is
            Class<?> elementType = Object.class;
            final TypeOfFacet typeOfFacet = facetedMethod.getFacet(TypeOfFacet.class);
            if (typeOfFacet != null) {
                elementType = typeOfFacet.value();
            }
            facetedMethod.setType(elementType);

            // skip if class substitutor says so.
            if (classSubstitutorRegistry.getSubstitution(elementType).isNeverIntrospect()) {
                continue;
            }

            onNewFacetMethod.accept(facetedMethod);
        }
    }

    private void createPropertyFacetedMethodsFromAccessors(
            final List<Method> accessorMethods,
            final Consumer<FacetedMethod> onNewFacetedMethod) throws MetaModelException {

        for (final Method accessorMethod : accessorMethods) {
            log.debug("  identified accessor method representing property: {}", accessorMethod);

            final Class<?> returnType = accessorMethod.getReturnType();

            // skip if class strategy says so.
            if (classSubstitutorRegistry.getSubstitution(returnType).isNeverIntrospect()) {
                continue;
            }

            // create a 1:1 association peer
            val facetedMethod = FacetedMethod.createForProperty(introspectedClass, accessorMethod);

            // process facets for the 1:1 association (eg. contributed properties)
            getFacetProcessor().process(
                    introspectedClass,
                    accessorMethod,
                    methodRemover,
                    facetedMethod,
                    FeatureType.PROPERTY,
                    isMixinMain(accessorMethod));

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
            log.debug("introspecting {}: actions", getClassName());
        }
        val actionFacetedMethods = _Lists.<FacetedMethod>newArrayList();
        collectActionFacetedMethods(actionFacetedMethods::add);
        return actionFacetedMethods;
    }

    private void collectActionFacetedMethods(
            final Consumer<FacetedMethod> onActionFacetedMethod) {

        if (log.isDebugEnabled()) {
            log.debug("  looking for action methods");
        }

        methodRemover.removeMethods(method->{
            val actionPeer = findActionFacetedMethod(method);
            if (actionPeer != null) {
                onActionFacetedMethod.accept(actionPeer);
                return true;
            }
            return false;
        });

    }

    private FacetedMethod findActionFacetedMethod(
            final Method actionMethod) {

        if (!representsAction(actionMethod)) {
            return null;
        }

        // build action
        return createActionFacetedMethod(actionMethod);
    }

    private FacetedMethod createActionFacetedMethod(
            final Method actionMethod) {

        if (!isAllParamTypesValid(actionMethod)) {
            return null;
        }

        final FacetedMethod action = FacetedMethod.createForAction(introspectedClass, actionMethod);

        // process facets on the action & parameters
        getFacetProcessor().process(
                introspectedClass,
                actionMethod,
                methodRemover,
                action,
                FeatureType.ACTION,
                isMixinMain(actionMethod));

        final List<FacetedMethodParameter> actionParams = action.getParameters();
        for (int j = 0; j < actionParams.size(); j++) {
            getFacetProcessor().processParams(introspectedClass, actionMethod, j, methodRemover, actionParams.get(j));
        }

        return action;
    }

    private boolean isAllParamTypesValid(final Method actionMethod) {
        for (val paramType : actionMethod.getParameterTypes()) {
            val paramSpec = getSpecificationLoader().loadSpecification(paramType);
            if (paramSpec == null) {
                return false;
            }
        }
        return true;
    }

    private boolean representsAction(final Method actionMethod) {

        if (MethodUtil.isStatic(actionMethod)) {
            return false;
        }

        val hasActionAnnotation = _Annotations
                .findNearestAnnotation(actionMethod, Action.class)
                .isPresent();

        // just an optimization, not strictly required:
        // return false if both are true
        // 1. actionMethod has no @Action annotation
        // 2. actionMethod does not identify as a mixin's main method
        if(isExplicitActionAnnotationConfigured()) {

            // even though when @Action is mandatory for action methods,
            // mixins now can contribute methods,
            // that do not need to be annotated (see ISIS-1998)

            if(!hasActionAnnotation) {
                // omitting the @Action annotation at given method is only allowed, when the
                // type is a mixin, and the mixin's main method identifies as the given actionMethod
                val type = actionMethod.getDeclaringClass();

                //XXX for given type we do this for every method, could cache the result!
                val mixedInMethodName = _Annotations.findNearestAnnotation(type, DomainObject.class)
                .filter(domainObject->Nature.MIXIN.equals(domainObject.nature()))
                .map(DomainObject::mixinMethod)
                .orElse(null);

                if(mixedInMethodName!=null) {
                    // we have a mixin type
                    if(!Objects.equals(actionMethod.getName(), mixedInMethodName)) {
                        // the actionMethod does not identify as the mixin's main method
                        return false;
                    }
                }
            }

            // else fall through

        }

        val specLoader = getSpecificationLoader();

        val anyLoadedAsNull = TypeExtractor.streamMethodReturn(actionMethod)
        .map(typeToLoad->specLoader.loadSpecification(typeToLoad, IntrospectionState.TYPE_INTROSPECTED))
        .anyMatch(Objects::isNull);

        if (anyLoadedAsNull) {
            return false;
        }

        // ensure we can load specs for all the params
//don't!! has side effect of pulling in all param types
//even those that should be ignored by the metamodel
//        if (!loadParamSpecs(actionMethod)) {
//            return false;
//        }

        if(isMixinMain(actionMethod)) {
            // we are introspecting a mixin type, so accept this method for further processing
            log.debug("  identified mixin-main action {}", actionMethod);
            return true;
        }

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

        if(isExplicitActionAnnotationConfigured()) {
            // we have no @Action, so dismiss
            return false;
        }

        // we have a valid action candidate, so fall through
        log.debug("  identified action {}", actionMethod);
        return true;
    }


    private boolean isExplicitActionAnnotationConfigured() {
        return explicitAnnotationsForActions;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Helpers for finding and removing methods.
    // ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param prefix
     * @param returnType
     * @param paramCount
     * @param onRemoved - collecting parameter
     */
    private void findAndRemovePrefixedNonVoidMethods(
            final String prefix,
            final Class<?> returnType,
            final int paramCount,
            final Consumer<Method> onRemoved) {

        findAndRemovePrefixedMethods(prefix, returnType, CanBeVoid.FALSE, paramCount, onRemoved);
    }

    private void findAndRemovePrefixedMethods(
            final String prefix,
            final Class<?> returnType,
            final CanBeVoid canBeVoid,
            final int paramCount,
            Consumer<Method> onMatch) {

        val filter = MethodUtil.Predicates.prefixed(prefix, returnType, canBeVoid, paramCount);
        methodRemover.removeMethods(filter, onMatch);

    }

    /**
     * In case this inspected type is a mixin, returns whether given method can be identified
     * as this mixin's main method.
     *
     * @param method
     */
    private boolean isMixinMain(Method method) {
        val mixinFacet = inspectedTypeSpec.lookupNonFallbackFacet(MixinFacet.class)
                .orElse(null);
        if(mixinFacet==null) {
            return false;
        }
        if(inspectedTypeSpec.isLessThan(IntrospectionState.FULLY_INTROSPECTED)) {
            // members are not introspected yet, so make a guess
            return mixinFacet.isCandidateForMain(method);
        }
        val mixinMember = inspectedTypeSpec.getMixedInMember(inspectedTypeSpec);
        if(!mixinMember.isPresent()) {
            return false;
        }
        val actionMethod_ofMixinMember = ((ObjectActionMixedIn)mixinMember.get())
                .getFacetedMethod().getMethod();
        return method.equals(actionMethod_ofMixinMember);
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

    // ////////////////////////////////////////////////////////////////////////////
    // Dependencies
    // ////////////////////////////////////////////////////////////////////////////

    private SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    private FacetProcessor getFacetProcessor() {
        return facetProcessor;
    }

}
