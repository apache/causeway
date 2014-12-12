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
import java.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.lang.ListExtensions;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.*;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class FacetProcessor implements RuntimeContextAware {

    private final IsisConfiguration configuration;
    private final ProgrammingModel programmingModel;

    private RuntimeContext runtimeContext;

    /**
     * Class<FacetFactory> => FacetFactory
     */
    private final Map<Class<? extends FacetFactory>, FacetFactory> factoryByFactoryType = Maps.newHashMap();

    /**
     * {@link FacetFactory Facet factories}, in order they were
     * {@link #registerFactory(FacetFactory) registered}.
     */
    private final List<FacetFactory> factories = Lists.newArrayList();

    /**
     * All method prefixes to check in {@link #recognizes(Method)}.
     * 
     * <p>
     * Derived from factories that implement
     * {@link MethodPrefixBasedFacetFactory}.
     * 
     * <p>
     * If <tt>null</tt>, indicates that the cache hasn't been built.
     */
    private List<String> cachedMethodPrefixes;

    /**
     * All registered {@link FacetFactory factories} that implement
     * {@link MethodFilteringFacetFactory}.
     * 
     * <p>
     * Used within {@link #recognizes(Method)}.
     * 
     * <p>
     * If <tt>null</tt>, indicates that the cache hasn't been built.
     */
    private List<MethodFilteringFacetFactory> cachedMethodFilteringFactories;
    
    /**
     * All registered {@link FacetFactory factories} that implement
     * {@link ContributeeMemberFacetFactory}.
     * 
     * <p>
     * If <tt>null</tt>, indicates that the cache hasn't been built.
     */
    private List<ContributeeMemberFacetFactory> cachedMemberOrderingFactories;

    /**
     * All registered {@link FacetFactory factories} that implement
     * {@link PropertyOrCollectionIdentifyingFacetFactory}.
     * 
     * <p>
     * Used within {@link #recognizes(Method)}.
     * 
     * <p>
     * If <tt>null</tt>, indicates that the cache hasn't been built.
     */
    private List<PropertyOrCollectionIdentifyingFacetFactory> cachedPropertyOrCollectionIdentifyingFactories;

    /**
     * ObjectFeatureType => List<FacetFactory>
     * 
     * <p>
     * Lazily initialized, then cached. The lists remain in the same order that
     * the factories were {@link #registerFactory(FacetFactory) registered}.
     */
    private Map<FeatureType, List<FacetFactory>> factoryListByFeatureType = null;

    public FacetProcessor(
            final IsisConfiguration configuration,
            final ProgrammingModel programmingModel) {
        ensureThatState(configuration, is(notNullValue()));
        ensureThatState(programmingModel, is(notNullValue()));

        this.configuration = configuration;
        this.programmingModel = programmingModel;
    }

    // //////////////////////////////////////////////////
    // init, shutdown (application scoped)
    // //////////////////////////////////////////////////

    public void init() {
        ensureThatState(runtimeContext, is(notNullValue()));
        final List<FacetFactory> facetFactoryList = programmingModel.getList();
        for (final FacetFactory facetFactory : facetFactoryList) {
            registerFactory(facetFactory);
        }
    }

    public void shutdown() {
    }

    public void registerFactory(final FacetFactory factory) {
        clearCaches();
        factoryByFactoryType.put(factory.getClass(), factory);
        factories.add(factory);

        injectDependenciesInto(factory);
    }

    /**
     * This is <tt>public</tt> so that can be used for <tt>@Facets</tt>
     * processing.
     */
    public void injectDependenciesInto(final FacetFactory factory) {
        getIsisConfiguration().injectInto(factory);

        // cascades all the subcomponents also
        getRuntimeContext().injectInto(factory);
    }

    public FacetFactory getFactoryByFactoryType(final Class<? extends FacetFactory> factoryType) {
        return factoryByFactoryType.get(factoryType);
    }

    /**
     * Appends to the supplied {@link Set} all of the {@link Method}s that may
     * represent a property or collection.
     * 
     * <p>
     * Delegates to all known
     * {@link PropertyOrCollectionIdentifyingFacetFactory}s.
     */
    public Set<Method> findAssociationCandidateAccessors(final List<Method> methods, final Set<Method> candidates) {
        cachePropertyOrCollectionIdentifyingFacetFactoriesIfRequired();
        for (final Method method : methods) {
            if (method == null) {
                continue;
            }
            for (final PropertyOrCollectionIdentifyingFacetFactory facetFactory : cachedPropertyOrCollectionIdentifyingFactories) {
                if (facetFactory.isPropertyOrCollectionAccessorCandidate(method)) {
                    candidates.add(method);
                }
            }
        }
        return candidates;
    }

    /**
     * Use the provided {@link MethodRemover} to have all known
     * {@link PropertyOrCollectionIdentifyingFacetFactory}s to remove all
     * property accessors, and append them to the supplied methodList.
     * 
     * <p>
     * Intended to be called after {@link #findAndRemovePropertyAccessors(org.apache.isis.core.metamodel.facetapi.MethodRemover, java.util.List)} once only reference properties remain.
     */
    public void findAndRemovePropertyAccessors(final MethodRemover methodRemover, final List<Method> methodListToAppendTo) {
        cachePropertyOrCollectionIdentifyingFacetFactoriesIfRequired();
        for (final PropertyOrCollectionIdentifyingFacetFactory facetFactory : cachedPropertyOrCollectionIdentifyingFactories) {
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
    public void findAndRemoveCollectionAccessors(final MethodRemover methodRemover, final List<Method> methodListToAppendTo) {
        cachePropertyOrCollectionIdentifyingFacetFactoriesIfRequired();
        for (final PropertyOrCollectionIdentifyingFacetFactory facetFactory : cachedPropertyOrCollectionIdentifyingFactories) {
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
    public boolean recognizes(final Method method) {
        cacheMethodPrefixesIfRequired();
        final String methodName = method.getName();
        for (final String prefix : cachedMethodPrefixes) {
            if (methodName.startsWith(prefix)) {
                return true;
            }
        }

        cacheMethodFilteringFacetFactoriesIfRequired();
        for (final MethodFilteringFacetFactory factory : cachedMethodFilteringFactories) {
            if (factory.recognizes(method)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Attaches all facets applicable to the provided {@link FeatureType#OBJECT
     * object}) to the supplied {@link FacetHolder}.
     * 
     * <p>
     * Delegates to {@link FacetFactory#process(org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext)} for each
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
            final Class<?> cls, 
            final Properties metadataProperties, 
            final MethodRemover methodRemover, 
            final FacetHolder facetHolder) {
        final List<FacetFactory> factoryList = getFactoryListByFeatureType(FeatureType.OBJECT);
        for (final FacetFactory facetFactory : factoryList) {
            facetFactory.process(new ProcessClassContext(cls, metadataProperties, removerElseNullRemover(methodRemover), facetHolder));
        }
    }

    public void processPost(
            final Class<?> cls, 
            final Properties metadataProperties, 
            final MethodRemover methodRemover, 
            final FacetHolder facetHolder) {
        final List<FacetFactory> factoryList = getFactoryListByFeatureType(FeatureType.OBJECT_POST_PROCESSING);
        for (final FacetFactory facetFactory : factoryList) {
            facetFactory.process(new ProcessClassContext(cls, metadataProperties, removerElseNullRemover(methodRemover), facetHolder));
        }
    }

    /**
     * Attaches all facets applicable to the provided {@link FeatureType type of
     * feature} to the supplied {@link FacetHolder}.
     * 
     * <p>
     * Delegates to {@link FacetFactory#process(org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext)} for each
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
     * @param metadataProperties 
     *            - additional properties to parse and use 
     */
    public void process(
            final Class<?> cls, 
            final Method method, 
            final MethodRemover methodRemover, 
            final FacetedMethod facetedMethod, 
            final FeatureType featureType, 
            final Properties metadataProperties) {
        final List<FacetFactory> factoryList = getFactoryListByFeatureType(featureType);
        for (final FacetFactory facetFactory : factoryList) {
            facetFactory.process(new ProcessMethodContext(cls, featureType, metadataProperties, method, removerElseNullRemover(methodRemover), facetedMethod));
        }
    }

    
    public void processMemberOrder(
            final Properties metadataProperties, 
            final ObjectMember facetHolder) {
        cacheMemberOrderingFacetFactoriesIfRequired();
        for (final ContributeeMemberFacetFactory facetFactory : cachedMemberOrderingFactories) {
            facetFactory.process(new ContributeeMemberFacetFactory.ProcessContributeeMemberContext(metadataProperties, facetHolder));
        }
    }

    /**
     * Attaches all facets applicable to the provided
     * {@link FeatureType#ACTION_PARAMETER parameter}), to the supplied
     * {@link FacetHolder}.
     * 
     * <p>
     * Delegates to {@link FacetFactory#processParams(ProcessParameterContext)}
     * for each appropriate factory.
     * 
     * @see FacetFactory#processParams(ProcessParameterContext)
     * 
     * @param method
     *            - action method to process
     * @param paramNum
     *            - 0-based
     * @param facetedMethodParameter
     *            - holder to attach facets to.
     */
    public void processParams(
            final Method method, 
            final int paramNum, 
            final FacetedMethodParameter facetedMethodParameter) {
        final List<FacetFactory> factoryList = getFactoryListByFeatureType(FeatureType.ACTION_PARAMETER);
        for (final FacetFactory facetFactory : factoryList) {
            facetFactory.processParams(new ProcessParameterContext(method, paramNum, facetedMethodParameter));
        }
    }


    
    
    private List<FacetFactory> getFactoryListByFeatureType(final FeatureType featureType) {
        cacheByFeatureTypeIfRequired();
        List<FacetFactory> list = factoryListByFeatureType.get(featureType);
        return list != null? list: Collections.<FacetFactory>emptyList();
    }

    private void clearCaches() {
        factoryListByFeatureType = null;
        cachedMethodPrefixes = null;
        cachedMethodFilteringFactories = null;
        cachedPropertyOrCollectionIdentifyingFactories = null;
    }

    private synchronized void cacheByFeatureTypeIfRequired() {
        if (factoryListByFeatureType != null) {
            return;
        }
        factoryListByFeatureType = Maps.newHashMap();
        for (final FacetFactory factory : factories) {
            final List<FeatureType> featureTypes = factory.getFeatureTypes();
            for (final FeatureType featureType : featureTypes) {
                final List<FacetFactory> factoryList = getList(factoryListByFeatureType, featureType);
                factoryList.add(factory);
            }
        }
    }

    private synchronized void cacheMethodPrefixesIfRequired() {
        if (cachedMethodPrefixes != null) {
            return;
        }
        cachedMethodPrefixes = Lists.newArrayList();
        for (final FacetFactory facetFactory : factories) {
            if (facetFactory instanceof MethodPrefixBasedFacetFactory) {
                final MethodPrefixBasedFacetFactory methodPrefixBasedFacetFactory = (MethodPrefixBasedFacetFactory) facetFactory;
                ListExtensions.mergeWith(cachedMethodPrefixes, methodPrefixBasedFacetFactory.getPrefixes());
            }
        }
    }

    private synchronized void cacheMethodFilteringFacetFactoriesIfRequired() {
        if (cachedMethodFilteringFactories != null) {
            return;
        }
        cachedMethodFilteringFactories = Lists.newArrayList();
        for (final FacetFactory factory : factories) {
            if (factory instanceof MethodFilteringFacetFactory) {
                final MethodFilteringFacetFactory methodFilteringFacetFactory = (MethodFilteringFacetFactory) factory;
                cachedMethodFilteringFactories.add(methodFilteringFacetFactory);
            }
        }
    }

    private synchronized void cacheMemberOrderingFacetFactoriesIfRequired() {
        if (cachedMemberOrderingFactories != null) {
            return;
        }
        cachedMemberOrderingFactories = Lists.newArrayList();
        for (final FacetFactory factory : factories) {
            if (factory instanceof ContributeeMemberFacetFactory) {
                final ContributeeMemberFacetFactory memberOrderingFacetFactory = (ContributeeMemberFacetFactory) factory;
                cachedMemberOrderingFactories.add(memberOrderingFacetFactory);
            }
        }
    }
    
    private synchronized void cachePropertyOrCollectionIdentifyingFacetFactoriesIfRequired() {
        if (cachedPropertyOrCollectionIdentifyingFactories != null) {
            return;
        }
        cachedPropertyOrCollectionIdentifyingFactories = Lists.newArrayList();
        for (FacetFactory factory : factories) {
            if (factory instanceof PropertyOrCollectionIdentifyingFacetFactory) {
                final PropertyOrCollectionIdentifyingFacetFactory identifyingFacetFactory = (PropertyOrCollectionIdentifyingFacetFactory) factory;
                cachedPropertyOrCollectionIdentifyingFactories.add(identifyingFacetFactory);
            }
        }
    }

    private static <K, T> List<T> getList(final Map<K, List<T>> map, final K key) {
        List<T> list = map.get(key);
        if (list == null) {
            list = Lists.newArrayList();
            map.put(key, list);
        }
        return list;
    }

    private MethodRemover removerElseNullRemover(final MethodRemover methodRemover) {
        return methodRemover != null ? methodRemover : MethodRemoverConstants.NULL;
    }


    //region > dependencies

    private IsisConfiguration getIsisConfiguration() {
        return configuration;
    }


    private RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    /**
     * Injected so can propogate to any {@link #registerFactory(FacetFactory)
     * registered} {@link FacetFactory} s that are also
     * {@link RuntimeContextAware}.
     */
    @Override
    public void setRuntimeContext(final RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

    //endregion

}
