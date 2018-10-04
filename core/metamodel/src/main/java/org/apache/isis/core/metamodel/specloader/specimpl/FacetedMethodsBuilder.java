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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.commons.lang.MethodUtil;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.object.facets.FacetsFacet;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader.ReaderException;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader2;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.traverser.SpecificationTraverser;

public class FacetedMethodsBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(FacetedMethodsBuilder.class);

    private static final String GET_PREFIX = "get";
    private static final String IS_PREFIX = "is";

    private static final class FacetedMethodsMethodRemover implements MethodRemover {

        private final Class<?> introspectedClass;
        private final List<Method> methods;

        private FacetedMethodsMethodRemover(final Class<?> introspectedClass, final List<Method> methods) {
            this.introspectedClass = introspectedClass;
            this.methods = methods;
        }

        @Override
        public void removeMethod(
                final MethodScope methodScope,
                final String methodName,
                final Class<?> returnType,
                final Class<?>[] parameterTypes) {
            MethodUtil.removeMethod(methods, methodScope, methodName, returnType, parameterTypes);
        }

        @Override
        public List<Method> removeMethods(
                final MethodScope methodScope,
                final String prefix,
                final Class<?> returnType,
                final boolean canBeVoid,
                final int paramCount) {
            return MethodUtil.removeMethods(methods, methodScope, prefix, returnType, canBeVoid, paramCount);
        }

        @Override
        public void removeMethod(final Method method) {
            if (method == null) {
                return;
            }
            for (int i = 0; i < methods.size(); i++) {
                if (methods.get(i) == null) {
                    continue;
                }
                if (methods.get(i).equals(method)) {
                    methods.set(i, null);
                }
            }
        }

        @Override
        public void removeMethods(final List<Method> methodsToRemove) {
            for (int i = 0; i < methods.size(); i++) {
                if (methods.get(i) == null) {
                    continue;
                }
                for (final Method method : methodsToRemove) {
                    if (methods.get(i).equals(method)) {
                        methods.set(i, null);
                        break;
                    }
                }
            }
        }
    }

    private final ObjectSpecificationAbstract spec;

    private final Class<?> introspectedClass;
    private final List<Method> methods;

    private List<FacetedMethod> associationFacetMethods;
    private List<FacetedMethod> actionFacetedMethods;

    private final FacetedMethodsMethodRemover methodRemover;

    private final FacetProcessor facetProcessor;

    private final SpecificationTraverser specificationTraverser = new SpecificationTraverser();
    private final ClassSubstitutor classSubstitutor = new ClassSubstitutor();

    private final SpecificationLoader specificationLoader;

    private final List<LayoutMetadataReader> layoutMetadataReaders;
    private final Map<LayoutMetadataReader, LayoutMetadataReader2.Support> supportByReader;


    private final boolean explicitAnnotationsForActions;

    // ////////////////////////////////////////////////////////////////////////////
    // Constructor & finalize
    // ////////////////////////////////////////////////////////////////////////////

    public FacetedMethodsBuilder(
            final ObjectSpecificationAbstract spec,
            final FacetedMethodsBuilderContext facetedMethodsBuilderContext) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating JavaIntrospector for {}", spec.getFullIdentifier());
        }

        this.spec = spec;
        this.introspectedClass = spec.getCorrespondingClass();
        this.methods = Arrays.asList(introspectedClass.getMethods());

        this.methodRemover = new FacetedMethodsMethodRemover(introspectedClass, methods);

        this.facetProcessor = facetedMethodsBuilderContext.facetProcessor;
        this.specificationLoader = facetedMethodsBuilderContext.specificationLoader;

        this.layoutMetadataReaders = facetedMethodsBuilderContext.layoutMetadataReaders;
        this.explicitAnnotationsForActions = facetedMethodsBuilderContext.configService.getBoolean("isis.reflector.explicitAnnotations.action");

        this.supportByReader = Maps.newHashMap();
        for (LayoutMetadataReader reader : layoutMetadataReaders) {

            if(reader instanceof LayoutMetadataReader2) {
                final LayoutMetadataReader2 reader2 = (LayoutMetadataReader2) reader;
                final LayoutMetadataReader2.Support support = reader2.support();
                supportByReader.put(reader, support);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (LOG.isDebugEnabled()) {
            LOG.debug("finalizing inspector {}", this);
        }
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


    public Properties introspectClass() {
        if (LOG.isInfoEnabled()) {
            LOG.info("introspecting {}: class-level details", getClassName());
        }

        // process facets at object level
        // this will also remove some methods, such as the superclass methods.

        final Properties metadataProperties = readMetadataProperties(introspectedClass);

        getFacetProcessor().process(introspectedClass, metadataProperties, methodRemover, spec);

        // if this class has additional facets (as per @Facets), then process
        // them.
        final FacetsFacet facetsFacet = spec.getFacet(FacetsFacet.class);
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
                facetFactory.process(new ProcessClassContext(introspectedClass, metadataProperties, methodRemover, spec));
            }
        }
        return metadataProperties;
    }

    private Properties readMetadataProperties(final Class<?> domainClass) {
        for (final LayoutMetadataReader reader : layoutMetadataReaders) {
            try {
                // ignore JDK, Joda and Guava classes
                if(isPrimitiveOrJdkOrJodaOrGuava(domainClass)) {
                    continue;
                }

                // skip class if the reader doesn't support it
                final LayoutMetadataReader2.Support support = supportByReader.get(reader);
                if(support != null) {

                    if (!support.interfaces() && domainClass.isInterface()) {
                        continue;
                    }

                    if (!support.anonymous() && domainClass.isAnonymousClass()) {
                        continue;
                    }

                    if (!support.synthetic() && domainClass.isSynthetic()) {
                        continue;
                    }

                    if (!support.array() && domainClass.isArray()) {
                        continue;
                    }

                    if (!support.enums() && domainClass.isEnum()) {
                        continue;
                    }

                    if (!support.applibValueTypes() && domainClass.getName().startsWith("org.apache.isis.applib.value")) {
                        continue;
                    }

                    if(!support.services() &&
                       getSpecificationLoader().isServiceClass(domainClass)) {
                        continue;
                    }

                }

                Properties properties = reader.asProperties(domainClass);
                if(properties != null) {
                    return properties;
                }
            } catch(final ReaderException ignore) {
                // ignore... it is now the responsibility of the reader to LOG any exceptions
            }
        }
        return null;
    }

    private static boolean isPrimitiveOrJdkOrJodaOrGuava(final Class<?> cls) {
        if(cls.isPrimitive()) {
            return true;
        }
        final String className = cls.getName();
        return className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("org.joda") || className.startsWith("com.google.common");
    }

    // ////////////////////////////////////////////////////////////////////////////
    // introspect associations
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a {@link List} of {@link FacetedMethod}s representing object
     * actions, lazily creating them first if required.
     */
    public List<FacetedMethod> getAssociationFacetedMethods(Properties properties) {
        if (associationFacetMethods == null) {
            associationFacetMethods = createAssociationFacetedMethods(properties);
        }
        return associationFacetMethods;
    }

    private List<FacetedMethod> createAssociationFacetedMethods(Properties properties) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("introspecting {}: properties and collections", getClassName());
        }
        final Set<Method> associationCandidateMethods = getFacetProcessor().findAssociationCandidateAccessors(methods, new HashSet<Method>());

        // Ensure all return types are known
        final List<Class<?>> typesToLoad = Lists.newArrayList();
        for (final Method method : associationCandidateMethods) {
            specificationTraverser.traverseTypes(method, typesToLoad);
        }
        getSpecificationLoader().loadSpecifications(typesToLoad, introspectedClass);

        // now create FacetedMethods for collections and for properties
        final List<FacetedMethod> associationFacetedMethods = Lists.newArrayList();

        findAndRemoveCollectionAccessorsAndCreateCorrespondingFacetedMethods(associationFacetedMethods, properties);
        findAndRemovePropertyAccessorsAndCreateCorrespondingFacetedMethods(associationFacetedMethods, properties);

        return Collections.unmodifiableList(associationFacetedMethods);
    }

    private void findAndRemoveCollectionAccessorsAndCreateCorrespondingFacetedMethods(final List<FacetedMethod> associationPeers, Properties properties) {
        final List<Method> collectionAccessors = Lists.newArrayList();
        getFacetProcessor().findAndRemoveCollectionAccessors(methodRemover, collectionAccessors);
        createCollectionFacetedMethodsFromAccessors(collectionAccessors, associationPeers, properties);
    }

    /**
     * Since the value properties and collections have already been processed,
     * this will pick up the remaining reference properties.
     * @param properties TODO
     */
    private void findAndRemovePropertyAccessorsAndCreateCorrespondingFacetedMethods(final List<FacetedMethod> fields, Properties properties) {
        final List<Method> propertyAccessors = Lists.newArrayList();
        getFacetProcessor().findAndRemovePropertyAccessors(methodRemover, propertyAccessors);

        findAndRemovePrefixedNonVoidMethods(MethodScope.OBJECT, GET_PREFIX, Object.class, 0, propertyAccessors);
        findAndRemovePrefixedNonVoidMethods(MethodScope.OBJECT, IS_PREFIX, Boolean.class, 0, propertyAccessors);

        createPropertyFacetedMethodsFromAccessors(propertyAccessors, fields, properties);
    }

    private void createCollectionFacetedMethodsFromAccessors(final List<Method> accessorMethods, final List<FacetedMethod> facetMethodsToAppendto, Properties properties) {
        for (final Method accessorMethod : accessorMethods) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("  identified accessor method representing collection: {}", accessorMethod);
            }

            // create property and add facets
            final FacetedMethod facetedMethod = FacetedMethod.createForCollection(introspectedClass, accessorMethod);
            getFacetProcessor().process(introspectedClass, accessorMethod, methodRemover, facetedMethod, FeatureType.COLLECTION, properties);

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

            facetMethodsToAppendto.add(facetedMethod);
        }
    }

    private void createPropertyFacetedMethodsFromAccessors(final List<Method> accessorMethods, final List<FacetedMethod> facetedMethodsToAppendto, Properties properties) throws MetaModelException {

        for (final Method accessorMethod : accessorMethods) {
            LOG.debug("  identified accessor method representing property: {}", accessorMethod);

            final Class<?> returnType = accessorMethod.getReturnType();

            // skip if class strategy says so.
            if (classSubstitutor.getClass(returnType) == null) {
                continue;
            }

            // create a 1:1 association peer
            final FacetedMethod facetedMethod = FacetedMethod.createForProperty(introspectedClass, accessorMethod);

            // process facets for the 1:1 association
            getFacetProcessor().process(introspectedClass, accessorMethod, methodRemover, facetedMethod, FeatureType.PROPERTY, properties);

            facetedMethodsToAppendto.add(facetedMethod);
        }
    }

    // ////////////////////////////////////////////////////////////////////////////
    // introspect actions
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a {@link List} of {@link FacetedMethod}s representing object
     * actions, lazily creating them first if required.
     */
    public List<FacetedMethod> getActionFacetedMethods(final Properties metadataProperties) {
        if (actionFacetedMethods == null) {
            actionFacetedMethods = findActionFacetedMethods(MethodScope.OBJECT, metadataProperties);
        }
        return actionFacetedMethods;
    }

    private enum RecognisedHelpersStrategy {
        SKIP, DONT_SKIP;
        public boolean skip() {
            return this == SKIP;
        }
    }

    /**
     * REVIEW: I'm not sure why we do two passes here.
     * 
     * <p>
     * Perhaps it's important to skip helpers first. I doubt it, though.
     */
    private List<FacetedMethod> findActionFacetedMethods(
            final MethodScope methodScope, 
            final Properties metadataProperties) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("introspecting {}: actions", getClassName());
        }
        final List<FacetedMethod> actionFacetedMethods1 = findActionFacetedMethods(methodScope, RecognisedHelpersStrategy.SKIP, metadataProperties);
        final List<FacetedMethod> actionFacetedMethods2 = findActionFacetedMethods(methodScope, RecognisedHelpersStrategy.DONT_SKIP, metadataProperties);
        return ListExtensions.combineWith(actionFacetedMethods1, actionFacetedMethods2);
    }

    private List<FacetedMethod> findActionFacetedMethods(
            final MethodScope methodScope, 
            final RecognisedHelpersStrategy recognisedHelpersStrategy, 
            final Properties metadataProperties) {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("  looking for action methods");
        }

        final List<FacetedMethod> actionFacetedMethods = Lists.newArrayList();

        for (int i = 0; i < methods.size(); i++) {
            final Method method = methods.get(i);
            if (method == null) {
                continue;
            }
            final FacetedMethod actionPeer = findActionFacetedMethod(methodScope, recognisedHelpersStrategy, method, metadataProperties);
            if (actionPeer != null) {
                methods.set(i, null);
                actionFacetedMethods.add(actionPeer);
            }
        }

        return actionFacetedMethods;
    }

    private FacetedMethod findActionFacetedMethod(
            final MethodScope methodScope, 
            final RecognisedHelpersStrategy recognisedHelpersStrategy, 
            final Method actionMethod, 
            final Properties metadataProperties) {

        if (!representsAction(actionMethod, methodScope, recognisedHelpersStrategy)) {
            return null;
        }

        // build action
        return createActionFacetedMethod(actionMethod, metadataProperties);
    }

    private FacetedMethod createActionFacetedMethod(
            final Method actionMethod, 
            final Properties metadataProperties) {
        
        if (!isAllParamTypesValid(actionMethod)) {
            return null;
        }

        final FacetedMethod action = FacetedMethod.createForAction(introspectedClass, actionMethod, getSpecificationLoader());

        // process facets on the action & parameters
        getFacetProcessor().process(introspectedClass, actionMethod, methodRemover, action, FeatureType.ACTION, metadataProperties);

        final List<FacetedMethodParameter> actionParams = action.getParameters();
        for (int j = 0; j < actionParams.size(); j++) {
            getFacetProcessor().processParams(introspectedClass, actionMethod, j, methodRemover, actionParams.get(j));
        }

        return action;
    }

    private boolean isAllParamTypesValid(final Method actionMethod) {
        for (final Class<?> paramType : actionMethod.getParameterTypes()) {
            final ObjectSpecification paramSpec = getSpecificationLoader().loadSpecification(paramType);
            if (paramSpec == null) {
                return false;
            }
        }
        return true;
    }

    private boolean representsAction(
            final Method actionMethod, 
            final MethodScope methodScope, 
            final RecognisedHelpersStrategy recognisedHelpersStrategy) {

        if (!MethodUtil.inScope(actionMethod, methodScope)) {
            return false;
        }

        final List<Class<?>> typesToLoad = new ArrayList<Class<?>>();
        specificationTraverser.traverseTypes(actionMethod, typesToLoad);

        final boolean anyLoadedAsNull = getSpecificationLoader().loadSpecifications(typesToLoad);
        if (anyLoadedAsNull) {
            return false;
        }

        if (!loadParamSpecs(actionMethod)) {
            return false;
        }

        if (getFacetProcessor().recognizes(actionMethod)) {
            // a bit of a hack
            if (actionMethod.getName().startsWith("set")) {
                return false;
            }
            if (recognisedHelpersStrategy.skip()) {
                LOG.debug("  skipping possible helper method {0}", actionMethod);
                return false;
            }
        }

        if(explicitActionAnnotationConfigured()) {
            if(!Annotations.isAnnotationPresent(actionMethod, Action.class)) {
                return false;
            }
        }
        LOG.debug("  identified action {0}", actionMethod);
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
     * As per {@link #findAndRemovePrefixedNonVoidMethods(org.apache.isis.core.metamodel.methodutils.MethodScope, String, Class, int, java.util.List)},
     * but appends to provided {@link List} (collecting parameter pattern).
     */
    private void findAndRemovePrefixedNonVoidMethods(
            final MethodScope methodScope, 
            final String prefix, 
            final Class<?> returnType, 
            final int paramCount, 
            final List<Method> methodListToAppendTo) {
        final List<Method> matchingMethods = findAndRemovePrefixedMethods(methodScope, prefix, returnType, false, paramCount);
        methodListToAppendTo.addAll(matchingMethods);
    }

    /**
     * Searches for all methods matching the prefix and returns them, also
     * removing it from the {@link #methods array of methods} if found.
     */
    private List<Method> findAndRemovePrefixedMethods(
            final MethodScope methodScope, 
            final String prefix, 
            final Class<?> returnType, 
            final boolean canBeVoid, 
            final int paramCount) {
        return MethodUtil.removeMethods(methods, methodScope, prefix, returnType, canBeVoid, paramCount);
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
