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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Timing;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.beans.IsisBeanMetaData;
import org.apache.isis.core.config.beans.IsisBeanTypeClassifier;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.isis.core.metamodel.commons.ClassUtil;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelService;
import org.apache.isis.core.metamodel.progmodels.dflt.ProgrammingModelFacetsJava11;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutor.Substitution;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorForCollections;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.core.metamodel.valuetypes.ValueSemanticsResolverDefault;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * The implementation provides for a degree of pluggability:
 * <ul>
 * <li>The most important plug-in point is {@link ProgrammingModel} that
 * specifies the set of {@link Facet} that make up programming model. If not
 * specified then defaults to {@link ProgrammingModelFacetsJava11} (which should
 * be used as a starting point for your own customizations).
 * <li>The only mandatory plug-in point is {@link ClassSubstitutor}, which
 * allows the class to be loaded to be substituted if required. This is used in
 * conjunction with some <tt>PersistenceMechanism</tt>s that do class
 * enhancement.
 * </ul>
 * </p>
 */
@Service
@Named("isis.metamodel.SpecificationLoaderDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@Log4j2
public class SpecificationLoaderDefault implements SpecificationLoader {

    private final IsisConfiguration isisConfiguration;
    private final IsisSystemEnvironment isisSystemEnvironment;
    private final ServiceRegistry serviceRegistry;
    private final IsisBeanTypeClassifier isisBeanTypeClassifier;
    private final IsisBeanTypeRegistry isisBeanTypeRegistry;
    private final ClassSubstitutorRegistry classSubstitutorRegistry;
    private final Provider<ValueSemanticsResolver> valueSemanticsResolver;

    private final ProgrammingModel programmingModel;
    private final PostProcessor postProcessor;

    @Getter private MetaModelContext metaModelContext; // cannot inject, would cause circular dependency

    private FacetProcessor facetProcessor;

    private final SpecificationCache<ObjectSpecification> cache = new SpecificationCacheDefault<>();
    private final LogicalTypeResolver logicalTypeResolver = new LogicalTypeResolverDefault();

    /**
     * We only ever mark the meta-model as fully introspected if in {@link #isFullIntrospect() full}
     * introspection mode.
     */
    @Getter @Setter
    private boolean metamodelFullyIntrospected = false;

    @Inject
    public SpecificationLoaderDefault(
            final ProgrammingModelService programmingModelService,
            final IsisConfiguration isisConfiguration,
            final IsisSystemEnvironment isisSystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final IsisBeanTypeClassifier isisBeanTypeClassifier,
            final IsisBeanTypeRegistry isisBeanTypeRegistry,
            final Provider<ValueSemanticsResolver> valueTypeRegistry,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {
        this(
                programmingModelService.getProgrammingModel(),
                isisConfiguration,
                isisSystemEnvironment,
                serviceRegistry,
                isisBeanTypeClassifier,
                isisBeanTypeRegistry,
                valueTypeRegistry,
                classSubstitutorRegistry);
    }

    SpecificationLoaderDefault(
            final ProgrammingModel programmingModel,
            final IsisConfiguration isisConfiguration,
            final IsisSystemEnvironment isisSystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final IsisBeanTypeClassifier isisBeanTypeClassifier,
            final IsisBeanTypeRegistry isisBeanTypeRegistry,
            final Provider<ValueSemanticsResolver> valueSemanticsRegistry,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {
        this.programmingModel = programmingModel;
        this.postProcessor = new PostProcessor(programmingModel);
        this.isisConfiguration = isisConfiguration;
        this.isisSystemEnvironment = isisSystemEnvironment;
        this.serviceRegistry = serviceRegistry;
        this.isisBeanTypeClassifier = isisBeanTypeClassifier;
        this.isisBeanTypeRegistry = isisBeanTypeRegistry;
        this.valueSemanticsResolver = valueSemanticsRegistry;
        this.classSubstitutorRegistry = classSubstitutorRegistry;
    }

    /** JUnit Test Support */
    public static SpecificationLoaderDefault getInstance(
            final IsisConfiguration isisConfiguration,
            final IsisSystemEnvironment isisSystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final ProgrammingModel programmingModel,
            final IsisBeanTypeClassifier isisBeanTypeClassifier,
            final IsisBeanTypeRegistry isisBeanTypeRegistry) {

        val instance = new SpecificationLoaderDefault(
                programmingModel, isisConfiguration, isisSystemEnvironment,
                serviceRegistry, isisBeanTypeClassifier, isisBeanTypeRegistry,
                ()->new ValueSemanticsResolverDefault(List.of(), null),
                new ClassSubstitutorRegistry(List.of(
                        //new ClassSubstitutorForDomainObjects(),
                        new ClassSubstitutorForCollections(),
                        new ClassSubstitutorDefault()
                        )));

        instance.metaModelContext = serviceRegistry.lookupServiceElseFail(MetaModelContext.class);
        instance.facetProcessor = new FacetProcessor(programmingModel, instance.metaModelContext);
        instance.facetProcessor.init();

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

        val knownSpecs = _Lists.<ObjectSpecification>newArrayList();

        val stopWatch = _Timing.now();

        //XXX[ISIS-2403] these classes only get discovered by validators, so just preload their specs
        // (an optimization, not strictly required)
        loadSpecifications(ApplicationFeatureSort.class/*, ...*/);

        log.info(" - adding value types from from class-path scan and ValueTypeProviders");

        val valueTypeSpecs = _Maps.<Class<?>, ObjectSpecification>newHashMap();

        Stream
            .concat(
                isisBeanTypeRegistry.getDiscoveredValueTypes().stream(),
                valueSemanticsResolver.get().streamClassesWithValueSemantics())
            .forEach(valueType -> {
                val valueSpec = loadSpecification(valueType, IntrospectionState.NOT_INTROSPECTED);
                if(valueSpec!=null) {
                    knownSpecs.add(valueSpec);
                    valueTypeSpecs.put(valueType, valueSpec);
                }
            });

        log.info(" - categorizing types from class-path scan");

        val domainObjectSpecs = _Lists.<ObjectSpecification>newArrayList();
        val mixinSpecs = _Lists.<ObjectSpecification>newArrayList();

        isisBeanTypeRegistry.streamIntrospectableTypes()
        .forEach(type->{

            val cls = type.getCorrespondingClass();
            val sort = type.getBeanSort();

            val spec = primeSpecification(cls, sort);
            if(spec==null) {
                //XXX only ever happens when the class substitutor vetoes
                return;
            }

            knownSpecs.add(spec);

            if(sort.isManagedBean() || sort.isEntity() || sort.isViewModel() ) {
                domainObjectSpecs.add(spec);
            } else if(sort.isMixin()) {
                mixinSpecs.add(spec);
            }

        });

        //XXX[ISIS-2382] when parallel introspecting, make sure we have the mixins before their holders

        SpecificationLoaderDefault_debug.logBefore(log, cache, knownSpecs);

        log.info(" - introspecting {} type hierarchies", knownSpecs.size());
        introspect(Can.ofCollection(knownSpecs), IntrospectionState.TYPE_INTROSPECTED);

        log.info(" - introspecting {} value types", valueTypeSpecs.size());
        introspect(Can.ofCollection(valueTypeSpecs.values()), IntrospectionState.FULLY_INTROSPECTED);

        log.info(" - introspecting {} mixins", isisBeanTypeRegistry.getMixinTypes().size());
        introspect(Can.ofCollection(mixinSpecs), IntrospectionState.FULLY_INTROSPECTED);

        log.info(" - introspecting {} managed beans contributing (domain services)", isisBeanTypeRegistry.getManagedBeansContributing().size());
//        log.info(" - introspecting {}/{} entities (JDO/JPA)",
//                isisBeanTypeRegistry.getEntityTypesJdo().size(),
//                isisBeanTypeRegistry.getEntityTypesJpa().size());

        log.info(" - introspecting {} entities (JDO+JPA)",
                isisBeanTypeRegistry.getEntityTypes().size());
        log.info(" - introspecting {} view models", isisBeanTypeRegistry.getViewModelTypes().size());

        serviceRegistry.lookupServiceElseFail(MenuBarsService.class).menuBars();

        introspect(Can.ofCollection(domainObjectSpecs), IntrospectionState.FULLY_INTROSPECTED);

        SpecificationLoaderDefault_debug.logAfter(log, cache, knownSpecs);

        if(isFullIntrospect()) {
            val snapshot = cache.snapshotSpecs();
            log.info(" - introspecting all {} types eagerly (FullIntrospect=true)", snapshot.size());
            introspect(snapshot.filter(x->x.getBeanSort().isMixin()), IntrospectionState.FULLY_INTROSPECTED);
            introspect(snapshot.filter(x->!x.getBeanSort().isMixin()), IntrospectionState.FULLY_INTROSPECTED);
        }

        log.info(" - running remaining validators");
        _Blackhole.consume(getOrAssessValidationResult()); // as a side effect memoizes the validation result

        stopWatch.stop();
        log.info("Metamodel created in " + stopWatch.getMillis() + " ms.");

        if(isFullIntrospect()) {
            setMetamodelFullyIntrospected(true);
        }
    }

    @Override
    public Optional<ValidationFailures> getValidationResult() {
        return validationResult.getMemoized();
    }

    @Override
    public ValidationFailures getOrAssessValidationResult() {
        return validationResult.get();
    }

    @Override
    public void disposeMetaModel() {
        logicalTypeResolver.clear();
        cache.clear();
        validationResult.clear();
        serviceRegistry.clearRegisteredBeans();
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
    public void reloadSpecification(final Class<?> domainType) {
        invalidateCache(domainType);
        loadSpecification(domainType, IntrospectionState.FULLY_INTROSPECTED);
    }

    @Override
    public boolean loadSpecifications(final Class<?>... domainTypes) {
        // ensure that all types are loadable
        if (Arrays.stream(domainTypes)
                .map(classSubstitutorRegistry::getSubstitution)
                .anyMatch(Substitution::isNeverIntrospect)) {
            return false;
        }
        Arrays.stream(domainTypes).forEach(this::loadSpecification);
        return true;
    }

    @Override @Nullable
    public ObjectSpecification loadSpecification(
            final @Nullable Class<?> type,
            final @NonNull IntrospectionState upTo) {

        return _loadSpecification(
                type,
                __->isisBeanTypeRegistry
                    .lookupIntrospectableType(type)
                    .map(IsisBeanMetaData::getBeanSort)
                    .orElseGet(()->
                        valueSemanticsResolver.get().hasValueSemantics(type)
                        ? BeanSort.VALUE
                        : isisBeanTypeClassifier.classify(type)
                                .getBeanSort()
                    ),
                upTo);
    }

    @Override
    public void validateLater(final ObjectSpecification objectSpec) {
        if(!isMetamodelFullyIntrospected()) {
            // don't trigger validation during bootstrapping
            // getValidationResult() is lazily populated later on first request anyway
            return;
        }
        if(!isisConfiguration.getCore().getMetaModel().getIntrospector().isValidateIncrementally()) {
            // re-validation after the initial one can be turned off by means of above config option
            return;
        }

        log.info("re-validation triggered by {}", objectSpec);

        // validators might discover new specs
        // to prevent deadlocks, we queue up validation requests to be processed later
        if(validationInProgress.get()) {
            _Assert.assertTrue(validationQueue.offer(objectSpec),
                    "The Validation Queue is expected to never deadlock or grow beyond its capacity.");
            return;
        }

        while(validationQueue.poll()!=null) {
            // keep re-validating until the queue is empty
            validationQueue.clear(); // shortcut
            validationResult.clear(); // invalidate
            // potentially triggers a call to the method we are currently in,
            // which adds more entries to the validationQueue
            getOrAssessValidationResult();
        }

        // only after things have settled we offer feedback to the user (interface)

        final ValidationFailures validationFailures = getOrAssessValidationResult();
        if(validationFailures.hasFailures()) {
            throw _Exceptions.illegalState(String.join("\n", validationFailures.getMessages("[%d] %s")));
        }

    }

    // -- LOOKUP

    @Override
    public Can<ObjectSpecification> snapshotSpecifications() {
        return cache.snapshotSpecs();
    }

    @Override
    public void forEach(final Consumer<ObjectSpecification> onSpec) {
        val shouldRunConcurrent = isisConfiguration.getCore().getMetaModel().getValidator().isParallelize();
        cache.forEach(onSpec, shouldRunConcurrent);
    }

    @Override
    public Optional<LogicalType> lookupLogicalType(final @NonNull String logicalTypeName) {
        val logicalType = logicalTypeResolver.lookup(logicalTypeName);
        if(logicalType.isPresent()) {
            return logicalType;
        }

        //TODO[2533] if the logicalTypeName is not available and instead a fqcn was passed in, that should also be supported

        // falling back assuming the logicalTypeName equals the fqn of the corresponding class
        // which might not always be true,

        val cls = ClassUtil.forNameElseNull(logicalTypeName);
        if(cls!=null) {

//TODO yet it seems we rely on this kind of fallback from several code paths, so lets not emit any warnings yet ...
//            log.warn("Lookup for ObjectType '{}' failed, but found a matching fully qualified "
//                    + "class name to use instead. This warning is an indicator, that {} is not "
//                    + "discovered by Spring during bootstrapping of this application.",
//                    logicalType.getName(),
//                    cls.getName());
            return Optional.of(LogicalType.fqcn(cls));
        }

        return Optional.empty();
    }

    // -- VALIDATION STUFF

    private final ValidationFailures validationFailures = new ValidationFailures();

    @Override
    public void addValidationFailure(final ValidationFailure validationFailure) {
//        if(validationResult.isMemoized()) {
//            validationResult.clear(); // invalidate
////            throw _Exceptions.illegalState(
////                    "Validation result was already created and can no longer be modified.");
//        }
        synchronized(validationFailures) {
            validationFailures.add(validationFailure);
        }
    }

    private _Lazy<ValidationFailures> validationResult =
            _Lazy.threadSafe(this::collectFailuresFromMetaModel);

    private final AtomicBoolean validationInProgress = new AtomicBoolean(false);
    private final BlockingQueue<ObjectSpecification> validationQueue = new LinkedBlockingQueue<>();

    private ValidationFailures collectFailuresFromMetaModel() {
        validationInProgress.set(true);

        programmingModel.streamValidators()
        .map(MetaModelValidatorAbstract.class::cast)
        .forEach(validator -> {
            log.debug("Running validator: {}", validator);
            try {
                validator.validate();
            } catch (Throwable t) {
                log.error(t);
                throw t;
            } finally {
                log.debug("Done validator: {}", validator);
            }
        });

        log.debug("Done");
        validationInProgress.set(false);

        return validationFailures;
    }


    // -- HELPER

    @Nullable
    private ObjectSpecification primeSpecification(
            final @Nullable Class<?> type,
            final @NonNull BeanSort sort) {
        return _loadSpecification(type, __->sort, IntrospectionState.NOT_INTROSPECTED);

    }

    @Nullable
    private ObjectSpecification _loadSpecification(
            final @Nullable Class<?> type,
            final @NonNull Function<Class<?>, BeanSort> beanClassifier,
            final @NonNull IntrospectionState upTo) {

        if(type==null) {
            return null;
        }

        val substitute = classSubstitutorRegistry.getSubstitution(type);
        if (substitute.isNeverIntrospect()) {
            return null; // never inspect
        }

        val substitutedType = substitute.apply(type);

        final ObjectSpecification spec = cache.computeIfAbsent(substitutedType, __->{
            val newSpec = createSpecification(substitutedType, beanClassifier.apply(substitutedType));
            logicalTypeResolver.register(newSpec);
            return newSpec;
        });

        spec.introspectUpTo(upTo);

        return spec;
    }

    private void guardAgainstMetamodelLockedAfterFullIntrospection(final Class<?> cls) {
        if(isMetamodelFullyIntrospected()
                && isisConfiguration.getCore().getMetaModel().getIntrospector().isLockAfterFullIntrospection()) {

            val sort = isisBeanTypeClassifier
                    .classify(cls)
                    .getBeanSort();

//          ISIS-2256:
//            throw _Exceptions.illegalState(
//                    "Cannot introspect class '%s' of sort %s, because the metamodel has been fully introspected and is now locked. " +
//                    "One reason this can happen is if you are attempting to invoke an action through the WrapperFactory " +
//                    "on a service class incorrectly annotated with Spring's @Service annotation instead of " +
//                    "@DomainService.",
//                    cls.getName(), sort);

            log.warn("Missed class '{}' when the metamodel was fully introspected.", cls.getName());

            if(sort.isValue()) {
                return; // opinionated: just relax when value
            }

            if(sort.isToBeIntrospected()) {
                log.error("Introspecting class '{}' of sort {}, after the metamodel had been fully introspected and is now locked. " +
                      "One reason this can happen is if you are attempting to invoke an action through the WrapperFactory " +
                      "on a service class incorrectly annotated with Spring's @Service annotation instead of " +
                      "@DomainService.",
                        cls.getName(), sort);
            }
        }
    }

    /**
     * Creates the appropriate type of {@link ObjectSpecification}.
     */
    private ObjectSpecification createSpecification(final Class<?> cls, final BeanSort beanSort) {

        guardAgainstMetamodelLockedAfterFullIntrospection(cls);

        // ... and create the specs

        val objectSpec = new ObjectSpecificationDefault(
                        cls,
                        beanSort,
                        metaModelContext,
                        facetProcessor,
                        isisBeanTypeRegistry.lookupManagedBeanNameForType(cls).orElse(null),
                        postProcessor,
                        classSubstitutorRegistry);

        return objectSpec;
    }

    private void introspectSequential(
            final Can<ObjectSpecification> specs,
            final IntrospectionState upTo) {
        for (val spec : specs) {
            spec.introspectUpTo(upTo);
        }
    }

    private void introspectParallel(
            final Can<ObjectSpecification> specs,
            final IntrospectionState upTo) {
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

    private void introspect(
            final Can<ObjectSpecification> specs,
            final IntrospectionState upTo) {
        val isConcurrentFromConfig = isisConfiguration.getCore().getMetaModel().getIntrospector().isParallelize();
        if(isConcurrentFromConfig) {
            introspectParallel(specs, upTo);
        } else {
            introspectSequential(specs, upTo);
        }
    }

    private void invalidateCache(final Class<?> cls) {

        val substitute = classSubstitutorRegistry.getSubstitution(cls);
        if(substitute.isNeverIntrospect()) {
            return;
        }

        ObjectSpecification spec =
                loadSpecification(substitute.apply(cls), IntrospectionState.FULLY_INTROSPECTED);

        while(spec != null) {
            val type = spec.getCorrespondingClass();
            cache.remove(type);
            spec = spec.superclass();
        }
    }



}
