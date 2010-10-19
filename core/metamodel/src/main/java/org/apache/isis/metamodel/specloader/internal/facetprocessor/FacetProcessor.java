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


package org.apache.isis.metamodel.specloader.internal.facetprocessor;

import static org.apache.isis.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.isis.commons.lang.ListUtils;
import org.apache.isis.metamodel.config.IsisConfiguration;
import org.apache.isis.metamodel.facets.FacetFactory;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.MethodFilteringFacetFactory;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.facets.MethodRemoverConstants;
import org.apache.isis.metamodel.facets.PropertyOrCollectionIdentifyingFacetFactory;
import org.apache.isis.metamodel.java5.MethodPrefixBasedFacetFactory;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContextAware;
import org.apache.isis.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.metamodel.specloader.progmodelfacets.ProgrammingModelFacets;


public class FacetProcessor implements RuntimeContextAware {

    private final IsisConfiguration configuration;
    private final CollectionTypeRegistry collectionTypeRegistry;
    private final ProgrammingModelFacets programmingModelFacets;
    
    private final SpecificationLoader specificationLoader;
	private RuntimeContext runtimeContext;


    /**
     * Class<FacetFactory> => FacetFactory
     */
    private final Map<Class<? extends FacetFactory>, FacetFactory> factoryByFactoryType = new HashMap<Class<? extends FacetFactory>, FacetFactory>();

    /**
     * {@link FacetFactory Facet factories}, in order they were {@link #registerFactory(FacetFactory)
     * registered}.
     */
    private final List<FacetFactory> factories = new ArrayList<FacetFactory>();

    /**
     * All method prefixes to check in {@link #recognizes(Method)}.
     * 
     * <p>
     * Derived from factories that implement {@link MethodPrefixBasedFacetFactory}.
     */
    private List<String> methodPrefixes;

    /**
     * All registered {@link FacetFactory factories} that implement {@link MethodFilteringFacetFactory}.
     * 
     * <p>
     * Used within {@link #recognizes(Method)}.
     */
    private List<MethodFilteringFacetFactory> methodFilteringFactories;

    /**
     * All registered {@link FacetFactory factories} that implement
     * {@link PropertyOrCollectionIdentifyingFacetFactory}.
     * 
     * <p>
     * Used within {@link #recognizes(Method)}.
     */
    private List<PropertyOrCollectionIdentifyingFacetFactory> propertyOrCollectionIdentifyingFactories;

    /**
     * ObjectFeatureType => List<FacetFactory>
     * 
     * <p>
     * Lazily initialized, then cached. The lists remain in the same order that the factories were
     * {@link #registerFactory(FacetFactory) registered}.
     */
    private Map<ObjectFeatureType, List<FacetFactory>> factoryListByFeatureType = null;

    
    public FacetProcessor(
    		final IsisConfiguration configuration, 
    		final SpecificationLoader specificationLoader, 
    		final CollectionTypeRegistry collectionTypeRegistry, 
    		final ProgrammingModelFacets programmingModelFacets) {
        ensureThatState(configuration, is(notNullValue()));
        ensureThatState(collectionTypeRegistry, is(notNullValue()));
        ensureThatState(programmingModelFacets, is(notNullValue()));
        ensureThatState(specificationLoader, is(notNullValue()));
    	
    	this.configuration = configuration;
    	this.specificationLoader = specificationLoader;
    	this.programmingModelFacets = programmingModelFacets;
    	this.collectionTypeRegistry = collectionTypeRegistry;
    }
    
    ////////////////////////////////////////////////////
    // init, shutdown (application scoped)
    ////////////////////////////////////////////////////
    
    public void init() {
        ensureThatState(runtimeContext, is(notNullValue()));
        programmingModelFacets.init();
        final List<FacetFactory> facetFactoryList = programmingModelFacets.getList();
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
     * This is <tt>public</tt> so that can be used for <tt>@Facets</tt> processing 
     * (eg in <tt>JavaIntrospector</tt>).
     *
     * <p>
     * See bug-517.
     */
	public void injectDependenciesInto(final FacetFactory factory) {
		getCollectionTypeRepository().injectInto(factory);
        getSpecificationLoader().injectInto(factory);
        getRuntimeContext().injectInto(factory);
        getIsisConfiguration().injectInto(factory);
	}

	public FacetFactory getFactoryByFactoryType(final Class<? extends FacetFactory> factoryType) {
        return factoryByFactoryType.get(factoryType);
    }

    /**
     * Appends to the supplied {@link Set} all of the {@link Method}s that may represent a property or
     * collection.
     * 
     * <p>
     * Delegates to all known {@link PropertyOrCollectionIdentifyingFacetFactory}s.
     */
    public Set<Method> findPropertyOrCollectionCandidateAccessors(final Method[] methods, final Set<Method> candidates) {
        cachePropertyOrCollectionIdentifyingFacetFactoriesIfRequired();
        for (int i = 0; i < methods.length; i++) {
            final Method method = methods[i];
            if (method == null) {
                continue;
            }
            for (final PropertyOrCollectionIdentifyingFacetFactory facetFactory : propertyOrCollectionIdentifyingFactories) {
                if (facetFactory.isPropertyOrCollectionAccessorCandidate(method)) {
                    candidates.add(method);
                }
            }
        }
        return candidates;
    }

    /**
     * Use the provided {@link MethodRemover} to have all known
     * {@link PropertyOrCollectionIdentifyingFacetFactory}s to remove all property accessors, and append them
     * to the supplied methodList.
     * 
     * <p>
     * Intended to be called after {@link #findAndRemoveValuePropertyAccessors(MethodRemover, List)} once only
     * reference properties remain.
     * 
     * @see PropertyOrCollectionIdentifyingFacetFactory#findAndRemoveValuePropertyAccessors(MethodRemover,
     *      List)
     */
    public void findAndRemovePropertyAccessors(final MethodRemover methodRemover, final List<Method> methodListToAppendTo) {
        for (final PropertyOrCollectionIdentifyingFacetFactory facetFactory : propertyOrCollectionIdentifyingFactories) {
            facetFactory.findAndRemovePropertyAccessors(methodRemover, methodListToAppendTo);
        }
    }

    /**
     * Use the provided {@link MethodRemover} to have all known
     * {@link PropertyOrCollectionIdentifyingFacetFactory}s to remove all property accessors, and append them
     * to the supplied methodList.
     * 
     * @see PropertyOrCollectionIdentifyingFacetFactory#findAndRemoveCollectionAccessors(MethodRemover, List)
     */
    public void findAndRemoveCollectionAccessors(final MethodRemover methodRemover, final List<Method> methodListToAppendTo) {
        cachePropertyOrCollectionIdentifyingFacetFactoriesIfRequired();
        for (final PropertyOrCollectionIdentifyingFacetFactory facetFactory : propertyOrCollectionIdentifyingFactories) {
            facetFactory.findAndRemoveCollectionAccessors(methodRemover, methodListToAppendTo);
        }
    }


    /**
     * Whether this {@link Method method} is recognized by any of the {@link FacetFactory}s.
     * 
     * <p>
     * Typically this is when method has a specific prefix, such as <tt>validate</tt> or <tt>hide</tt>.
     * Specifically, it checks:
     * <ul>
     * <li>the method's prefix against the prefixes supplied by any {@link MethodPrefixBasedFacetFactory}</li>
     * <li>the method against any {@link MethodFilteringFacetFactory}</li>
     * </ul>
     * 
     * <p>
     * The design of {@link MethodPrefixBasedFacetFactory} (whereby this facet factory set does the work) is a
     * slight performance optimization for when there are multiple facet factories that search for the same
     * prefix.
     */
    public boolean recognizes(final Method method) {
        cacheMethodPrefixesIfRequired();
        final String methodName = method.getName();
        for (final String prefix : methodPrefixes) {
            if (methodName.startsWith(prefix)) {
                return true;
            }
        }

        cacheMethodFilteringFacetFactoriesIfRequired();
        for (final MethodFilteringFacetFactory factory : methodFilteringFactories) {
            if (factory.recognizes(method)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Attaches all facets applicable to the provided {@link ObjectFeatureType#OBJECT object}) to the
     * supplied {@link FacetHolder}.
     * 
     * <p>
     * Delegates to {@link FacetFactory#process(Class, FacetHolder)} for each appropriate factory.
     * 
     * @see FacetFactory#process(Class, MethodRemover, FacetHolder)
     * 
     * @param cls
     *            - class to process
     * @param facetHolder
     *            - holder to attach facets to.
     * 
     * @return <tt>true</tt> if any facets were added, <tt>false</tt> otherwise.
     */
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder facetHolder) {
        boolean facetsAdded = false;
        final List<FacetFactory> factoryList = getFactoryListByFeatureType(ObjectFeatureType.OBJECT);
        for (final FacetFactory facetFactory : factoryList) {
            facetsAdded = facetFactory.process(cls, removerElseNullRemover(methodRemover), facetHolder) | facetsAdded;
        }
        return facetsAdded;
    }

    /**
     * Attaches all facets applicable to the provided {@link ObjectFeatureType type of feature} to the
     * supplied {@link FacetHolder}.
     * 
     * <p>
     * Delegates to {@link FacetFactory#process(Method, FacetHolder)} for each appropriate factory.
     * 
     * @see FacetFactory#process(Method, FacetHolder)
     * 
     * @param cls
     *           - class in which introspect; allowing the helper methods to be found is subclasses of that which the method was originally found.
     * @param method
     *            - method to process
     * @param facetHolder
     *            - holder to attach facets to.
     * @param featureType
     *            - what type of feature the method represents (property, action, collection etc)
     * 
     * @return <tt>true</tt> if any facets were added, <tt>false</tt> otherwise.
     */
    public boolean process(
            final Class<?> cls,
            final Method method,
            final MethodRemover methodRemover,
            final FacetHolder facetHolder,
            final ObjectFeatureType featureType) {
        boolean facetsAdded = false;
        final List<FacetFactory> factoryList = getFactoryListByFeatureType(featureType);
        for (final FacetFactory facetFactory : factoryList) {
            facetsAdded = facetFactory.process(cls, method, removerElseNullRemover(methodRemover), facetHolder) | facetsAdded;
        }
        return facetsAdded;
    }

    /**
     * Attaches all facets applicable to the provided {@link ObjectFeatureType#ACTION_PARAMETER
     * parameter}), to the supplied {@link FacetHolder}.
     * 
     * <p>
     * Delegates to {@link FacetFactory#processParams(Method, int, FacetHolder)} for each appropriate factory.
     * 
     * @see FacetFactory#processParams(Method, int, FacetHolder)
     * 
     * @param method
     *            - action method to process
     * @param paramNum
     *            - 0-based
     * @param facetHolder
     *            - holder to attach facets to.
     * 
     * @return <tt>true</tt> if any facets were added, <tt>false</tt> otherwise.
     */
    public boolean processParams(final Method method, final int paramNum, final FacetHolder facetHolder) {
        boolean facetsAdded = false;
        final List<FacetFactory> factoryList = getFactoryListByFeatureType(ObjectFeatureType.ACTION_PARAMETER);
        for (final FacetFactory facetFactory : factoryList) {
            facetsAdded = facetFactory.processParams(method, paramNum, facetHolder) | facetsAdded;
        }
        return facetsAdded;
    }

    private List<FacetFactory> getFactoryListByFeatureType(final ObjectFeatureType featureType) {
        cacheByFeatureTypeIfRequired();
        return factoryListByFeatureType.get(featureType);
    }

    private void clearCaches() {
        factoryListByFeatureType = null;
        methodPrefixes = null;
        methodFilteringFactories = null;
        propertyOrCollectionIdentifyingFactories = null;
    }

    private synchronized void cacheByFeatureTypeIfRequired() {
        if (factoryListByFeatureType != null) {
            return;
        }
        factoryListByFeatureType = new HashMap<ObjectFeatureType, List<FacetFactory>>();
        for (final FacetFactory factory : factories) {
            final ObjectFeatureType[] featureTypes = factory.getFeatureTypes();
            for (int i = 0; i < featureTypes.length; i++) {
                final List<FacetFactory> factoryList = getList(factoryListByFeatureType, featureTypes[i]);
                factoryList.add(factory);
            }
        }
    }

    private synchronized void cacheMethodPrefixesIfRequired() {
        if (methodPrefixes != null) {
            return;
        }
        methodPrefixes = new ArrayList<String>();
        for (final FacetFactory facetFactory : factories) {
            if (facetFactory instanceof MethodPrefixBasedFacetFactory) {
                final MethodPrefixBasedFacetFactory methodPrefixBasedFacetFactory = (MethodPrefixBasedFacetFactory) facetFactory;
                ListUtils.combine(methodPrefixes, methodPrefixBasedFacetFactory.getPrefixes());
            }
        }
    }

    private synchronized void cacheMethodFilteringFacetFactoriesIfRequired() {
        if (methodFilteringFactories != null) {
            return;
        }
        methodFilteringFactories = new ArrayList<MethodFilteringFacetFactory>();
        for (final FacetFactory factory : factories) {
            if (factory instanceof MethodFilteringFacetFactory) {
                final MethodFilteringFacetFactory methodFilteringFacetFactory = (MethodFilteringFacetFactory) factory;
                methodFilteringFactories.add(methodFilteringFacetFactory);
            }
        }
    }

    private synchronized void cachePropertyOrCollectionIdentifyingFacetFactoriesIfRequired() {
        if (propertyOrCollectionIdentifyingFactories != null) {
            return;
        }
        propertyOrCollectionIdentifyingFactories = new ArrayList<PropertyOrCollectionIdentifyingFacetFactory>();
        final Iterator<FacetFactory> iter = factories.iterator();
        while (iter.hasNext()) {
            final FacetFactory factory = iter.next();
            if (factory instanceof PropertyOrCollectionIdentifyingFacetFactory) {
                final PropertyOrCollectionIdentifyingFacetFactory identifyingFacetFactory = (PropertyOrCollectionIdentifyingFacetFactory) factory;
                propertyOrCollectionIdentifyingFactories.add(identifyingFacetFactory);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> getList(final Map map, final Object key) {
        List<T> list = (List<T>) map.get(key);
        if (list == null) {
            list = new ArrayList<T>();
            map.put(key, list);
        }
        return list;
    }

    private MethodRemover removerElseNullRemover(final MethodRemover methodRemover) {
        return methodRemover != null ? methodRemover : MethodRemoverConstants.NULL;
    }

    
    // ////////////////////////////////////////////////////////////////////
    // Dependencies (injected in constructor)
    // ////////////////////////////////////////////////////////////////////

    private IsisConfiguration getIsisConfiguration() {
        return configuration;
    }
    private SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }
    private CollectionTypeRegistry getCollectionTypeRepository() {
        return collectionTypeRegistry;
    }


    // ////////////////////////////////////////////////////////////////////
    // Dependencies (injected via setter due to *Aware)
    // ////////////////////////////////////////////////////////////////////

    private RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}
    /**
     * Injected so can propogate to any {@link #registerFactory(FacetFactory) registered} {@link FacetFactory}
     * s that are also {@link RuntimeContextAware}.
     */
	public void setRuntimeContext(RuntimeContext runtimeContext) {
		this.runtimeContext = runtimeContext;
	}


}
