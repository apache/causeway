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
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Timing;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.beans.IsisBeanTypeRegistryHolder;
import org.apache.isis.config.registry.types.IsisBeanTypeRegistry;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.progmodel.ProgrammingModelService;
import org.apache.isis.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;
import org.apache.isis.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.isis.metamodel.spec.FreeStandingList;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.isis.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.isis.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.metamodel.specloader.specimpl.standalonelist.ObjectSpecificationOnStandaloneList;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.schema.utils.CommonDtoUtils;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import static org.apache.isis.commons.internal.base._With.requires;

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
@Log4j2
public class SpecificationLoaderDefault implements SpecificationLoader {

    @Inject private ProgrammingModelService programmingModelService;
    @Inject private IsisConfiguration isisConfiguration;
    @Inject private IsisSystemEnvironment isisSystemEnvironment;
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder;
    @Inject private ClassSubstitutor classSubstitutor = new ClassSubstitutorDefault();  // default for testing purposes only, overwritten in prod

    private ProgrammingModel programmingModel;
    private FacetProcessor facetProcessor;
    private PostProcessor postProcessor;
    @Getter private MetaModelContext metaModelContext; // cannot inject, would cause circular dependency
    
    private final SpecificationCacheDefault<ObjectSpecification> cache = 
            new SpecificationCacheDefault<>();

    /** JUnit Test Support */
    public static SpecificationLoaderDefault getInstance (
            IsisConfiguration isisConfiguration,
            IsisSystemEnvironment isisSystemEnvironment,
            ServiceRegistry serviceRegistry,
            ServiceInjector serviceInjector,
            ProgrammingModel programmingModel,
            IsisBeanTypeRegistryHolder isisBeanTypeRegistryHolder) {

        val instance = new SpecificationLoaderDefault(); 

        instance.metaModelContext = serviceRegistry.lookupServiceElseFail(MetaModelContext.class);
        
        instance.isisConfiguration = isisConfiguration;
        instance.isisSystemEnvironment = isisSystemEnvironment;
        instance.serviceRegistry = serviceRegistry;
        instance.programmingModel = programmingModel;

        instance.facetProcessor = new FacetProcessor(programmingModel, instance.metaModelContext);
        instance.postProcessor = new PostProcessor(programmingModel);
        
        instance.isisBeanTypeRegistryHolder = isisBeanTypeRegistryHolder;
        
        
        return instance;
    }

    // -- LIVE CYCLE

    @PostConstruct
    public void init() {
        if (log.isDebugEnabled()) {
            log.debug("initialising {}", this);
        }
        this.metaModelContext = serviceRegistry.lookupServiceElseFail(MetaModelContext.class);
        this.programmingModel = programmingModelService.getProgrammingModel();
        this.facetProcessor = new FacetProcessor(programmingModel, metaModelContext);
        this.postProcessor = new PostProcessor(programmingModel);
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

        CommonDtoUtils.VALUE_TYPES.forEach(type->{
            val spec = loadSpecification(type, IntrospectionState.NOT_INTROSPECTED);
            if(spec!=null) scannedSpecs.add(spec);
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
        log.info("Metamodel created. (took " + stopWatch + ")");

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
        .forEach(validator->validator.collectFailuresInto(failures));
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
        postProcessor = null;
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

    @Override @Nullable
    public ObjectSpecification loadSpecification(@Nullable final Class<?> type, final IntrospectionState upTo) {

        if(type==null) {
            return null;
        }

        requires(upTo, "upTo");

        val substitutedType = classSubstitutor.getClass(type);
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

    // -- LOOKUP

    @Override
    public Collection<ObjectSpecification> snapshotSpecifications() {
        return cache.snapshotSpecs();
    }
    
    @Override
    public void forEach(Consumer<ObjectSpecification> onSpec) {
        val shouldRunConcurrent = isisConfiguration.getReflector().getIntrospector().isParallelize();
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
                    managedBeanNameIfAny, 
                    postProcessor, classSubstitutor);
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
        .forEach(spec -> spec.introspectUpTo(upTo)); // TODO swallows exceptions that happen inside (makes debugging hard)
        
    }

    private void invalidateCache(final Class<?> cls) {

        val substitutedType = classSubstitutor.getClass(cls);

        ObjectSpecification spec = 
                loadSpecification(substitutedType, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        
        while(spec != null) {
            val type = spec.getCorrespondingClass();
            cache.remove(type.getName());
            spec = spec.superclass();
        }
    }


}
