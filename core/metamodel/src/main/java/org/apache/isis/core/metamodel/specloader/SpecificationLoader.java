/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.specloader;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ClassUtil;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;
import org.apache.isis.core.metamodel.spec.FreeStandingList;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.isis.core.metamodel.specloader.specimpl.FacetedMethodsBuilderContext;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.metamodel.specloader.specimpl.standalonelist.ObjectSpecificationOnStandaloneList;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;
import org.apache.isis.schema.utils.CommonDtoUtils;

/**
 * Builds the meta-model.
 *
 * <p>
 * The implementation provides for a degree of pluggability:
 * <ul>
 * <li>The most important plug-in point is {@link ProgrammingModel} that
 * specifies the set of {@link Facet} that make up programming model. If not
 * specified then defaults to {@link ProgrammingModelFacetsJava5} (which should
 * be used as a starting point for your own customizations).
 * <li>The only mandatory plug-in point is {@link ClassSubstitutor}, which
 * allows the class to be loaded to be substituted if required. This is used in
 * conjunction with some <tt>PersistenceMechanism</tt>s that do class
 * enhancement.
 * </ul>
 * </p>
 *
 * <p>
 * Implementing class is added to {@link ServicesInjector} as an (internal) domain service; all public methods
 * must be annotated using {@link Programmatic}.
 * </p>
 *
 */
public class SpecificationLoader implements ApplicationScopedComponent {

    private final static Logger LOG = LoggerFactory.getLogger(SpecificationLoader.class);

    // -- constructor, fields
    private final ClassSubstitutor classSubstitutor = new ClassSubstitutor();

    private final ProgrammingModel programmingModel;
    private final FacetProcessor facetProcessor;

    private final IsisConfiguration configuration;
    private final ServicesInjector servicesInjector;

    private final MetaModelValidator metaModelValidator;
    private final SpecificationCacheDefault cache = new SpecificationCacheDefault();
    private final PostProcessor postProcessor;

    enum State {
        NOT_INITIALIZED,
        CACHING,
        INTROSPECTING
    }


    public SpecificationLoader(
            final IsisConfiguration configuration,
            final ProgrammingModel programmingModel,
            final MetaModelValidator metaModelValidator,
            final ServicesInjector servicesInjector) {

        this.configuration = configuration;

        this.servicesInjector = servicesInjector;
        this.programmingModel = programmingModel;
        this.metaModelValidator = metaModelValidator;

        this.facetProcessor = new FacetProcessor(programmingModel);
        this.postProcessor = new PostProcessor(programmingModel, servicesInjector);

        this.state = State.NOT_INITIALIZED;
    }

    // -- init

    private State state;

    /**
     * Initializes and wires up, and primes the cache based on any service
     * classes (provided by the {@link ServicesInjector}).
     */
    @Programmatic
    public void init() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("initialising {}", this);
        }

        // wire subcomponents into each other
        facetProcessor.setServicesInjector(servicesInjector);

        // initialize subcomponents
        this.programmingModel.init();
        facetProcessor.init();

        postProcessor.init();
        metaModelValidator.init(this);


        state = State.CACHING;

        // need to completely load services and mixins (synchronously)
        final List<ObjectSpecification> specificationsFromRegistry = _Lists.newArrayList();

        loadSpecificationsFor(
                CommonDtoUtils.VALUE_TYPES, null,
                IntrospectionStrategy.STUB, specificationsFromRegistry);
        loadSpecificationsFor(
                streamServiceClasses().collect(Collectors.toList()), NatureOfService.DOMAIN,
                IntrospectionStrategy.STUB, specificationsFromRegistry);
        loadSpecificationsFor(
                AppManifest.Registry.instance().getMixinTypes(), null,
                IntrospectionStrategy.STUB, specificationsFromRegistry);
        loadSpecificationsFor(
                AppManifest.Registry.instance().getDomainObjectTypes(), null,
                IntrospectionStrategy.STUB, specificationsFromRegistry);
        loadSpecificationsFor(
                AppManifest.Registry.instance().getViewModelTypes(), null,
                IntrospectionStrategy.STUB, specificationsFromRegistry);
        loadSpecificationsFor(
                AppManifest.Registry.instance().getXmlElementTypes(), null,
                IntrospectionStrategy.STUB, specificationsFromRegistry);

        state = State.INTROSPECTING;
        final Collection<ObjectSpecification> cachedSpecifications = allCachedSpecifications();


        // for debugging only
        LOG.info(String.format(
                "specificationsFromRegistry.size = %d ; cachedSpecifications.size = %d",
                specificationsFromRegistry.size(), cachedSpecifications.size()));

        List<ObjectSpecification> registryNotCached = specificationsFromRegistry.stream()
                .filter(spec -> !cachedSpecifications.contains(spec))
                .collect(Collectors.toList());
        List<ObjectSpecification> cachedNotRegistry = cachedSpecifications.stream()
                .filter(spec -> !specificationsFromRegistry.contains(spec))
                .collect(Collectors.toList());

        LOG.info(String.format(
                "registryNotCached.size = %d ; cachedNotRegistry.size = %d",
                registryNotCached.size(), cachedNotRegistry.size()));



        final List<Callable<Object>> callables = _Lists.newArrayList();
        for (final ObjectSpecification specification : specificationsFromRegistry) {

            Callable<Object> callable = new Callable<Object>() {
                @Override
                public Object call() {
                    introspectIfRequired(specification);
                    return null;
                }
                public String toString() {
                    return String.format(
                            "introspectIfRequired(\"%s\")",
                            specification.getFullIdentifier());
                }
            };
            callables.add(callable);
        }
        ThreadPoolSupport threadPoolSupport = ThreadPoolSupport.getInstance();
        List<Future<Object>> futures = threadPoolSupport.invokeAll(callables);
        threadPoolSupport.joinGatherFailures(futures);


        // for debugging only
        final Collection<ObjectSpecification> cachedSpecificationsAfter = allCachedSpecifications();
        List<ObjectSpecification> cachedAfterNotBefore = cachedSpecificationsAfter.stream()
                .filter(spec -> !cachedSpecifications.contains(spec))
                .collect(Collectors.toList());
        LOG.info(String.format("cachedSpecificationsAfter.size = %d ; cachedAfterNotBefore.size = %d",
                cachedSpecificationsAfter.size(), cachedAfterNotBefore.size()));


        // only after full introspection has occured do we cache ObjectSpecifications
        // by their ObjectSpecId.
        // the cache (SpecificationCacheDefault will fail-fast as not initialized
        cacheBySpecId(specificationsFromRegistry);

    }

    private void loadSpecificationsFor(
            final Collection<Class<?>> domainTypes,
            final NatureOfService natureOfServiceFallback,
            final IntrospectionStrategy introspectionStrategy,
            final List<ObjectSpecification> appendTo) {

        for (final Class<?> domainType : domainTypes) {

            ObjectSpecification objectSpecification =
                internalLoadSpecification(domainType, natureOfServiceFallback, introspectionStrategy);

            if(objectSpecification != null) {
                appendTo.add(objectSpecification);
            }
        }
    }


    private void cacheBySpecId(final Collection<ObjectSpecification> objectSpecifications) {
        final Map<ObjectSpecId, ObjectSpecification> specById = _Maps.newHashMap();
        for (final ObjectSpecification objSpec : objectSpecifications) {
            final ObjectSpecId objectSpecId = objSpec.getSpecId();
            if (objectSpecId == null) {
                continue;
            }
            specById.put(objectSpecId, objSpec);
        }

        cache.init(specById);
    }


    // -- shutdown

    @Programmatic
    public void shutdown() {
        LOG.info("shutting down {}", this);

        state = State.NOT_INITIALIZED;

        cache.clear();
    }

    // -- invalidateCache

    @Programmatic
    public void invalidateCache(final Class<?> cls) {

        if(!cache.isInitialized()) {
            // could be called by JRebel plugin, before we are up-and-running
            // just ignore.
            return;
        }
        final Class<?> substitutedType = classSubstitutor.getClass(cls);

        if(substitutedType.isAnonymousClass()) {
            // JRebel plugin might call us... just ignore 'em.
            return;
        }

        ObjectSpecification spec = loadSpecification(substitutedType);
        while(spec != null) {
            final Class<?> type = spec.getCorrespondingClass();
            cache.remove(type.getName());
            if(spec.containsDoOpFacet(ObjectSpecIdFacet.class)) {
                // umm.  Some specs do not have an ObjectSpecIdFacet...
                recache(spec);
            }
            spec = spec.superclass();
        }
    }


    private void recache(final ObjectSpecification newSpec) {
        cache.recache(newSpec);
    }

    // -- validation

    private ValidationFailures validationFailures;

    @Programmatic
    public void validateAndAssert() {
        ValidationFailures validationFailures = validate();
        validationFailures.assertNone();

        cacheBySpecId(allCachedSpecifications());
    }

    @Programmatic
    public ValidationFailures validate() {
        if(validationFailures == null) {
            validationFailures = new ValidationFailures();
            metaModelValidator.validate(validationFailures);
        }
        return validationFailures;
    }

    // -- loadSpecification, loadSpecifications

    /**
     * Return the specification for the specified class of object.
     *
     * <p>
     * It is possible for this method to return <tt>null</tt>, for example if
     * the configured {@link org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor}
     * has filtered out the class.
     */
    @Programmatic
    public ObjectSpecification loadSpecification(final String className) {
        assert className != null;

        try {
            final Class<?> cls = loadBuiltIn(className);
            return internalLoadSpecification(cls);
        } catch (final ClassNotFoundException e) {
            final ObjectSpecification spec = cache.get(className);
            if (spec == null) {
                throw new IsisException("No such class available: " + className);
            }
            return spec;
        }
    }

    /**
     * @see #loadSpecification(String)
     */
    @Programmatic
    public ObjectSpecification loadSpecification(final Class<?> type) {
        final ObjectSpecification spec = internalLoadSpecification(type);
        if(spec == null) {
            return null;
        }
        if(cache.isInitialized()) {
            // umm.  It turns out that anonymous inner classes (eg org.estatio.dom.WithTitleGetter$ToString$1)
            // don't have an ObjectSpecId; hence the guard.
            if(spec.containsDoOpFacet(ObjectSpecIdFacet.class)) {
                ObjectSpecId specId = spec.getSpecId();
                if (cache.getByObjectType(specId) == null) {
                    cache.recache(spec);
                }
            }
        }
        return spec;
    }

    enum IntrospectionStrategy {
        /**
         * Used in a first pass to create ObjectSpecifications that haven't been introspected,
         * but are cached.
         */
        STUB,
        /**
         * Second phase (default), to fully introspect.
         */
        COMPLETE
    }
    private ObjectSpecification internalLoadSpecification(final Class<?> type) {
        return internalLoadSpecification(type, null, IntrospectionStrategy.COMPLETE);
    }

    private ObjectSpecification internalLoadSpecification(
            final Class<?> type,
            final NatureOfService natureFallback,
            final IntrospectionStrategy introspectionStrategy) {

        final Class<?> substitutedType = classSubstitutor.getClass(type);
        return substitutedType != null
                ? loadSpecificationForSubstitutedClass(substitutedType, natureFallback, introspectionStrategy)
                : null;
    }

    private ObjectSpecification loadSpecificationForSubstitutedClass(
            final Class<?> type,
            final NatureOfService natureFallback,
            final IntrospectionStrategy introspectionStrategy) {
        Assert.assertNotNull(type);

        final String typeName = type.getName();
        final ObjectSpecification spec = cache.get(typeName);
        if (spec != null) {
            return spec;
        }

        return loadSpecificationForSubstitutedClassSynchronized(type, natureFallback, introspectionStrategy);
    }


    private synchronized ObjectSpecification loadSpecificationForSubstitutedClassSynchronized(
            final Class<?> type,
            final NatureOfService natureOfServiceFallback,
            final IntrospectionStrategy introspectionStrategy) {

        final String typeName = type.getName();
        final ObjectSpecification spec = cache.get(typeName);
        if (spec != null) {
            // because caller isn't synchronized.
            return spec;
        }

        final ObjectSpecification specification = createSpecification(type, natureOfServiceFallback);

        // put into the cache prior to introspecting, to prevent
        // infinite loops
        cache.cache(typeName, specification);

        if(introspectionStrategy == IntrospectionStrategy.COMPLETE) {
            introspectIfRequired(specification);
        }

        return specification;
    }

    /**
     * Loads the specifications of the specified types except the one specified
     * (to prevent an infinite loop).
     */
    @Programmatic
    public boolean loadSpecifications(final List<Class<?>> typesToLoad, final Class<?> typeToIgnore) {
        boolean anyLoadedAsNull = false;
        for (final Class<?> typeToLoad : typesToLoad) {
            if (typeToLoad != typeToIgnore) {
                final ObjectSpecification noSpec = internalLoadSpecification(typeToLoad);
                final boolean loadedAsNull = (noSpec == null);
                anyLoadedAsNull = loadedAsNull || anyLoadedAsNull;
            }
        }
        return anyLoadedAsNull;
    }

    /**
     * Loads the specifications of the specified types.
     */
    @Programmatic
    public boolean loadSpecifications(final List<Class<?>> typesToLoad) {
        return loadSpecifications(typesToLoad, null);
    }

    /**
     * Creates the appropriate type of {@link ObjectSpecification}.
     */
    private ObjectSpecification createSpecification(
            final Class<?> cls,
            final NatureOfService fallback) {

        // ... and create the specs
        if (FreeStandingList.class.isAssignableFrom(cls)) {
            return new ObjectSpecificationOnStandaloneList(servicesInjector, facetProcessor);
        } else {
            final ConfigurationServiceInternal configService = servicesInjector.lookupServiceElseFail(
                    ConfigurationServiceInternal.class);
            final FacetedMethodsBuilderContext facetedMethodsBuilderContext =
                    new FacetedMethodsBuilderContext(
                            this, facetProcessor, configService);

            final NatureOfService natureOfServiceIfAny = natureOfServiceFrom(cls, fallback);

            return new ObjectSpecificationDefault(cls, facetedMethodsBuilderContext,
                    servicesInjector, facetProcessor, natureOfServiceIfAny);
        }
    }

    private NatureOfService natureOfServiceFrom(
            final Class<?> type,
            final NatureOfService fallback) {
        final DomainService domainServiceIfAny = type.getAnnotation(DomainService.class);
        return domainServiceIfAny != null ? domainServiceIfAny.nature() : fallback;
    }

    private Class<?> loadBuiltIn(final String className) throws ClassNotFoundException {
        final Class<?> builtIn = ClassUtil.getBuiltIn(className);
        if (builtIn != null) {
            return builtIn;
        }
        return ClassUtil.forName(className);
    }

    /**
     * Typically does not need to be called, but is available for {@link FacetFactory}s to force
     * early introspection of referenced specs in certain circumstances.
     *
     * <p>
     * Originally introduced to support {@link AutoCompleteFacet}.
     */
    private ObjectSpecification introspectIfRequired(final ObjectSpecification spec) {

        if(state != State.INTROSPECTING) {
            return spec;
        }

        final ObjectSpecificationAbstract specSpi = (ObjectSpecificationAbstract)spec;
        final ObjectSpecificationAbstract.IntrospectionState introspectionState = specSpi.getIntrospectionState();

        // REVIEW: can't remember why this is done in multiple passes, could it be simplified?
        switch (introspectionState) {
        case NOT_INTROSPECTED:

            specSpi.setIntrospectionState(ObjectSpecificationAbstract.IntrospectionState.BEING_INTROSPECTED);
            introspect(specSpi);
            break;
        case BEING_INTROSPECTED:
            introspect(specSpi);
            break;
        case INTROSPECTED:
            // nothing to do
            break;
        }
        return spec;
    }

    private void introspect(final ObjectSpecificationAbstract specSpi) {
        specSpi.introspectTypeHierarchyAndMembers();
        specSpi.updateFromFacetValues();
        specSpi.setIntrospectionState(ObjectSpecificationAbstract.IntrospectionState.INTROSPECTED);
    }

    @Programmatic
    public void postProcess() {

        final Collection<ObjectSpecification> specs = allSpecifications();
        for (final ObjectSpecification spec : specs) {
            postProcess(spec);
        }

    }

    @Programmatic
    public void postProcess(final ObjectSpecification spec) {
        postProcessor.postProcess(spec);
    }



    // -- allSpecifications
    /**
     * Returns (a new list holding a copy of) all the loaded specifications.
     *
     * <p>
     *     A new list is returned to avoid concurrent modification exceptions for if the caller then
     *     iterates over all the specifications and performs an activity that might give rise to new
     *     ObjectSpec's being discovered, eg performing metamodel validation.
     * </p>
     */
    @Programmatic
    public Collection<ObjectSpecification> allSpecifications() {
        return _Lists.newArrayList(allCachedSpecifications());
    }

    private Collection<ObjectSpecification> allCachedSpecifications() {
        return cache.allSpecifications();
    }

    // -- getServiceClasses, isServiceClass

    @Programmatic
    public Stream<Class<?>> streamServiceClasses() {
        return servicesInjector.streamServiceTypes();
    }

    @Programmatic
    public boolean isServiceClass(Class<?> cls) {
        return this.servicesInjector.isRegisteredService(cls);
    }

    // -- loaded
    /**
     * Whether this class has been loaded.
     */
    @Programmatic
    public boolean loaded(final Class<?> cls) {
        return loaded(cls.getName());
    }

    /**
     * @see #loaded(Class).
     */
    @Programmatic
    public boolean loaded(final String fullyQualifiedClassName) {
        return cache.get(fullyQualifiedClassName) != null;
    }

    // -- lookupBySpecId
    @Programmatic
    public ObjectSpecification lookupBySpecId(ObjectSpecId objectSpecId) {
        if(!cache.isInitialized()) {
            throw new IllegalStateException("Internal cache not yet initialized");
        }
        final ObjectSpecification objectSpecification = cache.getByObjectType(objectSpecId);
        if(objectSpecification == null) {
            // fallback
            return loadSpecification(objectSpecId.asString());
        }
        return objectSpecification;
    }

    @Programmatic
    public IsisConfiguration getConfiguration() {
        return configuration;
    }

    @Programmatic
    public ServicesInjector getServicesInjector() {
        return servicesInjector;
    }
}
