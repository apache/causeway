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
package org.apache.causeway.core.metamodel.specloader;

import java.util.Arrays;
import java.util.Collections;
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

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Blackhole;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._Timing;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.config.beans.CausewayBeanTypeClassifier;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel.PreloadableTypes;
import org.apache.causeway.core.metamodel.commons.ClassUtil;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelService;
import org.apache.causeway.core.metamodel.progmodels.dflt.ProgrammingModelFacetsJava11;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutor.Substitution;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.causeway.core.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.causeway.core.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.causeway.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.causeway.core.metamodel.valuetypes.ValueSemanticsResolverDefault;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
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
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".SpecificationLoaderDefault")
@Priority(PriorityPrecedence.EARLY)
@Qualifier("Default")
@Log4j2
public class SpecificationLoaderDefault implements SpecificationLoader {

    private final CausewayConfiguration causewayConfiguration;
    private final CausewaySystemEnvironment causewaySystemEnvironment;
    private final ServiceRegistry serviceRegistry;
    private final CausewayBeanTypeClassifier causewayBeanTypeClassifier;
    private final CausewayBeanTypeRegistry causewayBeanTypeRegistry;
    private final ClassSubstitutorRegistry classSubstitutorRegistry;
    private final Provider<ValueSemanticsResolver> valueSemanticsResolver;
    private final ProgrammingModel programmingModel;
    private final PostProcessor postProcessor;

    @Inject
    public List<PreloadableTypes> preloadableTypes = Collections.emptyList();

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
            final CausewayConfiguration causewayConfiguration,
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final CausewayBeanTypeClassifier causewayBeanTypeClassifier,
            final CausewayBeanTypeRegistry causewayBeanTypeRegistry,
            final Provider<ValueSemanticsResolver> valueTypeRegistry,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {
        this(
                programmingModelService.getProgrammingModel(),
                causewayConfiguration,
                causewaySystemEnvironment,
                serviceRegistry,
                causewayBeanTypeClassifier,
                causewayBeanTypeRegistry,
                valueTypeRegistry,
                classSubstitutorRegistry);
    }

    SpecificationLoaderDefault(
            final ProgrammingModel programmingModel,
            final CausewayConfiguration causewayConfiguration,
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final CausewayBeanTypeClassifier causewayBeanTypeClassifier,
            final CausewayBeanTypeRegistry causewayBeanTypeRegistry,
            final Provider<ValueSemanticsResolver> valueSemanticsRegistry,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {
        this.programmingModel = programmingModel;
        this.postProcessor = new PostProcessor(programmingModel);
        this.causewayConfiguration = causewayConfiguration;
        this.causewaySystemEnvironment = causewaySystemEnvironment;
        this.serviceRegistry = serviceRegistry;
        this.causewayBeanTypeClassifier = causewayBeanTypeClassifier;
        this.causewayBeanTypeRegistry = causewayBeanTypeRegistry;
        this.valueSemanticsResolver = valueSemanticsRegistry;
        this.classSubstitutorRegistry = classSubstitutorRegistry;
    }

    /** JUnit Test Support */
    public static SpecificationLoaderDefault getInstance(
            final CausewayConfiguration causewayConfiguration,
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final ProgrammingModel programmingModel,
            final CausewayBeanTypeClassifier causewayBeanTypeClassifier,
            final CausewayBeanTypeRegistry causewayBeanTypeRegistry,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {

        val instance = new SpecificationLoaderDefault(
                programmingModel, causewayConfiguration, causewaySystemEnvironment,
                serviceRegistry, causewayBeanTypeClassifier, causewayBeanTypeRegistry,
                ()->new ValueSemanticsResolverDefault(List.of(), null),
                classSubstitutorRegistry);

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
     * classes (provided by the {@link CausewayBeanTypeRegistry}).
     */
    @Override
    public void createMetaModel() {

        log.info("About to create the Metamodel ...");

        // initialize subcomponents, only after @PostConstruct has globally completed
        facetProcessor.init();
        postProcessor.init();

        val knownSpecs = _Lists.<ObjectSpecification>newArrayList();

        val stopWatch = _Timing.now();

        // preload otherwise not eagerly discovered classes
        val prealoadCount = preloadableTypes.stream()
            .flatMap(PreloadableTypes::stream)
            .peek(this::loadSpecification)
            .count();

        log.info(" - preloaded {} otherwise not eagerly discovered types", prealoadCount);

        log.info(" - adding value types from from class-path scan and ValueTypeProviders");

        val valueTypeSpecs = _Maps.<Class<?>, ObjectSpecification>newHashMap();

        Stream
            .concat(
                causewayBeanTypeRegistry.getDiscoveredValueTypes().keySet().stream(),
                valueSemanticsResolver.get().streamClassesWithValueSemantics())
            .forEach(valueClass -> {
                val valueSpec = loadSpecification(valueClass, IntrospectionState.NOT_INTROSPECTED);
                if(valueSpec!=null) {
                    knownSpecs.add(valueSpec);
                    valueTypeSpecs.put(valueClass, valueSpec);
                }
            });

        log.info(" - categorizing types from class-path scan");

        val domainObjectSpecs = _Lists.<ObjectSpecification>newArrayList();
        val mixinSpecs = _Lists.<ObjectSpecification>newArrayList();

        causewayBeanTypeRegistry.streamIntrospectableTypes()
        .forEach(typeMeta->{

            val spec = primeSpecification(typeMeta);
            if(spec==null) {
                //XXX only ever happens when the class substitutor vetoes
                return;
            }

            knownSpecs.add(spec);

            val sort = typeMeta.getBeanSort();

            if(sort.isManagedBeanAny() || sort.isEntity() || sort.isViewModel() ) {
                domainObjectSpecs.add(spec);
            } else if(sort.isMixin()) {
                mixinSpecs.add(spec);
            }

        });

        //XXX[CAUSEWAY-2382] when parallel introspecting, make sure we have the mixins before their holders
        // (observation by experiment, no real understanding as to why)

        _Util.logBefore(log, cache, knownSpecs);

        log.info(" - introspecting {} type hierarchies", knownSpecs.size());
        introspect(Can.ofCollection(knownSpecs), IntrospectionState.TYPE_INTROSPECTED);

        log.info(" - introspecting {} value types", valueTypeSpecs.size());
        introspect(Can.ofCollection(valueTypeSpecs.values()), IntrospectionState.FULLY_INTROSPECTED);

        log.info(" - introspecting {} mixins", causewayBeanTypeRegistry.getMixinTypes().size());
        introspect(Can.ofCollection(mixinSpecs), IntrospectionState.FULLY_INTROSPECTED);

        log.info(" - introspecting {} managed beans contributing (domain services)",
                causewayBeanTypeRegistry.getManagedBeansContributing().size());

        log.info(" - introspecting {} entities ({})",
                causewayBeanTypeRegistry.getEntityTypes().size(),
                causewayBeanTypeRegistry.determineCurrentPersistenceStack().name());

        log.info(" - introspecting {} view models", causewayBeanTypeRegistry.getViewModelTypes().size());

        serviceRegistry.lookupServiceElseFail(MenuBarsService.class).menuBars();

        introspect(Can.ofCollection(domainObjectSpecs), IntrospectionState.FULLY_INTROSPECTED);

        _Util.logAfter(log, cache, knownSpecs);

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
        waitForValidationToFinish();
        logicalTypeResolver.clear();
        cache.clear();
        validationResult.clear();
        serviceRegistry.clearRegisteredBeans();
        log.info("Metamodel disposed.");
    }

    /**
     * [CAUSEWAY-3066] wait for validation (if any) to finish (max 5s)
     */
    @SneakyThrows
    private void waitForValidationToFinish() {
        int maxRetry = 50;
        while(!validationQueue.isEmpty()
                && maxRetry>0) {
            Thread.sleep(100);
            --maxRetry;
        }
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
        return IntrospectionMode.isFullIntrospect(causewayConfiguration, causewaySystemEnvironment);
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
        return _loadSpecification(type, this::classify, upTo);
    }

    @Override
    public void validateLater(final ObjectSpecification objectSpec) {
        if(!isMetamodelFullyIntrospected()) {
            // don't trigger validation during bootstrapping
            // getValidationResult() is lazily populated later on first request anyway
            return;
        }
        if(!causewayConfiguration.getCore().getMetaModel().getIntrospector().isValidateIncrementally()) {
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
        val shouldRunConcurrent = causewayConfiguration.getCore().getMetaModel().getValidator().isParallelize();
        if(shouldRunConcurrent) {
            cache.forEachConcurrent(onSpec);
        } else {
            cache.forEach(onSpec);
        }

    }

    @Override
    public Optional<LogicalType> lookupLogicalType(final @NonNull String logicalTypeName) {
        val logicalType = logicalTypeResolver.lookup(logicalTypeName);
        if(logicalType.isPresent()) {
            return logicalType;
        }

        //XXX[2533] if the logicalTypeName is not available and instead a fqcn was passed in, that should also be supported

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

    /**
     * Classification ideally happens eagerly during Spring's class path scan,
     * however as a fallback we might need to classify types that escaped eager introspection
     * here.
     */
    private CausewayBeanMetaData classify(final @Nullable Class<?> type) {
        return causewayBeanTypeRegistry
                .lookupIntrospectableType(type)
                .orElseGet(()->
                    valueSemanticsResolver.get().hasValueSemantics(type)
                    ? CausewayBeanMetaData.causewayManaged(BeanSort.VALUE, LogicalType.infer(type))
                    : causewayBeanTypeClassifier.classify(type)
                );
    }

    @Nullable
    private ObjectSpecification primeSpecification(
            final @NonNull CausewayBeanMetaData typeMeta) {
        return _loadSpecification(
                typeMeta.getCorrespondingClass(), type->typeMeta, IntrospectionState.NOT_INTROSPECTED);

    }

    @Nullable
    private ObjectSpecification _loadSpecification(
            final @Nullable Class<?> type,
            final @NonNull Function<Class<?>, CausewayBeanMetaData> beanClassifier,
            final @NonNull IntrospectionState upTo) {

        if(type==null) {
            return null;
        }

        val substitute = classSubstitutorRegistry.getSubstitution(type);
        if (substitute.isNeverIntrospect()) {
            return null; // never inspect
        }

        val substitutedType = substitute.apply(type);

        val spec = cache.computeIfAbsent(substitutedType, _spec->
            logicalTypeResolver
                .register(
                        createSpecification(beanClassifier.apply(substitutedType))));

        spec.introspectUpTo(upTo);

        if(spec.getAliases().isNotEmpty()
            // this bool. expr. is an optimization, not strictly required ... a bit of hack though
            && upTo == IntrospectionState.TYPE_INTROSPECTED) {

            //XXX[3063] hitting this a couple of times
            //(~5 see org.apache.causeway.testdomain.domainmodel.DomainModelTest_usingGoodDomain.aliasesOnDomainServices_shouldBeHonored())
            // per spec (with aliases), even though already registered;
            // room for performance optimizations, but at the time of writing
            // don't want to add a ObjectSpecification flag to keep track of alias registered state;
            // as an alternative purge the aliased facets and introspect aliased attributes from annotations
            // much earlier in the bootstrap process, same as we do with @Named processing

            logicalTypeResolver
                .registerAliases(spec);
        }

        return spec;
    }

    private void guardAgainstMetamodelLockedAfterFullIntrospection(final Class<?> cls) {
        if(isMetamodelFullyIntrospected()
                && causewayConfiguration.getCore().getMetaModel().getIntrospector().isLockAfterFullIntrospection()) {

            val warningMessage = ProgrammingModelConstants.Violation.TYPE_NOT_EAGERLY_DISCOVERED
                    .builder()
                    .addVariable("type", cls.getName())
                    .addVariable("beanSort", causewayBeanTypeClassifier
                            .classify(cls)
                            .getBeanSort()
                            .name())
                    .buildMessage();

            log.warn(warningMessage);
        }
    }

    /**
     * Creates the appropriate type of {@link ObjectSpecification}.
     */
    private ObjectSpecification createSpecification(final CausewayBeanMetaData typeMeta) {

        guardAgainstMetamodelLockedAfterFullIntrospection(typeMeta.getCorrespondingClass());

        // ... and create the specs

        val objectSpec = new ObjectSpecificationDefault(
                        typeMeta,
                        metaModelContext,
                        facetProcessor,
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
        val isConcurrentFromConfig = causewayConfiguration.getCore().getMetaModel().getIntrospector().isParallelize();
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
