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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facetapi.MethodRemover;
import org.apache.causeway.core.metamodel.facets.AccessorFacetFactory;
import org.apache.causeway.core.metamodel.facets.FacetFactory;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.FacetedMethodParameter;
import org.apache.causeway.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.causeway.core.metamodel.facets.ObjectTypeFacetFactory.ProcessObjectTypeContext;
import org.apache.causeway.core.metamodel.methods.MethodFilteringFacetFactory;
import org.apache.causeway.core.metamodel.methods.MethodPrefixBasedFacetFactory;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

import org.jspecify.annotations.NonNull;

record FacetProcessor(
        ProgrammingModel programmingModel,
        /**
         * {@link FacetFactory Facet factories}, in order as provided by the ProgrammingModel.
         */
        Can<FacetFactory> factories,
        /**
         * Class<FacetFactory> => FacetFactory
         */
        Map<Class<? extends FacetFactory>, FacetFactory> factoryByFactoryType,
        /**
         * <pre>ObjectFeatureType => List of FacetFactory</pre>
         * <p>
         * The lists remain in the same order as the order in
         * {@link #factories}.
         */
        ListMultimap<FeatureType, FacetFactory> factoriesByFeatureType,
        /**
         * All method prefixes to check in {@link #recognizes(Method)}.
         * <p>
         * Derived from factories that implement
         * {@link MethodPrefixBasedFacetFactory}.
         */
        Set<String> methodPrefixes,
        /**
         * All registered {@link FacetFactory factories} that implement
         * {@link MethodFilteringFacetFactory}.
         */
        List<MethodFilteringFacetFactory> methodFilteringFactories,
        /**
         * {@link FacetFactory factories} that implement {@link AccessorFacetFactory}
         * and support properties.
         */
        List<AccessorFacetFactory> propertyAccessorFactories,
        /**
         * {@link FacetFactory factories} that implement {@link AccessorFacetFactory}
         * and support collections.
         */
        List<AccessorFacetFactory> collectionAccessorFactories,
        List<ObjectTypeFacetFactory> objectSpecIfFacetFactoryList
        )
implements HasMetaModelContext {

    public FacetProcessor(
            final @NonNull ProgrammingModel programmingModel) {
        this(programmingModel, programmingModel.streamFactories()
                .map(programmingModel.getMetaModelContext().getServiceInjector()::injectServicesInto)
                .collect(Can.toCan()));
    }

    private FacetProcessor(
            final ProgrammingModel programmingModel,
            final Can<FacetFactory> factories) {
        this(programmingModel, factories, factories.toMap(FacetFactory::getClass), factoriesByFeatureType(factories));
    }

    private FacetProcessor(
            final ProgrammingModel programmingModel,
            final Can<FacetFactory> factories,
            final Map<Class<? extends FacetFactory>, FacetFactory> factoryByFactoryType,
            final ListMultimap<FeatureType, FacetFactory> factoriesByFeatureType) {
        this(programmingModel,
                factories,
                factoryByFactoryType,
                factoriesByFeatureType,
                methodPrefixes(factories),
                methodFilteringFactories(factories),
                propertyAccessorFactories(factories),
                collectionAccessorFactories(factories),
                objectSpecIfFacetFactoryList(factoriesByFeatureType, factories));
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return programmingModel.getMetaModelContext();
    }

    /**
     * Appends to the supplied {@link Set} all of the {@link Method}s that may
     * represent a property or collection.
     * <p>
     * Delegates to all known
     * {@link AccessorFacetFactory}s.
     */
    public void findAssociationCandidateGetters(
            final Stream<ResolvedMethod> methodStream,
            final Consumer<ResolvedMethod> onCandidate) {

        methodStream.forEach(method->{
            for (var facetFactory : propertyAccessorFactories) {
                if (facetFactory.isAssociationAccessor(method)) {
                    onCandidate.accept(method);
                    return; // first wins
                }
            }
            for (var facetFactory : collectionAccessorFactories) {
                if (facetFactory.isAssociationAccessor(method)) {
                    onCandidate.accept(method);
                    return; // first wins
                }
            }
        });
    }

    /**
     * Use the provided {@link MethodRemover} to call all known
     * {@link AccessorFacetFactory}s to remove all
     * property accessors and append them to the supplied methodList.
     */
    public List<ResolvedMethod> findAndRemovePropertyAccessors(
            final MethodRemover methodRemover) {
        var propertyAccessors = new ArrayList<ResolvedMethod>();
        for (var facetFactory : propertyAccessorFactories) {
            methodRemover.removeMethods(facetFactory::isAssociationAccessor, propertyAccessors::add);
        }
        return propertyAccessors;
    }

    /**
     * Use the provided {@link MethodRemover} to call all known
     * {@link AccessorFacetFactory}s to remove all
     * collection accessors and append them to the supplied methodList.
     */
    public List<ResolvedMethod> findAndRemoveCollectionAccessors(
            final MethodRemover methodRemover) {
        var collectionAccessors = new ArrayList<ResolvedMethod>();
        for (var facetFactory : collectionAccessorFactories) {
            methodRemover.removeMethods(facetFactory::isAssociationAccessor, collectionAccessors::add);
        }
        return collectionAccessors;
    }

    /**
     * Whether this {@link Method method} is recognized by any of the
     * {@link FacetFactory}s.
     * <p>
     * Typically this is when method has a specific prefix, such as
     * <tt>validate</tt> or <tt>hide</tt>. Specifically, it checks:
     * <ul>
     * <li>the method's prefix against the prefixes supplied by any
     * {@link MethodPrefixBasedFacetFactory}</li>
     * <li>the method against any {@link MethodFilteringFacetFactory}</li>
     * </ul>
     * <p>
     * The design of {@link MethodPrefixBasedFacetFactory} (whereby this facet
     * factory set does the work) is a slight performance optimization for when
     * there are multiple facet factories that search for the same prefix.
     */
    public boolean recognizes(final ResolvedMethod method) {
        var methodName = method.name();
        for (var prefix : methodPrefixes) {
            if (methodName.startsWith(prefix)) return true;
        }
        for (var factory : methodFilteringFactories) {
            if (factory.recognizes(method)) return true;
        }
        return false;
    }

    public void processObjectType(final Class<?> cls, final FacetHolder facetHolder) {
        for (var facetFactory : objectSpecIfFacetFactoryList()) {
            facetFactory.process(new ProcessObjectTypeContext(cls, facetHolder));
        }
    }

    /**
     * Attaches all facets applicable to the provided {@link FeatureType#OBJECT
     * object}) to the supplied {@link FacetHolder}.
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
            final Class<?> cls,
            final IntrospectionPolicy introspectionPolicy,
            final MethodRemover methodRemover,
            final FacetHolder facetHolder) {
        var ctx = new ProcessClassContext(
                cls,
                introspectionPolicy,
                removerElseNoopRemover(methodRemover),
                facetHolder);

        factoriesByFeatureType.getOrElseEmpty(FeatureType.OBJECT)
            .forEach(facetFactory->facetFactory.process(ctx));
    }

    /**
     * Attaches all facets applicable to the provided {@link FeatureType type of
     * feature} to the supplied {@link FacetHolder}.
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
            final Class<?> cls,
            final IntrospectionPolicy introspectionPolicy,
            final MethodFacade method,
            final MethodRemover methodRemover,
            final FacetedMethod facetedMethod,
            final FeatureType featureType,
            final boolean isMixinMain) {

        var processMethodContext =
                new ProcessMethodContext(
                        cls,
                        introspectionPolicy,
                        featureType,
                        method,
                        removerElseNoopRemover(methodRemover), facetedMethod, isMixinMain);

        for (FacetFactory facetFactory : factoriesByFeatureType.getOrElseEmpty(featureType)) {
            facetFactory.process(processMethodContext);
        }
    }

    public void processMemberOrder(final ObjectMember facetHolder) {

    }

    /**
     * Attaches all facets applicable to the provided parameter to the supplied
     * {@link FacetHolder}.
     * <p>
     * Delegates to {@link FacetFactory#processParams(ProcessParameterContext)}
     * for each appropriate factory.
     *
     * @see FacetFactory#processParams(ProcessParameterContext)
     *
     * @param introspectedClass
     * @param method
     *            - action method to process
     * @param methodRemover
     * @param facetedMethodParameter
     */
    public void processParams(
            final Class<?> introspectedClass,
            final IntrospectionPolicy introspectionPolicy,
            final MethodFacade method,
            final MethodRemover methodRemover,
            final FacetedMethodParameter facetedMethodParameter) {

        var processParameterContext =
                new ProcessParameterContext(introspectedClass, introspectionPolicy,
                        method, methodRemover, facetedMethodParameter);

        FeatureType.PARAMETERS_ONLY.stream()
            .map(factoriesByFeatureType::getOrElseEmpty)
            .flatMap(List::stream)
            .collect(Collectors.toSet())
            .forEach(facetFactory->facetFactory.processParams(processParameterContext));
    }

    // -- INITIALIZERS

    private static ListMultimap<FeatureType, FacetFactory> factoriesByFeatureType(final Iterable<FacetFactory> factories) {
        var factoryListByFeatureType = _Multimaps.<FeatureType, FacetFactory>newListMultimap();
        for (var factory : factories) {
            factory.getFeatureTypes().forEach(featureType->
                factoryListByFeatureType.putElement(featureType, factory));
        }
        return factoryListByFeatureType;
    }

    private static Set<String> methodPrefixes(final Iterable<FacetFactory> factories) {
        var cachedMethodPrefixes = _Sets.<String>newHashSet();
        for (var facetFactory : factories) {
            if (facetFactory instanceof MethodPrefixBasedFacetFactory methodPrefixBasedFacetFactory) {
                methodPrefixBasedFacetFactory.getPrefixes().forEach(cachedMethodPrefixes::add);
            }
        }
        return cachedMethodPrefixes;
    }

    private static List<MethodFilteringFacetFactory> methodFilteringFactories(final Iterable<FacetFactory> factories) {
        var methodFilteringFactories = new ArrayList<MethodFilteringFacetFactory>();
        for (var factory : factories) {
            if (factory instanceof MethodFilteringFacetFactory methodFilteringFacetFactory) {
                methodFilteringFactories.add(methodFilteringFacetFactory);
            }
        }
        return methodFilteringFactories;
    }

    private static List<AccessorFacetFactory> propertyAccessorFactories(final Iterable<FacetFactory> factories) {
        var propertyOrCollectionIdentifyingFactories = new ArrayList<AccessorFacetFactory>();
        for (var factory : factories) {
            if (factory instanceof AccessorFacetFactory accessorFacetFactory) {
                if(!accessorFacetFactory.supportsProperties()) continue;
                propertyOrCollectionIdentifyingFactories.add(accessorFacetFactory);
            }
        }
        return propertyOrCollectionIdentifyingFactories;
    }
    private static List<AccessorFacetFactory> collectionAccessorFactories(final Iterable<FacetFactory> factories) {
        var propertyOrCollectionIdentifyingFactories = new ArrayList<AccessorFacetFactory>();
        for (var factory : factories) {
            if (factory instanceof AccessorFacetFactory accessorFacetFactory) {
                if(!accessorFacetFactory.supportsCollections()) continue;
                propertyOrCollectionIdentifyingFactories.add(accessorFacetFactory);
            }
        }
        return propertyOrCollectionIdentifyingFactories;
    }

    private static List<ObjectTypeFacetFactory> objectSpecIfFacetFactoryList(
            final ListMultimap<FeatureType, FacetFactory> factoryListByFeatureType,
            final Iterable<FacetFactory> factories) {
            var facetFactories = new ArrayList<ObjectTypeFacetFactory>();
            factoryListByFeatureType.getOrElseEmpty(FeatureType.OBJECT)
            .forEach(facetFactory->{
                if (facetFactory instanceof ObjectTypeFacetFactory objectTypeFacetFactory) {
                    facetFactories.add(objectTypeFacetFactory);
                }
            });
            return Collections.unmodifiableList(facetFactories);
    }

    // -- HELPER

    private static MethodRemover removerElseNoopRemover(final MethodRemover methodRemover) {
        return methodRemover != null ? methodRemover : MethodRemover.NOOP;
    }

}
