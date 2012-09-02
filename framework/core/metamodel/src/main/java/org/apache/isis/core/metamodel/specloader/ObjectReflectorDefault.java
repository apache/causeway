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

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.JavaClassUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecorator;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecoratorSet;
import org.apache.isis.core.metamodel.facets.object.bounded.BoundedFacetUtils;
import org.apache.isis.core.metamodel.layout.MemberLayoutArranger;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAware;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.spec.FreeStandingList;
import org.apache.isis.core.metamodel.spec.ObjectInstantiator;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationContext;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpiAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryDefault;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.specimpl.CreateObjectContext;
import org.apache.isis.core.metamodel.specloader.specimpl.FacetedMethodsBuilderContext;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionContext;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.metamodel.specloader.specimpl.objectlist.ObjectSpecificationForFreeStandingList;
import org.apache.isis.core.metamodel.specloader.traverser.SpecificationTraverser;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

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
 * <li>The {@link CollectionTypeRegistry} specifies the types that should be
 * considered as collections. If not specified then will
 * {@link CollectionTypeRegistryDefault default}. (Note: this extension point
 * has not been tested, so should be considered more of a &quot;statement of
 * intent&quot; than actual API. Also, we may use annotations (similar to the
 * way in which Values are specified) as an alternative mechanism).
 * </ul>
 * 
 * <p>
 * In addition, the {@link RuntimeContext} can optionally be injected, but will
 * default to {@link RuntimeContextNoRuntime} if not provided prior to
 * {@link #init() initialization}. The purpose of {@link RuntimeContext} is to
 * allow the metamodel to be used standalone, for example in a Maven plugin. The
 * {@link RuntimeContextNoRuntime} implementation will through an exception for
 * any methods (such as finding an {@link ObjectAdapter adapter}) because there
 * is no runtime session. In the case of the metamodel being used by the
 * framework (that is, when there <i>is</i> a runtime), then the framework
 * injects an implementation of {@link RuntimeContext} that acts like a bridge
 * to its <tt>IsisContext</tt>.
 */

public class ObjectReflectorDefault implements SpecificationLoaderSpi, ApplicationScopedComponent, RuntimeContextAware, DebuggableWithTitle {

    private final static Logger LOG = Logger.getLogger(ObjectReflectorDefault.class);

    /**
     * Injected in the constructor.
     */
    private final IsisConfiguration configuration;
    /**
     * Injected in the constructor.
     */
    private final ClassSubstitutor classSubstitutor;
    /**
     * Injected in the constructor.
     */
    private final CollectionTypeRegistry collectionTypeRegistry;
    /**
     * Injected in the constructor.
     */
    private final ProgrammingModel programmingModel;

    /**
     * Defaulted in the constructor.
     */
    private final FacetProcessor facetProcessor;

    /**
     * Defaulted in the constructor, so can be added to via
     * {@link #setFacetDecorators(FacetDecoratorSet)} or
     * {@link #addFacetDecorator(FacetDecorator)}.
     * 
     * <p>
     * {@link FacetDecorator}s must be added prior to {@link #init()
     * initialization.}
     */
    private final FacetDecoratorSet facetDecoratorSet;

    /**
     * Can optionally be injected, but will default (to
     * {@link RuntimeContextNoRuntime}) otherwise.
     * 
     * <p>
     * Should be injected when used by framework, but will default to a no-op
     * implementation if the metamodel is being used standalone (eg for a
     * code-generator).
     */
    private RuntimeContext runtimeContext;

    private final SpecificationTraverser specificationTraverser;
    private final MemberLayoutArranger memberLayoutArranger;

    /**
     * Priming cache, optionally {@link #setServiceClasses(List) injected}.
     */
    private List<Class<?>> serviceClasses = Lists.newArrayList();

    private final MetaModelValidator metaModelValidator;

    /**
     * Defaulted in the constructor.
     */
    private final SpecificationCacheDefault cache = new SpecificationCacheDefault();

    // /////////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////////

    public ObjectReflectorDefault(final IsisConfiguration configuration, final ClassSubstitutor classSubstitutor, final CollectionTypeRegistry collectionTypeRegistry, final SpecificationTraverser specificationTraverser, final MemberLayoutArranger memberLayoutArranger,
            final ProgrammingModel programmingModel, final Set<FacetDecorator> facetDecorators, final MetaModelValidator metaModelValidator) {

        ensureThatArg(configuration, is(notNullValue()));
        ensureThatArg(classSubstitutor, is(notNullValue()));
        ensureThatArg(collectionTypeRegistry, is(notNullValue()));
        ensureThatArg(specificationTraverser, is(notNullValue()));
        ensureThatArg(memberLayoutArranger, is(notNullValue()));
        ensureThatArg(programmingModel, is(notNullValue()));
        ensureThatArg(facetDecorators, is(notNullValue()));
        ensureThatArg(metaModelValidator, is(notNullValue()));

        this.configuration = configuration;
        this.classSubstitutor = classSubstitutor;
        this.collectionTypeRegistry = collectionTypeRegistry;
        this.programmingModel = programmingModel;
        this.specificationTraverser = specificationTraverser;
        this.memberLayoutArranger = memberLayoutArranger;

        this.facetDecoratorSet = new FacetDecoratorSet();
        for (final FacetDecorator facetDecorator : facetDecorators) {
            this.facetDecoratorSet.add(facetDecorator);
        }

        this.metaModelValidator = metaModelValidator;
        this.facetProcessor = new FacetProcessor(configuration, collectionTypeRegistry, programmingModel);
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
     * classes that may have been {@link #setServiceClasses(List) injected}.
     */
    @Override
    public void init() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initialising " + this);
        }

        // default subcomponents
        if (runtimeContext == null) {
            runtimeContext = new RuntimeContextNoRuntime();
        }
        injectInto(runtimeContext);
        injectInto(specificationTraverser);
        injectInto(memberLayoutArranger);
        injectInto(metaModelValidator);

        // wire subcomponents into each other
        runtimeContext.injectInto(facetProcessor);

        // initialize subcomponents
        facetDecoratorSet.init();
        classSubstitutor.init();
        collectionTypeRegistry.init();
        specificationTraverser.init();
        programmingModel.init();
        facetProcessor.init();
        metaModelValidator.init();

        // prime cache and validate
        primeCache();
        
        ValidationFailures validationFailures = new ValidationFailures();
        metaModelValidator.validate(validationFailures);
        
        validationFailures.assertNone();
        
        cacheBySpecId();
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

    /**
     * load the service specifications and then, using the
     * {@link #getSpecificationTraverser() traverser}, keep loading all
     * referenced specifications until we can find no more.
     */
    private void primeCache() {
        for (final Class<?> serviceClass : serviceClasses) {
            internalLoadSpecification(serviceClass);
        }
        loadAllSpecifications();
    }

    private void loadAllSpecifications() {
        List<Class<?>> newlyDiscoveredClasses = newlyDiscoveredClasses();

        while (newlyDiscoveredClasses.size() > 0) {
            for (final Class<?> newClass : newlyDiscoveredClasses) {
                internalLoadSpecification(newClass);
            }
            newlyDiscoveredClasses = newlyDiscoveredClasses();
        }
    }

    private List<Class<?>> newlyDiscoveredClasses() {
        final List<Class<?>> newlyDiscoveredClasses = new ArrayList<Class<?>>();

        final Collection<ObjectSpecification> noSpecs = allSpecifications();
        try {
            for (final ObjectSpecification noSpec : noSpecs) {
                getSpecificationTraverser().traverseReferencedClasses(noSpec, newlyDiscoveredClasses);
            }
        } catch (final ClassNotFoundException ex) {
            throw new IsisException(ex);
        }
        return newlyDiscoveredClasses;
    }

    @Override
    public void shutdown() {
        LOG.info("shutting down " + this);
        getCache().clear();
        facetDecoratorSet.shutdown();
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
        return internalLoadSpecification(type);
    }

    private ObjectSpecification internalLoadSpecification(final Class<?> type) {
        final Class<?> substitutedType = getClassSubstitutor().getClass(type);
        return substitutedType != null ? loadSpecificationForSubstitutedClass(substitutedType) : null;
    }

    private ObjectSpecification loadSpecificationForSubstitutedClass(final Class<?> type) {
        Assert.assertNotNull(type);
        final String typeName = type.getName();

        final SpecificationCacheDefault specificationCache = getCache();
        synchronized (specificationCache) {
            final ObjectSpecification spec = specificationCache.get(typeName);
            if (spec != null) {
                return spec;
            }
            final ObjectSpecification specification = createSpecification(type);
            if (specification == null) {
                throw new IsisException("Failed to create specification for class " + typeName);
            }

            // put into the cache prior to introspecting, to prevent
            // infinite loops
            specificationCache.cache(typeName, specification);

            introspectSpecificationIfRequired(specification);

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

        final AuthenticationSessionProvider authenticationSessionProvider = getRuntimeContext().getAuthenticationSessionProvider();
        final SpecificationLoader specificationLookup = getRuntimeContext().getSpecificationLoader();
        final ServicesProvider servicesProvider = getRuntimeContext().getServicesProvider();
        final ObjectInstantiator objectInstantiator = getRuntimeContext().getObjectInstantiator();

        final SpecificationContext specContext = new SpecificationContext(authenticationSessionProvider, servicesProvider, objectInstantiator, specificationLookup);

        if (FreeStandingList.class.isAssignableFrom(cls)) {
            return new ObjectSpecificationForFreeStandingList(specContext);
        } else {
            final SpecificationLoaderSpi specificationLoader = this;
            final AdapterManager adapterMap = getRuntimeContext().getAdapterManager();
            final ObjectMemberContext objectMemberContext = new ObjectMemberContext(authenticationSessionProvider, specificationLookup, adapterMap, getRuntimeContext().getQuerySubmitter(), collectionTypeRegistry);
            final IntrospectionContext introspectionContext = new IntrospectionContext(getClassSubstitutor(), getMemberLayoutArranger());
            final ServicesInjector dependencyInjector = getRuntimeContext().getDependencyInjector();
            final CreateObjectContext createObjectContext = new CreateObjectContext(adapterMap, dependencyInjector);
            final FacetedMethodsBuilderContext facetedMethodsBuilderContext = new FacetedMethodsBuilderContext(specificationLoader, classSubstitutor, specificationTraverser, facetProcessor);
            return new ObjectSpecificationDefault(cls, facetedMethodsBuilderContext, introspectionContext, specContext, objectMemberContext, createObjectContext);
        }
    }

    private Class<?> loadBuiltIn(final String className) throws ClassNotFoundException {
        final Class<?> builtIn = JavaClassUtils.getBuiltIn(className);
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

    private ObjectSpecification introspectSpecificationIfRequired(final ObjectSpecification spec) {
        if (!spec.isIntrospected()) {
            spec.introspectTypeHierarchyAndMembers();
            facetDecoratorSet.decorate(spec);
            spec.updateFromFacetValues();
        }
        return spec;
    }

    @Override
    public ObjectSpecification lookupBySpecId(ObjectSpecId objectSpecId) {
        return getCache().getByObjectType(objectSpecId);
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
            cast.setSpecificationLookup(this);
        }

        getClassSubstitutor().injectInto(candidate);
        getCollectionTypeRegistry().injectInto(candidate);
    }

    // /////////////////////////////////////////////////////////////
    // Debugging
    // /////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder str) {
        facetDecoratorSet.debugData(str);
        str.appendln();

        str.appendTitle("Specifications");
        final List<ObjectSpecification> specs = Lists.newArrayList(allSpecifications());
        Collections.sort(specs, ObjectSpecification.COMPARATOR_SHORT_IDENTIFIER_IGNORE_CASE);
        for (final ObjectSpecification spec : specs) {
            str.append(spec.isAbstract() ? "A" : ".");
            str.append(spec.isService() ? "S" : ".");
            str.append(BoundedFacetUtils.isBoundedSet(spec) ? "B" : ".");
            str.append(spec.isParentedOrFreeCollection() ? "C" : ".");
            str.append(spec.isNotCollection() ? "O" : ".");
            str.append(spec.isParseable() ? "P" : ".");
            str.append(spec.isEncodeable() ? "E" : ".");
            str.append(spec.isValueOrIsParented() ? "A" : ".");
            
            final boolean hasIdentity = !(spec.isParentedOrFreeCollection() || spec.isParented() || spec.isValue());
            str.append( hasIdentity ? "I" : ".");
            str.append("  ");
            str.append(spec.getShortIdentifier());
            str.append("  [fqc=");
            str.append(spec.getFullIdentifier());
            str.append(",type=");
            str.append(spec.getClass().getName());
            str.appendln("]");
        }
    }

    @Override
    public String debugTitle() {
        return "Reflector";
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
     * As per {@link #setRuntimeContext(RuntimeContext)}.
     */
    public RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    /**
     * Due to {@link RuntimeContextAware}.
     */
    @Override
    public void setRuntimeContext(final RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

    // ////////////////////////////////////////////////////////////////////
    // Dependencies (setters, optional)
    // ////////////////////////////////////////////////////////////////////

    public List<Class<?>> getServiceClasses() {
        return Collections.unmodifiableList(serviceClasses);
    }

    @Override
    public void setServiceClasses(final List<Class<?>> serviceClasses) {
        this.serviceClasses = serviceClasses;
    }

    // ////////////////////////////////////////////////////////////////////
    // Dependencies (injected from constructor)
    // ////////////////////////////////////////////////////////////////////

    protected IsisConfiguration getIsisConfiguration() {
        return configuration;
    }

    protected CollectionTypeRegistry getCollectionTypeRegistry() {
        return collectionTypeRegistry;
    }

    protected ClassSubstitutor getClassSubstitutor() {
        return classSubstitutor;
    }

    protected SpecificationTraverser getSpecificationTraverser() {
        return specificationTraverser;
    }

    protected ProgrammingModel getProgrammingModelFacets() {
        return programmingModel;
    }

    protected MemberLayoutArranger getMemberLayoutArranger() {
        return memberLayoutArranger;
    }

    protected MetaModelValidator getMetaModelValidator() {
        return metaModelValidator;
    }

    protected Set<FacetDecorator> getFacetDecoratorSet() {
        return facetDecoratorSet.getFacetDecorators();
    }

    @Override
    public void validateSpecifications(ValidationFailures validationFailures) {
        final Map<ObjectSpecId, ObjectSpecification> specById = Maps.newHashMap();
        for (final ObjectSpecification objSpec : allSpecifications()) {
            final ObjectSpecId objectSpecId = objSpec.getSpecId();
            if (objectSpecId == null) {
                continue;
            }
            final ObjectSpecification existingSpec = specById.put(objectSpecId, objSpec);
            if (existingSpec == null) {
                continue;
            }
            validationFailures.add("Cannot have two entities with same object type (@ObjectType facet or equivalent) Value; " + "both {0} and {1} are annotated with value of ''{2}''.", existingSpec.getFullIdentifier(), objSpec.getFullIdentifier(), objectSpecId);
        }
    }

}
