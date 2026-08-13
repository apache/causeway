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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
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
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutor;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutor.Substitution;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.impl.IntrospectionStateHandler.IntrospectionRequest;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.causeway.core.metamodel.valuetypes.ValueSemanticsResolverDefault;
import org.apache.causeway.core.security.authorization.manager.ActionSemanticsResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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
    private MixinSpecStreamer mixinSpecStreamer = MixinSpecStreamer.EMPTY;
    //perf... private final Profiler profiler = Profiler.getInstance();

    @Inject
    public List<PreloadableTypes> preloadableTypes = Collections.emptyList();

    private FacetProcessor facetProcessor;

    private final Map<Class<?>, ObjectSpecificationInternal> cache = new ConcurrentHashMap<>();
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
        this.parallel = causewayConfiguration.core().metaModel().introspector().parallelize();
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
        this.facetProcessor = new FacetProcessor(programmingModel);
    }

    record SpecCollector(
            List<ObjectSpecificationInternal> knownSpecs,
            Map<Class<?>, ObjectSpecificationInternal> valueSpecs,
            List<ObjectSpecificationInternal> domainServiceSpecs,
            List<ObjectSpecificationInternal> mixinSpecs,
            List<ObjectSpecificationInternal> entitySpecs,
            List<ObjectSpecificationInternal> viewmodelSpecs,
            List<ObjectSpecificationInternal> otherSpecs) {

        SpecCollector() {
            this(new ArrayList<>(),
                    new HashMap<>(),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), new ArrayList<>());
        }

        public void collect(final @Nullable ObjectSpecificationInternal spec) {
            if(spec==null) return; // might be vetoed
            knownSpecs.add(spec);
            switch (spec.beanSort()) {
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

    @Override
    public boolean contains(@Nullable final Class<?> cls) {
    	return cls!=null
			? cache.containsKey(cls)
    		: false;
    }

    enum Phase {
    	NO_MIXINS_YET,
    	INTROSPECTING_MIXINS,
    	MIXINS_READY,
    }

    Phase phase = Phase.NO_MIXINS_YET;

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
        var preloadCount = preloadableTypes.stream()
            .flatMap(PreloadableTypes::stream)
            .map(this::loadSpecificationTypeOnly)
            .filter(_NullSafe::isPresent)
            .count();
        log.info(" - preloaded {} otherwise not eagerly discovered types", preloadCount);

        var valueTypesFromProviders = valueSemanticsResolver.get().streamClassesWithValueSemantics()
            .map(valueClass->CausewayBeanMetaData.value(LogicalType.infer(valueClass), DiscoveredBy.CAUSEWAY_UPFRONT))
            .toList();
        log.info(" - found {} value types via ValueTypeProviders", valueTypesFromProviders.size());

        Stream
            .concat(
                valueTypesFromProviders.stream(),
                causewayBeanTypeRegistry.streamScannedTypes())
            // prime (up to NOT_INTROSPECTED)
            .map(this::register)
            .forEach(specs::collect);

        introspectAndLog("type hierarchies", specs.knownSpecs, IntrospectionRequest.TYPE_ONLY);
        introspectAndLog("value types", specs.valueSpecs.values(), IntrospectionRequest.FULL);
        //this.mixinSpecStreamer = MixinSpecStreamer.EMPTY;
        //this.mixinSpecStreamer = new MixinSpecStreamerOnTheFly(this, causewayBeanTypeRegistry);
        this.phase = Phase.INTROSPECTING_MIXINS;
        introspectAndLog("mixins", specs.mixinSpecs, IntrospectionRequest.FULL);
        // lock down mixins, also assuming none of the previously fully introspected types need any mixins
        this.mixinSpecStreamer = new MixinSpecStreamerEager(this, causewayBeanTypeRegistry);
        this.phase = Phase.MIXINS_READY;

        //TODO good for abstract types, but cannot be decided for interfaces,
        // as those could in theory be shared with mixins and domain-types
        var mixinTypeHierarchyMembers = specs.mixinSpecs.stream()
	    	.flatMap(ObjectSpecificationInternal::streamTypeHierarchy)
	    	.filter(spec->!spec.isMixin())
	    	.collect(Collectors.toSet());

        //TODO expected no domain objects fully introspected yet. however, some facet/post processors, that
        // run on mixin-spec have the side effect of fully introspecting other types e.g. by asking for the
        // members's element type
        long brokenSpecCount = cache.values().stream()
    		.filter(spec->!spec.getCorrespondingClass().getName().startsWith("java."))
        	.filter(spec->!spec.isMixin())
        	.filter(spec->!spec.isValue())
        	.filter(ObjectSpecificationInternal::isFullyIntrospected)
        	.filter(spec->!mixinTypeHierarchyMembers.contains(spec))
        	.filter(spec->{
        		var msg = "type (non-mixin, non-value) found fully introspected after mixin introspection %s - reload triggered"
        				.formatted(spec.getCorrespondingClass());
    			log.warn(msg);
    			return true;
    			//reloadSpecification(spec.getCorrespondingClass());
        	}).count();
        if(brokenSpecCount>0)
        	//TODO for now we fail hard, because Spec reloading is not consistently supported yet
        	// that is currently we evict the spec from the cache but the Spec reference must stay constant
			throw _Exceptions.illegalState("brokenSpecCount=%d", brokenSpecCount);


        introspectAndLog("domain services", specs.domainServiceSpecs, IntrospectionRequest.FULL); //TODO no mixins required either
        introspectAndLog("entities (%s)".formatted(causewayBeanTypeRegistry.persistenceStack().name()),
                specs.entitySpecs(), IntrospectionRequest.FULL);
        introspectAndLog("view models", specs.viewmodelSpecs(), IntrospectionRequest.FULL);

        serviceRegistry.lookupServiceElseFail(MenuBarsService.class).menuBars();

        var snapshot = snapshotSpecifications();
        snapshot.stream()
	        .filter(ObjectSpecificationInternal::isMixin)
	        .filter(spec->!spec.isFullyIntrospected())
	        .forEach(spec->{
	        	log.warn("Mixin was missing during first pass {}."
	        			+ "It will not be added to the metamodel. For inclusion, "
	        			+ "make sure it is discovered by Spring.", spec);
	        });

        //if(isFullIntrospect())  //TODO enforced, otherwise types discovered during introspection never get fully introspected (bug)
        {
            log.info(" - introspecting types not initially discovered by Spring {}", snapshot.size());
            introspect(snapshot.filter(spec->!spec.isMixin()), IntrospectionRequest.FULL);
        }

      //debug
        //System.err.println(Mm2YamlUtils.toYaml(snapshotSpecifications()));

        log.info(" - running remaining validators");
        getOrAssessValidationResult(); // as a side effect memoizes the validation result

        log.info(" - clearing layout caches");
        clearLayoutCaches();

        log.info(" - collect qualified value semantics");
        new QualifiedValueSemanticsCollector(causewaySystemEnvironment).collect();

        stopWatch.stop();
        log.info("Metamodel created in {}ms. ({} introspection)", stopWatch.getMillis(), parallel ? "parallel" : "sequential");

        if(isFullIntrospect()) {
            setMetamodelFullyIntrospected(true);
        }

        //perf .. log.info("\n{}", profiler);
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
        clearLayoutCaches();
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
        loadSpecification(domainType, IntrospectionRequest.FULL);
    }

    @Override
    public boolean loadSpecifications(final Class<?>... domainTypes) {
        // ensure that all types are loadable
        if (Arrays.stream(domainTypes)
                .map(classSubstitutorRegistry::getSubstitution)
                .anyMatch(Substitution::isNeverIntrospect))
            return false;
        Arrays.stream(domainTypes).forEach(this::loadSpecification);
        return true;
    }

    /**
     * Return the specification for the specified class of object.
     * <p>
     * It is possible for this method to return <tt>null</tt>, for example if
     * any of the configured {@link ClassSubstitutor}s has filtered out the class.
     *
     * @return {@code null} if {@code domainType==null}, or if the type should be ignored.
     */
    @Override
    public ObjectSpecification loadSpecification(
            final @Nullable Class<?> type,
            final @NonNull IntrospectionRequest request) {
        return loadSpecificationNullable(type, this::classify, request);
    }

    @Override
    public void validateLater(
            final ObjectSpecification objectSpec) {
        if(!isMetamodelFullyIntrospected())
            // don't trigger validation during bootstrapping
            // getValidationResult() is lazily populated later on first request anyway
            return;
        if(!causewayConfiguration.core().metaModel().introspector().validateIncrementally())
            // re-validation after the initial one can be turned off by means of above config option
            return;

        if(log.isInfoEnabled()) {
            log.info("re-validation triggered for {}", objectSpec.getFullIdentifier());
        }

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
        if(validationFailures.hasFailures())
            throw _Exceptions.illegalState(String.join("\n", validationFailures.getMessages("[%d] %s")));

    }

    // -- LOOKUP

    @Override
    public Can<ObjectSpecificationInternal> snapshotSpecifications() {
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

    private final _Lazy<ValidationFailures> validationResult =
            _Lazy.threadSafe(this::runMetaModelValidators);

    private final AtomicBoolean validationInProgress = new AtomicBoolean(false);
    private final BlockingQueue<ObjectSpecification> validationQueue = new LinkedBlockingQueue<>();
	//private Can<ObjectSpecification> mixinSpecs = Can.empty();

    private ValidationFailures runMetaModelValidators() {
        validationInProgress.set(true);
        _ValidateUtil.runValidators(programmingModel, this);
        validationInProgress.set(false);

        return validationFailures;
    }

    // -- ACTION SEMANTICS RESOLVER

    @Override
    public Optional<SemanticsOf> getActionSemanticsOf(final Identifier identifier) {
        if(!identifier.type().isAction())
            return Optional.empty();
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
    private ObjectSpecificationInternal register(
            final @NonNull CausewayBeanMetaData typeMeta) {
        return loadSpecificationNullable(
                typeMeta.getCorrespondingClass(), type->typeMeta, IntrospectionRequest.REGISTER);

    }

    @Nullable
    private ObjectSpecificationInternal loadSpecificationNullable(
            final @Nullable Class<?> type,
            final @NonNull Function<Class<?>, CausewayBeanMetaData> beanClassifier,
            final @NonNull IntrospectionRequest request) {

        if(type==null)
        	return null;

        var substitute = classSubstitutorRegistry.getSubstitution(type);
        if (substitute.isNeverIntrospect())
        	return null; // never inspect

        var substitutedType = substitute.apply(type);

        var spec = cache.computeIfAbsent(substitutedType, _spec->
            logicalTypeResolver
                .register(
                        createSpecification(beanClassifier.apply(substitutedType))));

        if(phase == Phase.INTROSPECTING_MIXINS
        		&& request==IntrospectionRequest.FULL
        		&& !spec.isMixin()) {
        	// don't allow the side-effect of fully introspecting other types during mixin introspection
        	spec.introspect(IntrospectionRequest.TYPE_ONLY);
        } else {
        	spec.introspect(request);
        }

        if(spec.aliases().isNotEmpty()
            // this bool. expr. is an optimization, not strictly required ... a bit of hack though
            && request == IntrospectionRequest.TYPE_ONLY) {

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
    private ObjectSpecificationInternal createSpecification(
    		final CausewayBeanMetaData typeMeta) {
        var objectSpec = new ObjectSpecificationDefault(
                typeMeta,
                facetProcessor,
                postProcessor,
                classSubstitutorRegistry,
                ()->mixinSpecStreamer);
        return objectSpec;
    }

    private void introspectSequential(
            final Can<ObjectSpecificationInternal> specs,
            final IntrospectionRequest request) {
        for (var spec : specs) {
            spec.introspect(request);
        }
    }

    private void introspectParallel(
            final Can<ObjectSpecificationInternal> specs,
            final IntrospectionRequest request) {
        specs.parallelStream()
        .forEach(spec -> {
            try {
                spec.introspect(request);
            } catch (Throwable ex) {
                log.error("failure", ex);
                throw ex;
            }
        });
    }

    private void introspectAndLog(
            final String info,
            final Iterable<ObjectSpecificationInternal> specs,
            final IntrospectionRequest request) {
        var stopWatch = _Timing.now();
        introspect(Can.ofIterable(specs), request);
        stopWatch.stop();
        log.info(" - introspecting {} {} took {}ms", _NullSafe.sizeAutodetect(specs), info, stopWatch.getMillis());
    }

    private void introspect(
            final Can<ObjectSpecificationInternal> specs,
            final IntrospectionRequest request) {
        if(parallel) {
            introspectParallel(specs, request);
        } else {
            introspectSequential(specs, request);
        }
    }

    @Deprecated
    private void invalidateCache(final Class<?> cls) {
        var substitute = classSubstitutorRegistry.getSubstitution(cls);
        if(substitute.isNeverIntrospect())
        	return;

        var objSpec =
                loadSpecification(substitute.apply(cls), IntrospectionRequest.FULL);

        while(objSpec != null) {
            var type = objSpec.getCorrespondingClass();
            cache.remove(type);
            objSpec = objSpec.superSpec().orElse(null);
        }
    }

    private void clearLayoutCaches() {
        cache.values().parallelStream().forEach(spec->{
            spec.lookupFacet(GridFacet.class)
                .ifPresent(GridFacet::clearCache);
        });
        serviceRegistry.lookupService(GridService.class)
            .ifPresent(GridService::clearCache);
    }

}
