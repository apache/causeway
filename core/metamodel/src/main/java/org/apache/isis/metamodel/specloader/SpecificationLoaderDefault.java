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
package org.apache.isis.metamodel.specloader;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.progmodel.ProgrammingModelService;
import org.apache.isis.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;
import org.apache.isis.metamodel.spec.FreeStandingList;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.isis.metamodel.specloader.specimpl.FacetedMethodsBuilderContext;
import org.apache.isis.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.metamodel.specloader.specimpl.standalonelist.ObjectSpecificationOnStandaloneList;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorService;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.schema.utils.CommonDtoUtils;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * The implementation provides for a degree of pluggability:
 * <ul>
 * <li>The most important plug-in point is {@link ProgrammingModel} that
 * specifies the set of {@link Facet} that make up programming model. If not
 * specified then defaults to {@link ProgrammingModelFacetsJava8} (which should
 * be used as a starting point for your own customizations).
 * <li>The only mandatory plug-in point is {@link ClassSubstitutor}, which
 * allows the class to be loaded to be substituted if required. This is used in
 * conjunction with some <tt>PersistenceMechanism</tt>s that do class
 * enhancement.
 * </ul>
 * </p>
 */
@Singleton
@Log4j2
public class SpecificationLoaderDefault implements SpecificationLoader {

    private final ClassSubstitutor classSubstitutor = new ClassSubstitutor();

    private ProgrammingModel programmingModel;
    private FacetProcessor facetProcessor;


    private MetaModelValidator metaModelValidator;
    private final SpecificationCacheDefault cache = new SpecificationCacheDefault();
    private PostProcessor postProcessor;

    @PostConstruct
    public void preInit() {
        this.programmingModel = programmingModelService.get();
        this.metaModelValidator = metaModelValidatorService.get();

        this.facetProcessor = new FacetProcessor(programmingModel);
        this.postProcessor = new PostProcessor(programmingModel);
    }

    /** JUnit Test Support */
    public static SpecificationLoaderDefault getInstance (
            final ProgrammingModel programmingModel,
            final MetaModelValidator metaModelValidator) {

        val instance = new SpecificationLoaderDefault(); 

        instance.programmingModel = programmingModel;
        instance.metaModelValidator = metaModelValidator;

        instance.facetProcessor = new FacetProcessor(programmingModel);
        instance.postProcessor = new PostProcessor(programmingModel);

        return instance;
    }



    // -- LIVE CYCLE

    /**
     * Initializes and wires up, and primes the cache based on any service
     * classes (provided by the {@link IsisBeanTypeRegistry}).
     */
    @Override
    public void init() {

        if (log.isDebugEnabled()) {
            log.debug("initialising {}", this);
        }

        // wire subcomponents into each other
        //facetProcessor.setServicesInjector(servicesInjector);

        // initialize subcomponents
        programmingModel.init();
        facetProcessor.init();

        postProcessor.init();
        metaModelValidator.init();


        // need to completely load services and mixins (synchronously)
        log.info("Loading all specs (up to state of {})", IntrospectionState.NOT_INTROSPECTED);

        val typeRegistry = IsisBeanTypeRegistry.current();

        val specificationsFromRegistry = _Lists.<ObjectSpecification>newArrayList();
        val domainServiceSpecs = _Lists.<ObjectSpecification>newArrayList();
        val mixinSpecs = _Lists.<ObjectSpecification>newArrayList();

        CommonDtoUtils.VALUE_TYPES.forEach(type->{
            val spec = internalLoadSpecificationOrNull(type, IntrospectionState.NOT_INTROSPECTED);
            if(spec!=null) specificationsFromRegistry.add(spec);
        });

        typeRegistry.streamAndClearInbox().forEach(entry->{

            val type = entry.getKey();
            val sort = entry.getValue(); 

            val spec = internalLoadSpecificationOrNull(type, IntrospectionState.NOT_INTROSPECTED);
            if(spec!=null) specificationsFromRegistry.add(spec);

            switch (sort) {
            case MANAGED_BEAN:
                typeRegistry.getBeanTypes().add(type);
                domainServiceSpecs.add(spec);
                return;
            case MIXIN:
                typeRegistry.getMixinTypes().add(type);
                mixinSpecs.add(spec);
                return;
            case ENTITY:
                typeRegistry.getEntityTypes().add(type);
                mixinSpecs.add(spec);
                return;
            case VIEW_MODEL:
                typeRegistry.getViewModelTypes().add(type);
                return;

            default:
                return;
            }

        });


        cache.init();

        final Collection<ObjectSpecification> cachedSpecifications = allCachedSpecifications();

        logBefore(specificationsFromRegistry, cachedSpecifications);

        log.info("Introspecting all specs up to {}", IntrospectionState.TYPE_INTROSPECTED);
        introspect(specificationsFromRegistry, IntrospectionState.TYPE_INTROSPECTED);

        log.info("Introspecting domainService specs up to {}", IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        introspect(domainServiceSpecs, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        log.info("Introspecting mixin specs up to {}", IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        introspect(mixinSpecs, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        logAfter(cachedSpecifications);

        final IntrospectionMode mode = CONFIG_PROPERTY_MODE.from(getConfiguration());
        if(mode.isFullIntrospect(_Context.getEnvironment().getDeploymentType())) {
            log.info("Introspecting all cached specs up to {}", IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
            introspect(cachedSpecifications, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        }

        log.info("init() - done");

    }

    @Override
    public void shutdown() {
        cache.clear();

        log.info("shutting down {}", this);
    }

    // -- VALIDATION

    private ValidationFailures validationFailures;

    @Override
    public ValidationFailures validate() {
        if(validationFailures == null) {
            validationFailures = new ValidationFailures();

            if(IntrospectionMode.isFullIntrospect()) {
                metaModelValidator.validate(validationFailures);
            } else {
                log.info("Meta model validation skipped (full introspection of metamodel not configured)");
            }

        }
        return validationFailures;
    }

    // -- SPEC LOADING

    @Override
    public void reloadSpecification(Class<?> domainType) {
        invalidateCache(domainType);
        loadSpecification(domainType);
    }

    @Override
    public ObjectSpecification loadSpecification(@Nullable final Class<?> type, final IntrospectionState upTo) {

        if(type==null) {
            return null;
        }

        requires(upTo, "upTo");

        val spec = internalLoadSpecificationOrNull(type, upTo);
        if(spec == null) {
            return null;
        }

        // TODO: review, is this now needed?
        //  We now create the ObjectSpecIdFacet immediately after creating the ObjectSpecification,
        //  so the cache shouldn't need updating here also.
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

    private ObjectSpecification internalLoadSpecificationOrNull(
            final Class<?> type,
            final IntrospectionState upTo) {

        final Class<?> substitutedType = classSubstitutor.getClass(type);
        if (substitutedType == null) {
            return null;
        }

        val typeName = substitutedType.getName();

        //TODO[2033] don't block on long running code ... 'specSpi.introspectUpTo(upTo);'
        synchronized (cache) {

            val spec = cache.get(typeName);
            if (spec != null) {
                if(spec instanceof ObjectSpecificationAbstract) {
                    ((ObjectSpecificationAbstract)spec).introspectUpTo(upTo);
                }
                return spec;
            }

            val specification = createSpecification(substitutedType);

            // put into the cache prior to introspecting, to prevent
            // infinite loops
            cache.cache(typeName, specification);
            specification.introspectUpTo(upTo);

            return specification;
        }
    }

    // -- LOOKUP

    @Override
    public List<ObjectSpecification> currentSpecifications() {

        final List<ObjectSpecification> defensiveCopy;

        synchronized (cache) {
            defensiveCopy = _Lists.newArrayList(allCachedSpecifications());
        }

        return defensiveCopy;
    }

    @Override
    public ObjectSpecification lookupBySpecIdElseLoad(ObjectSpecId objectSpecId) {
        if(!cache.isInitialized()) {
            throw new IllegalStateException("Internal cache not yet initialized");
        }
        val spec = cache.getByObjectType(objectSpecId);
        if(spec!=null) {
            return spec;
        }

        // fallback
        return loadSpecification(objectSpecId.asString(), IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
    }



    // -- HELPER

    private IsisConfiguration getConfiguration() {
        return _Config.getConfiguration();
    }	

    private Collection<ObjectSpecification> allCachedSpecifications() {
        return cache.allSpecifications();
    }

    /**
     * Creates the appropriate type of {@link ObjectSpecification}.
     */
    private ObjectSpecificationAbstract createSpecification(final Class<?> cls) {

        // ... and create the specs
        final ObjectSpecificationAbstract objectSpec;
        if (FreeStandingList.class.isAssignableFrom(cls)) {

            objectSpec = new ObjectSpecificationOnStandaloneList(facetProcessor, postProcessor);

        } else {

            final FacetedMethodsBuilderContext facetedMethodsBuilderContext =
                    new FacetedMethodsBuilderContext(
                            this, facetProcessor);

            val isManagedBean = IsisBeanTypeRegistry.current().isManagedBean(cls);

            objectSpec = new ObjectSpecificationDefault(cls,
                    facetedMethodsBuilderContext,
                    facetProcessor, isManagedBean, postProcessor);
        }

        return objectSpec;
    }

    //private final static _Probe probe = _Probe.unlimited().label("SpecificationLoader");

    private void logBefore(
            final List<ObjectSpecification> specificationsFromRegistry,
            final Collection<ObjectSpecification> cachedSpecifications) {
        if(!log.isDebugEnabled()) {
            return;
        }
        log.debug(String.format(
                "specificationsFromRegistry.size = %d ; cachedSpecifications.size = %d",
                specificationsFromRegistry.size(), cachedSpecifications.size()));

        List<ObjectSpecification> registryNotCached = specificationsFromRegistry.stream()
                .filter(spec -> !cachedSpecifications.contains(spec))
                .collect(Collectors.toList());
        List<ObjectSpecification> cachedNotRegistry = cachedSpecifications.stream()
                .filter(spec -> !specificationsFromRegistry.contains(spec))
                .collect(Collectors.toList());

        log.debug(String.format(
                "registryNotCached.size = %d ; cachedNotRegistry.size = %d",
                registryNotCached.size(), cachedNotRegistry.size()));
    }

    private void logAfter(final Collection<ObjectSpecification> cachedSpecifications) {
        if(!log.isDebugEnabled()) {
            return;
        }

        final Collection<ObjectSpecification> cachedSpecificationsAfter = cache.allSpecifications();
        List<ObjectSpecification> cachedAfterNotBefore = cachedSpecificationsAfter.stream()
                .filter(spec -> !cachedSpecifications.contains(spec))
                .collect(Collectors.toList());
        log.debug(String.format("cachedSpecificationsAfter.size = %d ; cachedAfterNotBefore.size = %d",
                cachedSpecificationsAfter.size(), cachedAfterNotBefore.size()));
    }

    private void introspect(final Collection<ObjectSpecification> specs, final IntrospectionState upTo) {

        val isConcurrentFromConfig = (boolean) CONFIG_PROPERTY_PARALLELIZE.from(getConfiguration());
        
        val runSequential = !isConcurrentFromConfig || true; //FIXME concurrent specloading disabled, it deadlocks
        
        if(runSequential) { 
            
            for (final ObjectSpecification specification : specs) {
                val specSpi = (ObjectSpecificationAbstract) specification;
                specSpi.introspectUpTo(upTo);
            }
            
            return;
        }
        
        specs.parallelStream()
        .map(spec -> (ObjectSpecificationAbstract) spec)
        .forEach(spec -> spec.introspectUpTo(upTo));
    }


    private void invalidateCache(final Class<?> cls) {

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

        ObjectSpecification spec = loadSpecification(substitutedType, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
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

    // -- DEPS

    private @Inject ProgrammingModelService programmingModelService;
    private @Inject MetaModelValidatorService metaModelValidatorService; 

    // -- DEPRECATED

    //	/**
    //	 * Loads the specifications of the specified types except the one specified
    //	 * (to prevent an infinite loop).
    //	 */
    //	public boolean loadSpecifications(
    //			final List<Class<?>> typesToLoad,
    //			final Class<?> typeToIgnore,
    //			final IntrospectionState upTo) {
    //
    //		boolean anyLoadedAsNull = false;
    //		for (final Class<?> typeToLoad : typesToLoad) {
    //			if (typeToLoad != typeToIgnore) {
    //				final ObjectSpecification objectSpecification =
    //						internalLoadSpecification(typeToLoad, null, upTo);
    //				final boolean loadedAsNull = (objectSpecification == null);
    //				anyLoadedAsNull = loadedAsNull || anyLoadedAsNull;
    //			}
    //		}
    //		return anyLoadedAsNull;
    //	}

    //	public ObjectSpecification peekSpecification(final Class<?> type) {
    //
    //		final Class<?> substitutedType = classSubstitutor.getClass(type);
    //		if (substitutedType == null) {
    //			return null;
    //		}
    //
    //		final String typeName = substitutedType.getName();
    //		ObjectSpecification spec = cache.get(typeName);
    //		if (spec != null) {
    //			return spec;
    //		}
    //
    //		return null;
    //	}

    //	/**
    //	 * Whether this class has been loaded.
    //	 */
    //	private boolean loaded(final Class<?> cls) {
    //		return loaded(cls.getName());
    //	}

    //	/**
    //	 * @see #loaded(Class).
    //	 */
    //	private boolean loaded(final String fullyQualifiedClassName) {
    //		return cache.get(fullyQualifiedClassName) != null;
    //	}

}
