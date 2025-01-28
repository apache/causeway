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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Blackhole;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Timing;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData;
import org.apache.causeway.core.config.beans.CausewayBeanMetaData.DiscoveredBy;
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
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutor.Substitution;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationMutable.IntrospectionState;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.causeway.core.metamodel.valuetypes.ValueSemanticsResolverDefault;
import org.apache.causeway.core.security.authorization.manager.ActionSemanticsResolver;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * The implementation provides for a degree of pluggability:
 * <ul>
 * <li>The most important plug-in point is {@link ProgrammingModel} that
 * specifies the set of {@link Facet} that make up programming model. If not
 * specified then defaults to {@link ProgrammingModelDefault} (which should
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
class SpecificationLoaderDefault
implements
    SpecificationLoaderInternal,
    ActionSemanticsResolver {

    private final CausewayConfiguration causewayConfiguration;
    private final CausewaySystemEnvironment causewaySystemEnvironment;
    private final ServiceRegistry serviceRegistry;
    private final CausewayBeanTypeClassifier causewayBeanTypeClassifier;
    private final CausewayBeanTypeRegistry causewayBeanTypeRegistry;
    private final ClassSubstitutorRegistry classSubstitutorRegistry;
    private final Provider<ValueSemanticsResolver> valueSemanticsResolver;
    private final ProgrammingModel programmingModel;
    private PostProcessor postProcessor;

    @Inject
    public List<PreloadableTypes> preloadableTypes = Collections.emptyList();

    @Getter private MetaModelContext metaModelContext; // cannot inject, would cause circular dependency

    private FacetProcessor facetProcessor;

    private final Map<Class<?>, ObjectSpecificationMutable> cache = new ConcurrentHashMap<>();
    private final LogicalTypeResolver logicalTypeResolver = new LogicalTypeResolver();

    /**
     * We only ever mark the meta-model as fully introspected if in {@link #isFullIntrospect() full}
     * introspection mode.
     */
    @Getter @Setter
    private boolean metamodelFullyIntrospected = false;

    private final boolean parallel;

    @Inject
    public SpecificationLoaderDefault(
            final ProgrammingModel programmingModel,
            final CausewayConfiguration causewayConfiguration,
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final CausewayBeanTypeClassifier causewayBeanTypeClassifier,
            final CausewayBeanTypeRegistry causewayBeanTypeRegistry,
            final Provider<ValueSemanticsResolver> valueSemanticsRegistry,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {
        this.programmingModel = programmingModel;
        this.causewayConfiguration = causewayConfiguration;
        this.causewaySystemEnvironment = causewaySystemEnvironment;
        this.serviceRegistry = serviceRegistry;
        this.causewayBeanTypeClassifier = causewayBeanTypeClassifier;
        this.causewayBeanTypeRegistry = causewayBeanTypeRegistry;
        this.valueSemanticsResolver = valueSemanticsRegistry;
        this.classSubstitutorRegistry = classSubstitutorRegistry;
        this.parallel = causewayConfiguration.getCore().getMetaModel().getIntrospector().isParallelize();
    }

    /** JUnit Test Support */
    static SpecificationLoaderDefault instanceForTesting(
            final CausewayConfiguration causewayConfiguration,
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final ServiceRegistry serviceRegistry,
            final ProgrammingModel programmingModel,
            final boolean enablePostprocessors,
            final CausewayBeanTypeClassifier causewayBeanTypeClassifier,
            final CausewayBeanTypeRegistry causewayBeanTypeRegistry,
            final ClassSubstitutorRegistry classSubstitutorRegistry) {

        var instance = new SpecificationLoaderDefault(
                programmingModel, causewayConfiguration, causewaySystemEnvironment,
                serviceRegistry, causewayBeanTypeClassifier, causewayBeanTypeRegistry,
                ()->new ValueSemanticsResolverDefault(List.of(), null),
                classSubstitutorRegistry);

        instance.metaModelContext = serviceRegistry.lookupServiceElseFail(MetaModelContext.class);
        instance.facetProcessor = new FacetProcessor(programmingModel);
        instance.postProcessor = enablePostprocessors
                ? new PostProcessor(programmingModel)
                : new PostProcessor(programmingModel, Can.empty()); // explicitly use empty post processor list

        return instance;
    }

    // -- LIVE CYCLE

    @PostConstruct
    public void init() {
        if (log.isDebugEnabled()) {
            log.debug("initialising {}", this);
        }
        this.metaModelContext = serviceRegistry.lookupServiceElseFail(MetaModelContext.class);
        this.facetProcessor = new FacetProcessor(programmingModel);
    }

    record SpecCollector(
            List<ObjectSpecificationMutable> knownSpecs,
            Map<Class<?>, ObjectSpecificationMutable> valueSpecs,
            List<ObjectSpecificationMutable> domainServiceSpecs,
            List<ObjectSpecificationMutable> mixinSpecs,
            List<ObjectSpecificationMutable> entitySpecs,
            List<ObjectSpecificationMutable> viewmodelSpecs,
            List<ObjectSpecificationMutable> otherSpecs) {

        SpecCollector() {
            this(new ArrayList<>(),
                    new HashMap<>(),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), new ArrayList<>());
        }

        public void collect(final @Nullable ObjectSpecificationMutable spec) {
            if(spec==null) return; // might be vetoed
            knownSpecs.add(spec);
            switch (spec.getBeanSort()) {
                case VALUE -> valueSpecs.put(spec.getCorrespondingClass(), spec);
                case MANAGED_BEAN_CONTRIBUTING -> domainServiceSpecs.add(spec);
                case MIXIN -> mixinSpecs.add(spec);
                case ENTITY -> entitySpecs.add(spec);
                case VIEW_MODEL -> viewmodelSpecs.add(spec);
                case PROGRAMMATIC, UNKNOWN, COLLECTION, ABSTRACT -> otherSpecs.add(spec);
                case VETOED, MANAGED_BEAN_NOT_CONTRIBUTING -> {}
            }
        }
    }

    /**
     * Initializes and wires up, and primes the cache based on any service
     * classes (provided by the {@link CausewayBeanTypeRegistry}).
     */
    @Override
    public void createMetaModel() {

        log.info("About to create the Metamodel ...");
        var stopWatch = _Timing.now();

        // initialize subcomponents, only after @PostConstruct has globally completed
        this.facetProcessor = new FacetProcessor(programmingModel);
        this.postProcessor = new PostProcessor(programmingModel);

        var specs = new SpecCollector();

        // preload otherwise not eagerly discovered classes
        var prealoadCount = preloadableTypes.stream()
            .flatMap(PreloadableTypes::stream)
            .map(this::loadSpecification)
            .filter(_NullSafe::isPresent)
            .count();
        log.info(" - preloaded {} otherwise not eagerly discovered types", prealoadCount);

        var valueTypesFromProviders = valueSemanticsResolver.get().streamClassesWithValueSemantics()
            .map(valueClass->CausewayBeanMetaData.value(LogicalType.infer(valueClass), DiscoveredBy.CAUSEWAY_UPFRONT))
            .toList();
        log.info(" - found {} value types via ValueTypeProviders", valueTypesFromProviders.size());

        Stream
            .concat(
                valueTypesFromProviders.stream(),
                causewayBeanTypeRegistry.streamScannedTypes())
            // prime (up to NOT_INTROSPECTED)
            .map(this::primeSpecification)
            .forEach(specs::collect);

        introspectAndLog("type hierarchies", specs.knownSpecs, IntrospectionState.TYPE_INTROSPECTED);
        introspectAndLog("value types", specs.valueSpecs.values(), IntrospectionState.FULLY_INTROSPECTED);
        introspectAndLog("mixins", specs.mixinSpecs, IntrospectionState.FULLY_INTROSPECTED);
        introspectAndLog("domain services", specs.domainServiceSpecs, IntrospectionState.FULLY_INTROSPECTED);
        introspectAndLog("entities (%s)".formatted(causewayBeanTypeRegistry.persistenceStack().name()),
                specs.entitySpecs(), IntrospectionState.FULLY_INTROSPECTED);
        introspectAndLog("view models", specs.viewmodelSpecs(), IntrospectionState.FULLY_INTROSPECTED);

        serviceRegistry.lookupServiceElseFail(MenuBarsService.class).menuBars();

        if(isFullIntrospect()) {
            var snapshot = snapshotSpecifications();
            log.info(" - introspecting all {} types eagerly (FullIntrospect=true)", snapshot.size());
            introspect(snapshot.filter(x->x.getBeanSort().isMixin()), IntrospectionState.FULLY_INTROSPECTED);
            introspect(snapshot.filter(x->!x.getBeanSort().isMixin()), IntrospectionState.FULLY_INTROSPECTED);
        }

        log.info(" - running remaining validators");
        _Blackhole.consume(getOrAssessValidationResult()); // as a side effect memoizes the validation result

        stopWatch.stop();
        log.info("Metamodel created in {}ms. ({} introspection)", stopWatch.getMillis(), parallel ? "parallel" : "sequential");

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
        int retryCount = 0;
        while(!validationQueue.isEmpty()
                && maxRetry>0) {
            Thread.sleep(100);
            --maxRetry;
            ++retryCount;
        }
        if(retryCount>0) log.info("wait for validation to finish took {}ms", retryCount * 100);
    }

    @PreDestroy
    public void shutdown() {
        log.debug("shutting down {}", this);
        disposeMetaModel();
        facetProcessor = null;
        postProcessor = null;
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

    /**
     * Return the specification for the specified class of object.
     *
     * <p>
     * It is possible for this method to return <tt>null</tt>, for example if
     * any of the configured {@link ClassSubstitutor}s has filtered out the class.
     *
     * @return {@code null} if {@code domainType==null}, or if the type should be ignored.
     */
    @Override
    public ObjectSpecification loadSpecification(
            final @Nullable Class<?> type,
            final @NonNull IntrospectionState upTo) {
        return loadSpecificationNullable(type, this::classify, upTo);
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
    public Can<ObjectSpecificationMutable> snapshotSpecifications() {
        return Can.ofCollection(cache.values());
    }

    @Override
    public void forEach(final Consumer<ObjectSpecification> onSpec) {
        var snapshot = snapshotSpecifications();
        if(parallel) {
            snapshot
                .stream()
                .parallel()
                .forEach(onSpec);
        } else {
            snapshot
                .forEach(onSpec);
        }
    }

    @Override
    public Optional<LogicalType> lookupLogicalType(final @NonNull String logicalTypeName) {
        var logicalType = logicalTypeResolver.lookup(logicalTypeName);
        if(logicalType.isPresent()) return logicalType;

        //XXX[2533] if the logicalTypeName is not available and instead a fqcn was passed in, that should also be supported

        // falling back assuming the logicalTypeName equals the fqn of the corresponding class
        // which might not always be true,

//TODO yet it seems we rely on this kind of fallback from several code paths, so lets not emit any warnings yet ...
//      log.warn("Lookup for ObjectType '{}' failed, but found a matching fully qualified "
//              + "class name to use instead. This warning is an indicator, that {} is not "
//              + "discovered by Spring during bootstrapping of this application.",
//              logicalType.getName(),
//              cls.getName());
        var cls = ClassUtil.forNameElseNull(logicalTypeName);
        return cls!=null
            ? Optional.of(LogicalType.fqcn(cls))
            : Optional.empty();
    }

    // -- VALIDATION STUFF

    private final ValidationFailures validationFailures = new ValidationFailures();

    @Override
    public void addValidationFailure(final ValidationFailure validationFailure) {
//        if(validationResult.isMemoized()) {
//            validationResult.clear(); // invalidate
//            throw _Exceptions.illegalState(
//                    "Validation result was already created and can no longer be modified.");
//        }
        synchronized(validationFailures) {
            validationFailures.add(validationFailure);
        }
    }

    private _Lazy<ValidationFailures> validationResult =
            _Lazy.threadSafe(this::runMetaModelValidators);

    private final AtomicBoolean validationInProgress = new AtomicBoolean(false);
    private final BlockingQueue<ObjectSpecification> validationQueue = new LinkedBlockingQueue<>();

    private ValidationFailures runMetaModelValidators() {
        validationInProgress.set(true);
        _ValidateUtil.runValidators(programmingModel, this);
        validationInProgress.set(false);

        return validationFailures;
    }

    // -- ACTION SEMANTICS RESOLVER

    @Override
    public Optional<SemanticsOf> getActionSemanticsOf(final Identifier identifier) {
        if(!identifier.type().isAction()) {
            return Optional.empty();
        }
        return specForLogicalType(identifier.logicalType())
            .flatMap(objSpec->objSpec.getAction(identifier.memberLogicalName()))
            .map(ObjectAction::getSemantics);
    }

    // -- HELPER

    /**
     * Classification ideally happens eagerly during Spring's class path scan,
     * however as a fallback we might need to classify types that escaped eager introspection
     * here.
     */
    private CausewayBeanMetaData classify(final @Nullable Class<?> type) {

        var discoveredBy = isMetamodelFullyIntrospected()
                ? DiscoveredBy.CAUSEWAY_ONTHEFLY
                : DiscoveredBy.CAUSEWAY_UPFRONT;

        var typeMeta = causewayBeanTypeRegistry
                .lookupScannedType(type)
                .orElseGet(()->
                    valueSemanticsResolver.get().hasValueSemantics(type)
                        ? CausewayBeanMetaData.value(LogicalType.infer(type), discoveredBy)
                        : causewayBeanTypeClassifier.classify(LogicalType.infer(type), discoveredBy)
                );

        if(isMetamodelFullyIntrospected()) {
            var warningMessage = ProgrammingModelConstants.MessageTemplate.TYPE_NOT_EAGERLY_DISCOVERED
                    .builder()
                    .addVariable("type", type.getName())
                    .addVariable("beanSort", typeMeta.beanSort().name())
                    .buildMessage();

            log.warn(warningMessage);
        }

        return typeMeta;
    }

    @Nullable
    private ObjectSpecificationMutable primeSpecification(
            final @NonNull CausewayBeanMetaData typeMeta) {
        return loadSpecificationNullable(
                typeMeta.getCorrespondingClass(), type->typeMeta, IntrospectionState.NOT_INTROSPECTED);

    }

    @Nullable
    private ObjectSpecificationMutable loadSpecificationNullable(
            final @Nullable Class<?> type,
            final @NonNull Function<Class<?>, CausewayBeanMetaData> beanClassifier,
            final @NonNull IntrospectionState upTo) {

        if(type==null) {
            return null;
        }

        var substitute = classSubstitutorRegistry.getSubstitution(type);
        if (substitute.isNeverIntrospect()) return null; // never inspect

        var substitutedType = substitute.apply(type);

        var spec = cache.computeIfAbsent(substitutedType, _spec->
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

    /**
     * Creates the appropriate type of {@link ObjectSpecification}.
     */
    private ObjectSpecificationMutable createSpecification(final CausewayBeanMetaData typeMeta) {
        var objectSpec = new ObjectSpecificationDefault(
                        typeMeta,
                        metaModelContext,
                        facetProcessor,
                        postProcessor,
                        classSubstitutorRegistry);
        return objectSpec;
    }

    private void introspectSequential(
            final Can<ObjectSpecificationMutable> specs,
            final IntrospectionState upTo) {
        for (var spec : specs) {
            spec.introspectUpTo(upTo);
        }
    }

    private void introspectParallel(
            final Can<ObjectSpecificationMutable> specs,
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

    private void introspectAndLog(
            final String info,
            final Iterable<ObjectSpecificationMutable> specs,
            final IntrospectionState upTo) {
        var stopWatch = _Timing.now();
        introspect(Can.ofIterable(specs), upTo);
        stopWatch.stop();
        log.info(" - introspecting {} {} took {}ms", _NullSafe.sizeAutodetect(specs), info, stopWatch.getMillis());
    }

    private void introspect(
            final Can<ObjectSpecificationMutable> specs,
            final IntrospectionState upTo) {
        if(parallel) {
            introspectParallel(specs, upTo);
        } else {
            introspectSequential(specs, upTo);
        }
    }

    private void invalidateCache(final Class<?> cls) {

        var substitute = classSubstitutorRegistry.getSubstitution(cls);
        if(substitute.isNeverIntrospect()) {
            return;
        }

        ObjectSpecification spec =
                loadSpecification(substitute.apply(cls), IntrospectionState.FULLY_INTROSPECTED);

        while(spec != null) {
            var type = spec.getCorrespondingClass();
            cache.remove(type);
            spec = spec.superclass();
        }
    }

}
