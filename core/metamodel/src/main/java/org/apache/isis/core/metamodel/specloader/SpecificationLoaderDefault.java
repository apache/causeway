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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.commons.internal.base._Blackhole;
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.commons.internal.base._Timing;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistryHolder;
import org.apache.isis.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.metamodel.specloader.specimpl.standalonelist.ObjectSpecificationOnStandaloneList;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelService;
import org.apache.isis.core.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorForCollections;
import org.apache.isis.core.metamodel.spec.FreeStandingList;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.valuetypes.ValueTypeProviderDefault;
import org.apache.isis.core.metamodel.valuetypes.ValueTypeRegistry;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.Getter;
import lombok.Setter;
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
@Service
@Named("isisMetaModel.SpecificationLoaderDefault")
@Order(OrderPrecedence.HIGH)
@Primary
@Qualifier("Default")
@Log4j2
public class SpecificationLoaderDefault implements SpecificationLoader {

    private final IsisConfiguration isisConfiguration;
    private final IsisSystemEnvironment isisSystemEnvironment;
    private final ServiceRegistry serviceRegistry;
    private final IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder;
    private final ClassSubstitutorRegistry classSubstitutorRegistry;
    private final ValueTypeRegistry valueTypeRegistry;

    private final ProgrammingModel programmingModel;
    private final PostProcessor postProcessor;

    @Getter private MetaModelContext metaModelContext; // cannot inject, would cause circular dependency

    private FacetProcessor facetProcessor;

    private final SpecificationCacheDefault<ObjectSpecification> cache = new SpecificationCacheDefault<>();

    /**
     * We only ever mark the metamodel as fully introspected if in {@link #isFullIntrospect() full} introspection mode.
     */
    @Getter @Setter
    private boolean metamodelFullyIntrospected = false;

    @Inject
    public SpecificationLoaderDefault(
            final ProgrammingModelService programmingModelService,
            final IsisConfiguration isisConfiguration,
            final IsisSystemEnvironment isisSystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder,
            final ValueTypeRegistry valueTypeRegistry,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {
        this(
                programmingModelService.getProgrammingModel(),
                isisConfiguration,
                isisSystemEnvironment,
                serviceRegistry,
                isisBeanTypeRegistryHolder, valueTypeRegistry, classSubstitutorRegistry);
    }

    SpecificationLoaderDefault(
            final ProgrammingModel programmingModel,
            final IsisConfiguration isisConfiguration,
            final IsisSystemEnvironment isisSystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder,
            final ValueTypeRegistry valueTypeRegistry,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {
        this.programmingModel = programmingModel;
        this.postProcessor = new PostProcessor(programmingModel);
        this.isisConfiguration = isisConfiguration;
        this.isisSystemEnvironment = isisSystemEnvironment;
        this.serviceRegistry = serviceRegistry;
        this.isisBeanTypeRegistryHolder = isisBeanTypeRegistryHolder;
        this.valueTypeRegistry = valueTypeRegistry;
        this.classSubstitutorRegistry = classSubstitutorRegistry;
    }

    /** JUnit Test Support */
    public static SpecificationLoaderDefault getInstance(
            final IsisConfiguration isisConfiguration,
            final IsisSystemEnvironment isisSystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final ProgrammingModel programmingModel,
            final IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder) {

        val instance = new SpecificationLoaderDefault(
                programmingModel, isisConfiguration, isisSystemEnvironment,
                serviceRegistry, isisBeanTypeRegistryHolder,
                new ValueTypeRegistry(Collections.singletonList(new ValueTypeProviderDefault())),
                new ClassSubstitutorRegistry(Collections.unmodifiableList(Arrays.asList(new ClassSubstitutorDefault(), new ClassSubstitutorForCollections()))));

        instance.metaModelContext = serviceRegistry.lookupServiceElseFail(MetaModelContext.class);
        instance.facetProcessor = new FacetProcessor(programmingModel, instance.metaModelContext);

        return instance;
    }

    // -- LIVE CYCLE

    @PostConstruct
    public void init() {
        if (log.isDebugEnabled()) {
            log.debug("initialising {}", this);
        }
        this.metaModelContext = serviceRegistry.lookupServiceElseFail(MetaModelContext.class);
        this.facetProcessor = new FacetProcessor(programmingModel, metaModelContext);
    }
    
    /**
     * Initializes and wires up, and primes the cache based on any service
     * classes (provided by the {@link IsisBeanTypeRegistry}).
     */
    @Override
    public void createMetaModel() {
        
        log.info("About to create the Metamodel ...");
        
        // initialize subcomponents, only after @PostConstruct has globally completed
        facetProcessor.init();
        postProcessor.init();

        val typeRegistry = isisBeanTypeRegistryHolder.getIsisBeanTypeRegistry();

        val scannedSpecs = _Lists.<ObjectSpecification>newArrayList();
        val domainServiceSpecs = _Lists.<ObjectSpecification>newArrayList();
        val domainObjectSpecs = _Lists.<ObjectSpecification>newArrayList();

        valueTypeRegistry.classes().forEach(clazz -> {
            val spec = loadSpecification(clazz, IntrospectionState.NOT_INTROSPECTED);
            if(spec!=null) {
                scannedSpecs.add(spec);
            }
        });

        log.info(" - categorizing types from class-path scan");
        
        typeRegistry.snapshotIntrospectableTypes().entrySet()
        .forEach(entry->{

            val type = entry.getKey();
            val sort = entry.getValue(); 

            val spec = loadSpecification(type, IntrospectionState.NOT_INTROSPECTED);
            if(spec!=null) {
                scannedSpecs.add(spec);
            } else {
                typeRegistry.veto(type);
            }

            switch (sort) {
            case MANAGED_BEAN:
                domainServiceSpecs.add(spec);
                return;
            case MIXIN:
            case ENTITY:
            case VIEW_MODEL:
                domainObjectSpecs.add(spec);
                return;

            default:
                return;
            }

        });

        val stopWatch = _Timing.now();
        

        SpecificationLoaderDefault_debug.logBefore(log, cache, scannedSpecs);

        log.info(" - introspecting {} type hierarchies", scannedSpecs.size());
        introspect(scannedSpecs, IntrospectionState.TYPE_INTROSPECTED);

        log.info(" - introspecting {} domain services", domainServiceSpecs.size());
        introspect(domainServiceSpecs, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        log.info(" - introspecting {} mixins", typeRegistry.getMixinTypes().size());
        log.info(" - introspecting {} entities", typeRegistry.getEntityTypes().size());
        log.info(" - introspecting {} view models", typeRegistry.getViewModelTypes().size());
        introspect(domainObjectSpecs, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        SpecificationLoaderDefault_debug.logAfter(log, cache, scannedSpecs);

        if(isFullIntrospect()) {
            val snapshot = cache.snapshotSpecs();
            log.info(" - introspecting all {} types eagerly (FullIntrospect=true)", snapshot.size());
            introspect(snapshot, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        }
        
        log.info(" - running remaining validators");
        _Blackhole.consume(getValidationResult()); // as a side effect memoizes the validation result

        stopWatch.stop();
        log.info("Metamodel created in " + (long)stopWatch.getMillis() + " ms.");

        if(isFullIntrospect()) {
            setMetamodelFullyIntrospected(true);
        }
    }
    
    @Override
    public ValidationFailures getValidationResult() {
        return validationResult.get();
    }

    private _Lazy<ValidationFailures> validationResult = 
            _Lazy.threadSafe(this::collectFailuresFromMetaModel);

    private ValidationFailures collectFailuresFromMetaModel() {
        val failures = new ValidationFailures();
        programmingModel.streamValidators()
        .map(MetaModelValidatorAbstract.class::cast)
        .forEach(validator -> {
            log.debug("Running validator: {}", validator);
            try {
                validator.collectFailuresInto(failures);
            } catch (Throwable t) {
                log.error(t);
                throw t;
            } finally {
                log.debug("Done validator: {}", validator);
            }
        });
        log.debug("Done");
        return failures;
    }

    @Override
    public void disposeMetaModel() {
        cache.clear();
        validationResult.clear();
        log.info("Metamodel disposed.");
    }
    
    @PreDestroy
    public void shutdown() {
        log.debug("shutting down {}", this);
        disposeMetaModel();
        facetProcessor.shutdown();
        postProcessor.shutdown();
        facetProcessor = null;
    }

    /**
     * @return whether current introspection mode is 'full', dependent on current
     * deployment mode and configuration
     */
    private boolean isFullIntrospect() {
        return IntrospectionMode.isFullIntrospect(isisConfiguration, isisSystemEnvironment);
    }

    // -- SPEC LOADING

    @Override
    public void reloadSpecification(Class<?> domainType) {
        invalidateCache(domainType);
        loadSpecification(domainType, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
    }

    @Override
    public boolean loadSpecifications(Class<?>... domainTypes) {
        // ensure that all types are loadable
        if (Arrays.stream(domainTypes)
                .map(classSubstitutorRegistry::getClass)
                .anyMatch(Objects::isNull)) {
            return false;
        }
        Arrays.stream(domainTypes).forEach(this::loadSpecification);
        return true;
    }

    @Override @Nullable
    public ObjectSpecification loadSpecification(
            @Nullable final Class<?> type, 
            final IntrospectionState upTo) {

        if(type==null) {
            return null;
        }

        requires(upTo, "upTo");

        val substitutedType = classSubstitutorRegistry.getClass(type);
        if (substitutedType == null) {
            return null;
        }
        
        val typeName = substitutedType.getName();

        final ObjectSpecification cachedSpec;
        
        // we try not to block on long running code ... 'spec.introspectUpTo(upTo);'
        synchronized (cache) {
            cachedSpec = cache.computeIfAbsent(typeName, __->createSpecification(substitutedType));
        }

        cachedSpec.introspectUpTo(upTo);

        return cachedSpec;
    }

    public void revalidateIfNecessary() {
        if(!isMetamodelFullyIntrospected()) {
            return;
        }
        if(!this.isisConfiguration.getReflector().getIntrospector().isValidateIncrementally()) {
            return;
        }

        if (this.validationResult.isMemoized()) {
            this.validationResult.clear();
            final ValidationFailures validationFailures = this.getValidationResult();

            if(validationFailures.hasFailures()) {
                throw _Exceptions.illegalState(String.join("\n", validationFailures.getMessages("[%d] %s")));
            }
        }
    }

    // -- LOOKUP

    @Override
    public Collection<ObjectSpecification> snapshotSpecifications() {
        return cache.snapshotSpecs();
    }
    
    @Override
    public void forEach(Consumer<ObjectSpecification> onSpec) {
        val shouldRunConcurrent = isisConfiguration.getReflector().getValidator().isParallelize();
        val vList = cache.getVList(); // vList is thread-safe
        if(shouldRunConcurrent) {
            vList.forEachParallel(onSpec);    
        } else {
            vList.forEach(onSpec);
        }
        
    }

    @Override
    public ObjectSpecification lookupBySpecIdElseLoad(ObjectSpecId objectSpecId) {
        val spec = cache.getByObjectType(objectSpecId);
        if(spec!=null) {
            return spec;
        }
        // fallback
        return loadSpecification(objectSpecId, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
    }
    
    @Override
    public Class<?> lookupType(ObjectSpecId objectSpecId) {
        return cache.resolveType(objectSpecId);
    }


    // -- HELPER
    
    /**
     * Creates the appropriate type of {@link ObjectSpecification}.
     */
    private ObjectSpecification createSpecification(final Class<?> cls) {

        if(isMetamodelFullyIntrospected() && isisConfiguration.getReflector().getIntrospector().isLockAfterFullIntrospection()) {
            throw _Exceptions.illegalState(
                    "Cannot introspect class '%s' because the metamodel has been fully introspected and is now locked. " +
                    "One reason this can happen is if you are attempting to invoke an action through the WrapperFactory " +
                    "on a service class incorrectly annotated with Spring's @Service annotation instead of " +
                    "@DomainService.",
                    cls.getName());
        }

        // ... and create the specs
        final ObjectSpecificationAbstract objectSpec;
        if (FreeStandingList.class.isAssignableFrom(cls)) {

            objectSpec = new ObjectSpecificationOnStandaloneList(facetProcessor, postProcessor);
            objectSpec.setMetaModelContext(metaModelContext);

        } else {
            
            val typeRegistry = isisBeanTypeRegistryHolder.getIsisBeanTypeRegistry();

            val managedBeanNameIfAny = typeRegistry.getManagedBeanNameForType(cls);
            objectSpec = new ObjectSpecificationDefault(
                            cls,
                            metaModelContext,
                            facetProcessor,
                            managedBeanNameIfAny.orElse(null),
                            postProcessor,
                            classSubstitutorRegistry);
        }

        return objectSpec;
    }

    private void introspect(
            final Collection<ObjectSpecification> specs, 
            final IntrospectionState upTo) {

        val isConcurrentFromConfig = isisConfiguration.getReflector().getIntrospector().isParallelize();

        val runSequential = !isConcurrentFromConfig;
        
        if(runSequential) { 
            
            for (val spec : specs) {
                spec.introspectUpTo(upTo);
            }
            
            return; // sequential run done
        }
        
        specs.parallelStream()
        .forEach(spec -> {
            try {
                spec.introspectUpTo(upTo);
            } catch (Throwable ex) {
                log.error(ex);
                throw ex;
            }
        });

    }

    private void invalidateCache(final Class<?> cls) {

        val substitutedType = classSubstitutorRegistry.getClass(cls);

        ObjectSpecification spec = 
                loadSpecification(substitutedType, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        
        while(spec != null) {
            val type = spec.getCorrespondingClass();
            cache.remove(type.getName());
            spec = spec.superclass();
        }
    }


}
