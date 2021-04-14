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

package org.apache.isis.core.metamodel.specloader.facetprocessor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.ObjectSpecIdFacetFactory;
import org.apache.isis.core.metamodel.facets.ObjectSpecIdFacetFactory.ProcessObjectSpecIdContext;
import org.apache.isis.core.metamodel.facets.PropertyOrCollectionIdentifyingFacetFactory;
import org.apache.isis.core.metamodel.methods.MethodFilteringFacetFactory;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactory;
import org.apache.isis.core.metamodel.methods.MethodRemoverConstants;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class FacetProcessor {

    @NonNull private final ProgrammingModel programmingModel;
    @NonNull private final MetaModelContext metaModelContext;

    /**
     * Class<FacetFactory> => FacetFactory
     */
    private final Map<Class<? extends FacetFactory>, FacetFactory> factoryByFactoryType = _Maps.newHashMap();

    /**
     * {@link FacetFactory Facet factories}, in order they were
     * {@link #registerFactory(FacetFactory) registered}.
     */
    private final List<FacetFactory> factories = _Lists.newArrayList();

    /**
     * All method prefixes to check in {@link #recognizes(Method)}.
     *
     * <p>
     * Derived from factories that implement
     * {@link MethodPrefixBasedFacetFactory}.
     *
     */
    private final _Lazy<Set<String>> methodPrefixes = 
            _Lazy.threadSafe(this::init_methodPrefixes);

    /**
     * All registered {@link FacetFactory factories} that implement
     * {@link MethodFilteringFacetFactory}.
     *
     * <p>
     * Used within {@link #recognizes(Method)}.
     *
     */
    private final _Lazy<List<MethodFilteringFacetFactory>> methodFilteringFactories =
            _Lazy.threadSafe(this::init_methodFilteringFactories);

    /**
     * All registered {@link FacetFactory factories} that implement
     * {@link PropertyOrCollectionIdentifyingFacetFactory}.
     *
     * <p>
     * Used within {@link #recognizes(Method)}.
     */
    private final _Lazy<List<PropertyOrCollectionIdentifyingFacetFactory>> propertyOrCollectionIdentifyingFactories =
            _Lazy.threadSafe(this::init_propertyOrCollectionIdentifyingFactories);

    /**
     * ObjectFeatureType => List<FacetFactory>
     *
     * <p>
     * Lazily initialized, then cached. The lists remain in the same order that
     * the factories were {@link #registerFactory(FacetFactory) registered}.
     */
    private final _Lazy<ListMultimap<FeatureType, FacetFactory>> factoryListByFeatureType = 
            _Lazy.threadSafe(this::init_factoriesByFeatureType);
    
    // -- LIFECYCLE
    
    public void init() {
        cleanUp(); 
        programmingModel.streamFactories()
        .forEach(this::registerFactory);
    }

    public void shutdown() {
        cleanUp();
    }

    private void cleanUp() {
        clearCaches();
        factories.clear();
        factoryByFactoryType.clear();
    }
    
    private void registerFactory(FacetFactory factory) {
        factoryByFactoryType.put(factory.getClass(), factory);
        factories.add(factory);
        injectDependenciesInto(factory);
    }

    /**
     * This is <tt>public</tt> so that can be used for <tt>@Facets</tt>
     * processing.
     */
    public void injectDependenciesInto(FacetFactory factory) {
        metaModelContext.getServiceInjector().injectServicesInto(factory);
    }

    /**
     * Appends to the supplied {@link Set} all of the {@link Method}s that may
     * represent a property or collection.
     *
     * <p>
     * Delegates to all known
     * {@link PropertyOrCollectionIdentifyingFacetFactory}s.
     */
    public void findAssociationCandidateAccessors(
            Stream<Method> methods, 
            Consumer<Method> onCandidate) {
        
        val factories = propertyOrCollectionIdentifyingFactories.get();
        
        methods.forEach(method->{
            for (val facetFactory : factories) {
                if (facetFactory.isPropertyOrCollectionAccessorCandidate(method)) {
                    onCandidate.accept(method);
                }
            }
        });
    }

    /**
     * Use the provided {@link MethodRemover} to have all known
     * {@link PropertyOrCollectionIdentifyingFacetFactory}s to remove all
     * property accessors, and append them to the supplied methodList.
     *
     * <p>
     * Intended to be called after {@link #findAndRemovePropertyAccessors(MethodRemover, java.util.List)} once only reference properties remain.
     */
    public void findAndRemovePropertyAccessors(
            MethodRemover methodRemover, 
            List<Method> methodListToAppendTo) {
        
        for (val facetFactory : propertyOrCollectionIdentifyingFactories.get()) {
            facetFactory.findAndRemovePropertyAccessors(methodRemover, methodListToAppendTo);
        }
    }

    /**
     * Use the provided {@link MethodRemover} to have all known
     * {@link PropertyOrCollectionIdentifyingFacetFactory}s to remove all
     * property accessors, and append them to the supplied methodList.
     *
     * @see PropertyOrCollectionIdentifyingFacetFactory#findAndRemoveCollectionAccessors(MethodRemover,
     *      List)
     */
    public void findAndRemoveCollectionAccessors(
            MethodRemover methodRemover, 
            List<Method> methodListToAppendTo) {
        
        for (val facetFactory : propertyOrCollectionIdentifyingFactories.get()) {
            facetFactory.findAndRemoveCollectionAccessors(methodRemover, methodListToAppendTo);
        }
    }

    /**
     * Whether this {@link Method method} is recognized by any of the
     * {@link FacetFactory}s.
     *
     * <p>
     * Typically this is when method has a specific prefix, such as
     * <tt>validate</tt> or <tt>hide</tt>. Specifically, it checks:
     * <ul>
     * <li>the method's prefix against the prefixes supplied by any
     * {@link MethodPrefixBasedFacetFactory}</li>
     * <li>the method against any {@link MethodFilteringFacetFactory}</li>
     * </ul>
     *
     * <p>
     * The design of {@link MethodPrefixBasedFacetFactory} (whereby this facet
     * factory set does the work) is a slight performance optimization for when
     * there are multiple facet factories that search for the same prefix.
     */
    public boolean recognizes(Method method) {
        val methodName = method.getName();
        for (val prefix : methodPrefixes.get()) {
            if (methodName.startsWith(prefix)) {
                return true;
            }
        }

        for (val factory : methodFilteringFactories.get()) {
            if (factory.recognizes(method)) {
                return true;
            }
        }

        return false;
    }

    public void processObjectSpecId(Class<?> cls, FacetHolder facetHolder) {
        val factoryList = getObjectSpecIfFacetFactoryList();
        for (val facetFactory : factoryList) {
            facetFactory.process(new ProcessObjectSpecIdContext(cls, facetHolder));
        }
    }

    private List<ObjectSpecIdFacetFactory> objectSpecIfFacetFactoryList = null;

    
    private List<ObjectSpecIdFacetFactory> getObjectSpecIfFacetFactoryList() {
        if(objectSpecIfFacetFactoryList == null) {
            val facetFactories = _Lists.<ObjectSpecIdFacetFactory>newArrayList();
            
            factoryListByFeatureType.get().getOrElseEmpty(FeatureType.OBJECT)
            .forEach(facetFactory->{
                if (facetFactory instanceof ObjectSpecIdFacetFactory) {
                    val objectSpecIdFacetFactory = (ObjectSpecIdFacetFactory) facetFactory;
                    facetFactories.add(objectSpecIdFacetFactory);
                }
            });
            
            objectSpecIfFacetFactoryList = Collections.unmodifiableList(facetFactories);
        }
        return objectSpecIfFacetFactoryList;
    }

    /**
     * Attaches all facets applicable to the provided {@link FeatureType#OBJECT
     * object}) to the supplied {@link FacetHolder}.
     *
     * <p>
     * Delegates to {@link FacetFactory#process(FacetFactory.ProcessClassContext)} for each
     * appropriate factory.
     *
     * @see FacetFactory#process(ProcessClassContext)
     *
     * @param cls
     *            - class to process
     * @param facetHolder
     *            - holder to attach facets to.
     */
    public void process(
            Class<?> cls,
            MethodRemover methodRemover,
            FacetHolder facetHolder) {
        
        val ctx = new ProcessClassContext(
                cls, 
                removerElseNoopRemover(methodRemover), 
                facetHolder);
        
        factoryListByFeatureType.get().getOrElseEmpty(FeatureType.OBJECT)
        .forEach(facetFactory->facetFactory.process(ctx));
    }


    /**
     * Attaches all facets applicable to the provided {@link FeatureType type of
     * feature} to the supplied {@link FacetHolder}.
     *
     * <p>
     * Delegates to {@link FacetFactory#process(FacetFactory.ProcessMethodContext)} for each
     * appropriate factory.
     *
     * @param cls
     *            - class in which introspect; allowing the helper methods to be
     *            found is subclasses of that which the method was originally
     *            found.
     * @param method
     *            - method to process
     * @param facetedMethod
     *            - holder to attach facets to.
     * @param featureType
     *            - what type of feature the method represents (property,
     *            action, collection etc)
     * @param isMixinMain
     *            - Whether we are currently processing a mixin type AND this context's method 
     *            can be identified as the main method of the processed mixin class. (since 2.0)
     */
    public void process(
            Class<?> cls,
            Method method,
            MethodRemover methodRemover,
            FacetedMethod facetedMethod,
            FeatureType featureType, 
            boolean isMixinMain) {
        
        facetedMethod.setMetaModelContext(metaModelContext);
        
        val processMethodContext =
                new ProcessMethodContext(
                        cls, 
                        featureType, 
                        method, 
                        removerElseNoopRemover(methodRemover), facetedMethod, isMixinMain);
        
        factoryListByFeatureType.get().getOrElseEmpty(featureType)
        .forEach(facetFactory->facetFactory.process(processMethodContext));
    }

    public void processMemberOrder(ObjectMember facetHolder) {
        
    }

    /**
     * Attaches all facets applicable to the provided parameter to the supplied
     * {@link FacetHolder}.
     *
     * <p>
     * Delegates to {@link FacetFactory#processParams(ProcessParameterContext)}
     * for each appropriate factory.
     *
     * @see FacetFactory#processParams(ProcessParameterContext)
     *
     * @param introspectedClass
     * @param method
     *            - action method to process
     * @param paramNum
     *            - 0-based
     * @param methodRemover
     * @param facetedMethodParameter
     */
    public void processParams(
            Class<?> introspectedClass,
            Method method,
            int paramNum,
            MethodRemover methodRemover,
            FacetedMethodParameter facetedMethodParameter) {
        
        facetedMethodParameter.setMetaModelContext(metaModelContext);
        
        for (val featureType : FeatureType.PARAMETERS_ONLY) {
            processParams(introspectedClass, method, paramNum, methodRemover, facetedMethodParameter, featureType);
        }
    }

    public void processParams(
            Class<?> introspectedClass,
            Method method,
            int paramNum,
            MethodRemover methodRemover,
            FacetedMethodParameter facetedMethodParameter,
            FeatureType featureType) {
        
        facetedMethodParameter.setMetaModelContext(metaModelContext);
        
        val processParameterContext =
                new ProcessParameterContext(introspectedClass, method, paramNum, methodRemover, facetedMethodParameter);
        
        factoryListByFeatureType.get().getOrElseEmpty(featureType)
        .forEach(facetFactory->facetFactory.processParams(processParameterContext));
    }


    private void clearCaches() {
        factoryListByFeatureType.clear();
        methodPrefixes.clear();
        methodFilteringFactories.clear();
        propertyOrCollectionIdentifyingFactories.clear();
    }
    
    // -- INITIALIZERS

    private ListMultimap<FeatureType, FacetFactory> init_factoriesByFeatureType() {
        val factoryListByFeatureType = _Multimaps.<FeatureType, FacetFactory>newListMultimap();
        for (val factory : factories) {
            factory.getFeatureTypes().forEach(featureType->
                factoryListByFeatureType.putElement(featureType, factory));
        }
        return factoryListByFeatureType;
    }

    private Set<String> init_methodPrefixes() {
        val cachedMethodPrefixes = _Sets.<String>newHashSet();
        for (val facetFactory : factories) {
            if (facetFactory instanceof MethodPrefixBasedFacetFactory) {
                val methodPrefixBasedFacetFactory = (MethodPrefixBasedFacetFactory) facetFactory;
                methodPrefixBasedFacetFactory.getPrefixes().forEach(cachedMethodPrefixes::add);
            }
        }
        return cachedMethodPrefixes;
    }

    private List<MethodFilteringFacetFactory> init_methodFilteringFactories() {
        val methodFilteringFactories = _Lists.<MethodFilteringFacetFactory>newArrayList();
        for (val factory : factories) {
            if (factory instanceof MethodFilteringFacetFactory) {
                val methodFilteringFacetFactory = (MethodFilteringFacetFactory) factory;
                methodFilteringFactories.add(methodFilteringFacetFactory);
            }
        }
        return methodFilteringFactories;
    }

    private List<PropertyOrCollectionIdentifyingFacetFactory> init_propertyOrCollectionIdentifyingFactories() {
        val propertyOrCollectionIdentifyingFactories = _Lists.<PropertyOrCollectionIdentifyingFacetFactory>newArrayList();
        for (val factory : factories) {
            if (factory instanceof PropertyOrCollectionIdentifyingFacetFactory) {
                val identifyingFacetFactory = (PropertyOrCollectionIdentifyingFacetFactory) factory;
                propertyOrCollectionIdentifyingFactories.add(identifyingFacetFactory);
            }
        }
        return propertyOrCollectionIdentifyingFactories;
    }
    
    // -- HELPER

    private static MethodRemover removerElseNoopRemover(MethodRemover methodRemover) {
        return methodRemover != null ? methodRemover : MethodRemoverConstants.NOOP;
    }

}
