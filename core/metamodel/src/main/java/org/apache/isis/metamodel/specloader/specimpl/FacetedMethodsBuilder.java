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

package org.apache.isis.metamodel.specloader.specimpl;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.metamodel.commons.MethodUtil;
import org.apache.isis.metamodel.commons.ToString;
import org.apache.isis.metamodel.exceptions.MetaModelException;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MethodRemover;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.object.facets.FacetsFacet;
import org.apache.isis.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.metamodel.methodutils.MethodScope;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.metamodel.specloader.traverser.SpecificationTraverser;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FacetedMethodsBuilder {

    private static final String GET_PREFIX = "get";
    private static final String IS_PREFIX = "is";

    /* thread-safety ... make sure every methodsRemaining access is synchronized! */
    private static final class FacetedMethodsMethodRemover implements MethodRemover {

        //private final Object $lock = new Object();
        
        private final Set<Method> methodsRemaining;

        private FacetedMethodsMethodRemover(final Class<?> introspectedClass, Method[] methods) {
            this.methodsRemaining = Stream.of(methods)
                    .collect(Collectors.toCollection(_Sets::newConcurrentHashSet));
                    //.collect(Collectors.toCollection(_Sets::newHashSet));
        }

        @Override
        public void removeMethod(
                MethodScope methodScope,
                String methodName,
                Class<?> returnType,
                Class<?>[] parameterTypes) {
            
            //synchronized($lock) {
                MethodUtil.removeMethod(methodsRemaining, methodScope, methodName, returnType, parameterTypes);
            //}
        }

        @Override
        public void removeMethods(
                MethodScope methodScope,
                String prefix,
                Class<?> returnType,
                boolean canBeVoid,
                int paramCount,
                Consumer<Method> onRemoval) {
            
            //synchronized($lock) {
                MethodUtil.removeMethods(methodsRemaining, methodScope, prefix, returnType, canBeVoid, paramCount, onRemoval);
            //}
        }

        @Override
        public void removeMethod(Method method) {
            if(method==null) {
                return;
            }
            //synchronized($lock) {
                methodsRemaining.remove(method);    
            //}
        }

        void removeIf(Predicate<Method> matcher) {
            //synchronized($lock) {
                methodsRemaining.removeIf(matcher);
            //}
        }

        void acceptRemaining(Consumer<Set<Method>> consumer) {
            //synchronized($lock) {
                consumer.accept(methodsRemaining);
            //}
        }
        
    }

    private final ObjectSpecificationAbstract inspectedTypeSpec;

    private final Class<?> introspectedClass;
   // private final Set<Method> methodsRemaining;

    private List<FacetedMethod> associationFacetMethods;
    private List<FacetedMethod> actionFacetedMethods;

    private final FacetedMethodsMethodRemover methodRemover;

    private final FacetProcessor facetProcessor;

    private final SpecificationTraverser specificationTraverser = new SpecificationTraverser();
    private final ClassSubstitutor classSubstitutor = new ClassSubstitutor();

    private final SpecificationLoader specificationLoader;


    private final boolean explicitAnnotationsForActions;

    // ////////////////////////////////////////////////////////////////////////////
    // Constructor & finalize
    // ////////////////////////////////////////////////////////////////////////////

    public FacetedMethodsBuilder(
            final ObjectSpecificationAbstract inspectedTypeSpec,
            final FacetProcessor facetProcessor) {
        
        if (log.isDebugEnabled()) {
            log.debug("creating JavaIntrospector for {}", inspectedTypeSpec.getFullIdentifier());
        }
        
        val mmContext = inspectedTypeSpec.getMetaModelContext();

        this.inspectedTypeSpec = inspectedTypeSpec;
        this.introspectedClass = inspectedTypeSpec.getCorrespondingClass();
        
        val methodsRemaining = introspectedClass.getMethods();
        this.methodRemover = new FacetedMethodsMethodRemover(introspectedClass, methodsRemaining);

        this.facetProcessor = facetProcessor;
        this.specificationLoader = mmContext.getSpecificationLoader();

        val isisConfiguration = mmContext.getConfiguration();
        
        this.explicitAnnotationsForActions = isisConfiguration.getReflector().getExplicitAnnotations().isAction();

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


    public void introspectObjectSpecId() {
        if (log.isDebugEnabled()) {
            log.debug("introspecting {}: objectSpecId", getClassName());
        }
        getFacetProcessor().processObjectSpecId(introspectedClass, inspectedTypeSpec);
    }
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
                    throw new IsisException(e);
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
        
        val associationCandidateMethods = new HashSet<Method>();
        
        methodRemover.acceptRemaining(methodsRemaining->{
            getFacetProcessor()
            .findAssociationCandidateAccessors(methodsRemaining, associationCandidateMethods::add);
        });

        // Ensure all return types are known
        val typesToLoad = _Sets.<Class<?>>newHashSet();
        for (val method : associationCandidateMethods) {
            specificationTraverser.traverseTypes(method, typesToLoad::add);
        }
        typesToLoad.remove(introspectedClass);

        val specLoader = getSpecificationLoader();
        val upTo = IntrospectionState.TYPE_INTROSPECTED;
        typesToLoad.forEach(typeToLoad->specLoader.loadSpecification(typeToLoad, upTo));

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

        findAndRemovePrefixedNonVoidMethods(MethodScope.OBJECT, GET_PREFIX, Object.class, 0, propertyAccessors::add);
        findAndRemovePrefixedNonVoidMethods(MethodScope.OBJECT, IS_PREFIX, Boolean.class, 0, propertyAccessors::add);

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
            if (classSubstitutor.getClass(elementType) == null) {
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
            if (classSubstitutor.getClass(returnType) == null) {
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
            actionFacetedMethods = findActionFacetedMethods(MethodScope.OBJECT);
        }
        return actionFacetedMethods;
    }

    private List<FacetedMethod> findActionFacetedMethods(
            final MethodScope methodScope) {
        
        if (log.isDebugEnabled()) {
            log.debug("introspecting {}: actions", getClassName());
        }
        val actionFacetedMethods = _Lists.<FacetedMethod>newArrayList();
        collectActionFacetedMethods(actionFacetedMethods::add, methodScope);
        return actionFacetedMethods;
    }

    private void collectActionFacetedMethods(
            final Consumer<FacetedMethod> onActionFacetedMethod,
            final MethodScope methodScope) {

        if (log.isDebugEnabled()) {
            log.debug("  looking for action methods");
        }

        methodRemover.removeIf(method->{
            val actionPeer = findActionFacetedMethod(methodScope, method);
            if (actionPeer != null) {
                onActionFacetedMethod.accept(actionPeer);
                return true;
            }
            return false;
        });
        
    }

    private FacetedMethod findActionFacetedMethod(
            final MethodScope methodScope,
            final Method actionMethod) {

        if (!representsAction(actionMethod, methodScope)) {
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

    private boolean representsAction(
            final Method actionMethod,
            final MethodScope methodScope) {

        if (!MethodUtil.inScope(actionMethod, methodScope)) {
            return false;
        }

        val typesToLoad = _Sets.<Class<?>>newHashSet();
        specificationTraverser.traverseTypes(actionMethod, typesToLoad::add);

        val specLoader = getSpecificationLoader();

        val anyLoadedAsNull = typesToLoad.stream()
                .map(typeToLoad->specLoader.loadSpecification(typeToLoad, IntrospectionState.TYPE_INTROSPECTED))
                .anyMatch(spec->spec==null);

        if (anyLoadedAsNull) {
            return false;
        }

        // ensure we can load specs for all the params
        if (!loadParamSpecs(actionMethod)) {
            return false;
        }
        
        if(isMixinMain(actionMethod)) {
            // we are introspecting a mixin type, so accept this method for further processing
            log.debug("  identified mixin-main action {}", actionMethod);
            return true;
        } 
        
        if(explicitActionAnnotationConfigured()) {
            
            if(_Annotations.isPresent(actionMethod, Action.class)) {
                log.debug("  identified action {}", actionMethod);
                return true;
            }
            // we have no @Action, so dismiss
            return false;
            
        } 

        // exclude those that have eg. reserved prefixes
        if (getFacetProcessor().recognizes(actionMethod)) {
            return false;
        }
        // we have a valid action candidate, so fall through
        
        log.debug("  identified action {}", actionMethod);
        return true;
    }


    private boolean explicitActionAnnotationConfigured() {
        return explicitAnnotationsForActions;
    }

    private boolean loadParamSpecs(final Method actionMethod) {
        final Class<?>[] parameterTypes = actionMethod.getParameterTypes();
        return loadParamSpecs(parameterTypes);
    }

    private boolean loadParamSpecs(final Class<?>[] parameterTypes) {
        final int numParameters = parameterTypes.length;
        for (int j = 0; j < numParameters; j++) {
            final ObjectSpecification paramSpec = getSpecificationLoader().loadSpecification(parameterTypes[j]);
            if (paramSpec == null) {
                return false;
            }
        }
        return true;
    }


    // ////////////////////////////////////////////////////////////////////////////
    // Helpers for finding and removing methods.
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * As per {@link #findAndRemovePrefixedNonVoidMethods(org.apache.isis.metamodel.methodutils.MethodScope, String, Class, int, java.util.List)},
     * but appends to provided {@link List} (collecting parameter pattern).
     */
    private void findAndRemovePrefixedNonVoidMethods(
            final MethodScope methodScope,
            final String prefix,
            final Class<?> returnType,
            final int paramCount,
            final Consumer<Method> onRemoved) {
        
        findAndRemovePrefixedMethods(methodScope, prefix, returnType, false, paramCount, onRemoved);
    }

    /**
     * Searches for all methods matching the prefix and returns them, also
     * removing it from the {@link #methodsRemaining array of methods} if found.
     * @param onMatch 
     */
    private void findAndRemovePrefixedMethods(
            final MethodScope methodScope,
            final String prefix,
            final Class<?> returnType,
            final boolean canBeVoid,
            final int paramCount, 
            Consumer<Method> onMatch) {
        
        methodRemover.acceptRemaining(methodsRemaining->{
            MethodUtil.removeMethods(methodsRemaining, methodScope, prefix, returnType, canBeVoid, paramCount, onMatch);    
        });
        
    }
    
    /**
     * In case this inspected type is a mixin, returns whether given method can be identified 
     * as this mixin's main method. 
     *  
     * @param method
     */
    private boolean isMixinMain(Method method) {
        val mixinFacet = inspectedTypeSpec.getFacet(MixinFacet.class);
        if(mixinFacet==null || mixinFacet.isNoop()) {
            return false;
        }
        if(inspectedTypeSpec.isLessThan(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED)) {
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
