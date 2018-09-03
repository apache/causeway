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
import org.apache.isis.applib.annotation.Programmatic;
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
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

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
    }

    // -- init

    private boolean initialized = false;

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
        programmingModel.init();
        facetProcessor.init();
        postProcessor.init();
        metaModelValidator.init(this);

        loadSpecificationsForServices();
        loadSpecificationsForMixins();
        cacheBySpecId();

        initialized = true;
    }

    private void loadSpecificationsForServices() {
        for (final Class<?> serviceClass : allServiceClasses()) {
            final DomainService domainService = serviceClass.getAnnotation(DomainService.class);
            final NatureOfService nature = domainService != null ? domainService.nature() : NatureOfService.DOMAIN;
            // will 'markAsService'
            internalLoadSpecification(serviceClass, nature);
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
        for (final ObjectSpecification objSpec : allCachedSpecifications()) {
            final ObjectSpecId objectSpecId = objSpec.getSpecId();
            if (objectSpecId == null) {
                continue;
            }
            specById.put(objectSpecId, objSpec);
        }

        cache.setCacheBySpecId(specById);
    }

    @Programmatic
    public boolean isInitialized() {
        return initialized;
    }

    // -- shutdown

    @Programmatic
    public void shutdown() {
        LOG.info("shutting down {}", this);

        initialized = false;

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

        cacheBySpecId();
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

    private ObjectSpecification internalLoadSpecification(final Class<?> type) {
        // superclasses tend to be loaded via this method, implicitly.
        // what can happen is that a subclass domain service, eg a fake one such as FakeLocationLookupService
        // can be registered first prior to the "real" implementation.  As belt-n-braces, if that superclass is
        // annotated using @DomainService, then we ensure its own spec is created correctly as a service spec.
        final DomainService domainServiceIfAny = type.getAnnotation(DomainService.class);
        final NatureOfService natureOfServiceIfAny = domainServiceIfAny != null ? domainServiceIfAny.nature() : null;
        return internalLoadSpecification(type, natureOfServiceIfAny);
    }

    private ObjectSpecification internalLoadSpecification(final Class<?> type, final NatureOfService nature) {
        final Class<?> substitutedType = classSubstitutor.getClass(type);
        return substitutedType != null ? loadSpecificationForSubstitutedClass(substitutedType, nature) : null;
    }

    private ObjectSpecification loadSpecificationForSubstitutedClass(final Class<?> type, final NatureOfService nature) {
        Assert.assertNotNull(type);

        final String typeName = type.getName();
        final ObjectSpecification spec = cache.get(typeName);
        if (spec != null) {
            return spec;
        }

        return loadSpecificationForSubstitutedClassSynchronized(type, nature);
    }

    private synchronized ObjectSpecification loadSpecificationForSubstitutedClassSynchronized(
            final Class<?> type,
            final NatureOfService natureOfService) {

        final String typeName = type.getName();
        final ObjectSpecification spec = cache.get(typeName);
        if (spec != null) {
            // because caller isn't synchronized.
            return spec;
        }
        final ObjectSpecification specification = createSpecification(type, natureOfService);

        // put into the cache prior to introspecting, to prevent
        // infinite loops
        cache.cache(typeName, specification);

        introspectIfRequired(specification);

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
            final NatureOfService natureOfServiceIfAny) {

        // ... and create the specs
        if (FreeStandingList.class.isAssignableFrom(cls)) {
            return new ObjectSpecificationOnStandaloneList(servicesInjector,
                    facetProcessor);
        } else {
            final ConfigurationServiceInternal configService = servicesInjector.lookupService(
                    ConfigurationServiceInternal.class);
            final FacetedMethodsBuilderContext facetedMethodsBuilderContext =
                    new FacetedMethodsBuilderContext(
                            this, facetProcessor, configService);
            return new ObjectSpecificationDefault(cls, facetedMethodsBuilderContext,
                    servicesInjector, facetProcessor, natureOfServiceIfAny);
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
     * Typically does not need to be called, but is available for {@link FacetFactory}s to force
     * early introspection of referenced specs in certain circumstances.
     *
     * <p>
     * Originally introduced to support {@link AutoCompleteFacet}.
     */
    private ObjectSpecification introspectIfRequired(final ObjectSpecification spec) {

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
        return Lists.newArrayList(allCachedSpecifications());
    }

    private Collection<ObjectSpecification> allCachedSpecifications() {
        return cache.allSpecifications();
    }

    // -- getServiceClasses, isServiceClass

    @Programmatic
    public List<Class<?>> allServiceClasses() {
        List<Class<?>> serviceClasses = Lists
                .transform(this.servicesInjector.getRegisteredServices(), new Function<Object, Class<?>>(){
                    @Override
                    public Class<?> apply(Object o) {
                        return o.getClass();
                    }
                });
        // take a copy, to allow eg I18nFacetFactory to add in default implementations of missing services.
        return Collections.unmodifiableList(Lists.newArrayList(serviceClasses));
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
