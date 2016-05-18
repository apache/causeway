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

package org.apache.isis.core.metamodel.specloader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ClassUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.runtimecontext.ConfigurationServiceInternal;
import org.apache.isis.core.metamodel.runtimecontext.PersistenceSessionService;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.metamodel.spec.FreeStandingList;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationDependencies;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpiAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberDependencies;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.specimpl.FacetedMethodsBuilderContext;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract.IntrospectionState;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.metamodel.specloader.specimpl.standalonelist.ObjectSpecificationOnStandaloneList;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

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
 * 
 * <p>
 * In addition, the {@link RuntimeContext} can optionally be injected, but will
 * default to {@link RuntimeContextNoRuntime} if not provided prior to
 * {@link SpecificationLoaderSpi#init(RuntimeContext) initialization}. The purpose of {@link RuntimeContext} is to
 * allow the metamodel to be used standalone, for example in a Maven plugin. The
 * {@link RuntimeContextNoRuntime} implementation will through an exception for
 * any methods (such as finding an {@link ObjectAdapter adapter}) because there
 * is no runtime session. In the case of the metamodel being used by the
 * framework (that is, when there <i>is</i> a runtime), then the framework
 * injects an implementation of {@link RuntimeContext} that acts like a bridge
 * to its <tt>IsisContext</tt>.
 */

public final class ObjectReflectorDefault
        implements SpecificationLoaderSpi, ApplicationScopedComponent {

    private final static Logger LOG = LoggerFactory.getLogger(ObjectReflectorDefault.class);

    private final ClassSubstitutor classSubstitutor = new ClassSubstitutor();

    private final DeploymentCategory deploymentCategory;
    private final ProgrammingModel programmingModel;
    private final FacetProcessor facetProcessor;

    private final ServicesInjectorSpi servicesInjector;

    private final MetaModelValidator metaModelValidator;
    private final SpecificationCacheDefault cache = new SpecificationCacheDefault();
    private final List<LayoutMetadataReader> layoutMetadataReaders;

    /**
     * Can optionally be injected, but will default (to
     * {@link RuntimeContextNoRuntime}) otherwise.
     *
     * <p>
     * Should be injected when used by framework, but will default to a no-op implementation if the metamodel is
     * being used standalone (eg for a code-generator).
     */
    private RuntimeContext runtimeContext;

    private boolean initialized = false;
    private ValidationFailures validationFailures;

    // /////////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////////

    public ObjectReflectorDefault(
            final DeploymentCategory deploymentCategory,
            final IsisConfiguration configuration,
            final ProgrammingModel programmingModel,
            final MetaModelValidator metaModelValidator,
            final List<LayoutMetadataReader> layoutMetadataReaders,
            final ServicesInjectorSpi servicesInjector) {

        ensureThatArg(deploymentCategory, is(notNullValue()));
        ensureThatArg(configuration, is(notNullValue()));
        ensureThatArg(servicesInjector, is(notNullValue()));
        ensureThatArg(programmingModel, is(notNullValue()));
        ensureThatArg(metaModelValidator, is(notNullValue()));
        ensureThatArg(layoutMetadataReaders, is(notNullValue()));
        ensureThatArg(layoutMetadataReaders, is(not(emptyCollectionOf(LayoutMetadataReader.class))));

        this.deploymentCategory = deploymentCategory;
        this.servicesInjector = servicesInjector;

        this.programmingModel = programmingModel;


        this.metaModelValidator = metaModelValidator;
        this.facetProcessor = new FacetProcessor(configuration, programmingModel);
        this.layoutMetadataReaders = layoutMetadataReaders;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LOG.info("finalizing reflector factory " + this);
    }

    // /////////////////////////////////////////////////////////////
    // init, shutdown
    // /////////////////////////////////////////////////////////////

    /**
     * Initializes and wires up, and primes the cache based on any service
     * classes (provided by the {@link ServicesInjectorSpi}).
     * @param runtimeContext
     */
    public void init(final RuntimeContext runtimeContext) {

        initialize(runtimeContext);

        cacheBySpecId();
        
        initialized = true;
    }

    public void validateAndAssert() {
        ValidationFailures validationFailures = validate();
        validationFailures.assertNone();

        cacheBySpecId();
    }

    public ValidationFailures validate() {
        if(validationFailures == null) {
            validationFailures = new ValidationFailures();
            metaModelValidator.validate(validationFailures);
        }
        return validationFailures;
    }

    public void initialize(final RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;

        if (LOG.isDebugEnabled()) {
            LOG.debug("initialising " + this);
        }

        // default subcomponents
        if (this.runtimeContext == null) {
            servicesInjector.addFallbackIfRequired(ConfigurationServiceInternal.class, new IsisConfigurationDefault(null));
            this.runtimeContext = new RuntimeContextNoRuntime(servicesInjector, this);
        }

        injectInto(metaModelValidator);

        // wire subcomponents into each other
        this.runtimeContext.injectInto(facetProcessor);
        for (final LayoutMetadataReader layoutMetadataReader : layoutMetadataReaders) {
            this.runtimeContext.injectInto(layoutMetadataReader);
        }

        // initialize subcomponents
        programmingModel.init();
        facetProcessor.init();
        metaModelValidator.init();

        loadSpecificationsForServices();
        loadSpecificationsForMixins();
    }

    private void loadSpecificationsForServices() {
        for (final Class<?> serviceClass : getServiceClasses()) {
            final DomainService domainService = serviceClass.getAnnotation(DomainService.class);
            if(domainService != null) {
                if(domainService.nature() == NatureOfService.VIEW || domainService.nature() == NatureOfService.VIEW_CONTRIBUTIONS_ONLY) {
                    internalLoadSpecification(serviceClass, domainService.nature());
                }
            }
        }
    }


    private void loadSpecificationsForMixins() {
        final Set<Class<?>> mixinTypes = AppManifest.Registry.instance().getMixinTypes();
        if(mixinTypes == null) {
            return;
        }
        for (final Class<?> mixinType : mixinTypes) {
            internalLoadSpecification(mixinType);
        }
    }

    private void cacheBySpecId() {
        final Map<ObjectSpecId, ObjectSpecification> specById = Maps.newHashMap();
        for (final ObjectSpecification objSpec : allSpecifications()) {
            final ObjectSpecId objectSpecId = objSpec.getSpecId();
            if (objectSpecId == null) {
                continue;
            }
            specById.put(objectSpecId, objSpec);
        }

        getCache().setCacheBySpecId(specById);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    public void shutdown() {
        LOG.info("shutting down " + this);

        initialized = false;
        
        getCache().clear();
    }


    @Override
    public void invalidateCache(final Class<?> cls) {
        
        if(!getCache().isInitialized()) {
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
            getCache().remove(type.getName());
            if(spec.containsDoOpFacet(ObjectSpecIdFacet.class)) {
                // umm.  Some specs do not have an ObjectSpecIdFacet...
                recache(spec);
            }
            spec = spec.superclass(); 
        }
    }


    private void recache(final ObjectSpecification newSpec) {
        getCache().recache(newSpec);
    }

    

    // /////////////////////////////////////////////////////////////
    // install, load, allSpecifications, lookup
    // /////////////////////////////////////////////////////////////

    /**
     * API: Return the specification for the specified class of object.
     */
    @Override
    public final ObjectSpecification loadSpecification(final String className) {
        ensureThatArg(className, is(notNullValue()), "specification class name must be specified");

        try {
            final Class<?> cls = loadBuiltIn(className);
            return internalLoadSpecification(cls);
        } catch (final ClassNotFoundException e) {
            final ObjectSpecification spec = getCache().get(className);
            if (spec == null) {
                throw new IsisException("No such class available: " + className);
            }
            return spec;
        }
    }

    /**
     * API: Return specification.
     */
    @Override
    public ObjectSpecification loadSpecification(final Class<?> type) {
        final ObjectSpecification spec = internalLoadSpecification(type);
        if(spec == null) {
            return null;
        }
        if(getCache().isInitialized()) {
            // umm.  It turns out that anonymous inner classes (eg org.estatio.dom.WithTitleGetter$ToString$1)
            // don't have an ObjectSpecId; hence the guard.
            if(spec.containsDoOpFacet(ObjectSpecIdFacet.class)) {
                ObjectSpecId specId = spec.getSpecId();
                if (getCache().getByObjectType(specId) == null) {
                    getCache().recache(spec);
                }
            }
        }
        return spec;
    }

    private ObjectSpecification internalLoadSpecification(final Class<?> type) {
        return internalLoadSpecification(type, null);
    }

    private ObjectSpecification internalLoadSpecification(final Class<?> type, final NatureOfService nature) {
        final Class<?> substitutedType = classSubstitutor.getClass(type);
        return substitutedType != null ? loadSpecificationForSubstitutedClass(substitutedType, nature) : null;
    }


    private ObjectSpecification loadSpecificationForSubstitutedClass(final Class<?> type, final NatureOfService nature) {
        Assert.assertNotNull(type);
        final String typeName = type.getName();

        final SpecificationCacheDefault specificationCache = getCache();
        synchronized (specificationCache) {
            final ObjectSpecification spec = specificationCache.get(typeName);
            if (spec != null) {
                return spec;
            }
            final ObjectSpecification specification = createSpecification(type);
            if(nature != null) {
                specification.markAsService();
            }
            if (specification == null) {
                throw new IsisException("Failed to create specification for class " + typeName);
            }

            // put into the cache prior to introspecting, to prevent
            // infinite loops
            specificationCache.cache(typeName, specification);

            introspectIfRequired(specification);

            return specification;
        }
    }

    /**
     * Loads the specifications of the specified types except the one specified
     * (to prevent an infinite loop).
     */
    @Override
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
    @Override
    public boolean loadSpecifications(final List<Class<?>> typesToLoad) {
        return loadSpecifications(typesToLoad, null);
    }

    /**
     * Creates the appropriate type of {@link ObjectSpecification}.
     */
    private ObjectSpecification createSpecification(final Class<?> cls) {

        final ServicesInjector servicesInjector = getRuntimeContext().getServicesInjector();
        final PersistenceSessionService persistenceSessionService = getRuntimeContext().getPersistenceSessionService();

        final ObjectSpecificationDependencies specContext =
                new ObjectSpecificationDependencies(deploymentCategory, servicesInjector, this, facetProcessor);

        final ObjectMemberDependencies objectMemberDependencies =
                new ObjectMemberDependencies(this, servicesInjector, persistenceSessionService);

        // ... and create the specs
        if (FreeStandingList.class.isAssignableFrom(cls)) {
            return new ObjectSpecificationOnStandaloneList(specContext, objectMemberDependencies);
        } else {
            final FacetedMethodsBuilderContext facetedMethodsBuilderContext =
                    new FacetedMethodsBuilderContext(
                            this, facetProcessor, layoutMetadataReaders);
            return new ObjectSpecificationDefault(cls, facetedMethodsBuilderContext, specContext,
                    objectMemberDependencies);
        }
    }

    private Class<?> loadBuiltIn(final String className) throws ClassNotFoundException {
        final Class<?> builtIn = ClassUtil.getBuiltIn(className);
        if (builtIn != null) {
            return builtIn;
        }
        return Class.forName(className);
    }

    /**
     * Return all the loaded specifications.
     */
    @Override
    public Collection<ObjectSpecification> allSpecifications() {
        return getCache().allSpecifications();
    }

    @Override
    public boolean loaded(final Class<?> cls) {
        return loaded(cls.getName());
    }

    @Override
    public boolean loaded(final String fullyQualifiedClassName) {
        return getCache().get(fullyQualifiedClassName) != null;
    }

    public ObjectSpecification introspectIfRequired(final ObjectSpecification spec) {

        final ObjectSpecificationAbstract specSpi = (ObjectSpecificationAbstract)spec;
        final IntrospectionState introspectionState = specSpi.getIntrospectionState();

        // REVIEW: can't remember why this is done in multiple passes, could it be simplified?
        if (introspectionState == IntrospectionState.NOT_INTROSPECTED) {

            specSpi.setIntrospectionState(IntrospectionState.BEING_INTROSPECTED);
            introspect(specSpi);

        } else if (introspectionState == IntrospectionState.BEING_INTROSPECTED) {

            introspect(specSpi);

        } else if (introspectionState == IntrospectionState.INTROSPECTED) {
            // nothing to do
        }
        return spec;
    }

    private void introspect(final ObjectSpecificationAbstract specSpi) {
        specSpi.introspectTypeHierarchyAndMembers();
        specSpi.updateFromFacetValues();
        specSpi.setIntrospectionState(IntrospectionState.INTROSPECTED);

    }

    @Override
    public ObjectSpecification lookupBySpecId(ObjectSpecId objectSpecId) {
        final ObjectSpecification objectSpecification = getCache().getByObjectType(objectSpecId);
        if(objectSpecification == null) {
            // fallback
            return loadSpecification(objectSpecId.asString());
        }
        return objectSpecification;
    }


    // ////////////////////////////////////////////////////////////////////
    // injectInto
    // ////////////////////////////////////////////////////////////////////

    /**
     * Injects self into candidate if required, and instructs its subcomponents
     * to do so also.
     */
    @Override
    public void injectInto(final Object candidate) {
        final Class<?> candidateClass = candidate.getClass();
        if (SpecificationLoaderSpiAware.class.isAssignableFrom(candidateClass)) {
            final SpecificationLoaderSpiAware cast = SpecificationLoaderSpiAware.class.cast(candidate);
            cast.setSpecificationLoaderSpi(this);
        }
        if (SpecificationLoaderAware.class.isAssignableFrom(candidateClass)) {
            final SpecificationLoaderAware cast = SpecificationLoaderAware.class.cast(candidate);
            cast.setSpecificationLoader(this);
        }
        if (ServicesInjectorAware.class.isAssignableFrom(candidateClass)) {
            final ServicesInjectorAware cast = ServicesInjectorAware.class.cast(candidate);
            cast.setServicesInjector(this.servicesInjector);
        }
    }


    // /////////////////////////////////////////////////////////////
    // Helpers (were previously injected, but no longer required)
    // /////////////////////////////////////////////////////////////

    /**
     * Provides access to the registered {@link Facet}s.
     */
    public FacetProcessor getFacetProcessor() {
        return facetProcessor;
    }

    private SpecificationCacheDefault getCache() {
        return cache;
    }

    // ////////////////////////////////////////////////////////////////////
    // Dependencies (injected by setter due to *Aware)
    // ////////////////////////////////////////////////////////////////////

    /**
     * Injected in {@link #initialize(RuntimeContext)}.
     */
    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }


    // ////////////////////////////////////////////////////////////////////
    // Dependencies (setters, optional)
    // ////////////////////////////////////////////////////////////////////

    public List<Class<?>> getServiceClasses() {
        List<Class<?>> serviceClasses = Lists.transform(this.servicesInjector.getRegisteredServices(), new Function<Object, Class<?>>(){
            public Class<?> apply(Object o) {
                return o.getClass();
            }
        });
        // take a copy, to allow eg I18nFacetFactory to add in default implementations of missing services.
        return Collections.unmodifiableList(Lists.newArrayList(serviceClasses));
    }

    public boolean isServiceClass(Class<?> cls) {
        return this.servicesInjector.isRegisteredService(cls);
    }

    // ////////////////////////////////////////////////////////////////////
    // Dependencies (injected from constructor)
    // ////////////////////////////////////////////////////////////////////

    protected MetaModelValidator getMetaModelValidator() {
        return metaModelValidator;
    }




}
