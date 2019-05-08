package org.apache.isis.core.metamodel.specloader;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.enterprise.inject.Vetoed;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.core.metamodel.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.FreeStandingList;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.isis.core.metamodel.specloader.specimpl.FacetedMethodsBuilderContext;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.metamodel.specloader.specimpl.standalonelist.ObjectSpecificationOnStandaloneList;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelDeficiencies;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.core.runtime.threadpool.ThreadPoolExecutionMode;
import org.apache.isis.core.runtime.threadpool.ThreadPoolSupport;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;
import org.apache.isis.schema.utils.CommonDtoUtils;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
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
 */
@Vetoed // has a producer
@Slf4j
public class SpecificationLoaderDefault implements SpecificationLoader {

	private final ClassSubstitutor classSubstitutor = new ClassSubstitutor();

	private final ProgrammingModel programmingModel;
	private final FacetProcessor facetProcessor;


	private final MetaModelValidator metaModelValidator;
	private final SpecificationCacheDefault cache = new SpecificationCacheDefault();
	private final PostProcessor postProcessor;


	public SpecificationLoaderDefault(
			final ProgrammingModel programmingModel,
			final MetaModelValidator metaModelValidator) {

		this.programmingModel = programmingModel;
		this.metaModelValidator = metaModelValidator;

		this.facetProcessor = new FacetProcessor(programmingModel);
		this.postProcessor = new PostProcessor(programmingModel);
	}

	// -- LIVE CYCLE

	/**
	 * Initializes and wires up, and primes the cache based on any service
	 * classes (provided by the {@link ServicesInjector}).
	 */
	@Override
	public void init() {

		if (log.isDebugEnabled()) {
			log.debug("initialising {}", this);
		}

		// wire subcomponents into each other
		//facetProcessor.setServicesInjector(servicesInjector);

		// initialize subcomponents
		this.programmingModel.init();
		facetProcessor.init();

		postProcessor.init();
		metaModelValidator.init();


		// need to completely load services and mixins (synchronously)
		log.info("Loading all specs (up to state of {})", IntrospectionState.NOT_INTROSPECTED);

		val typeRegistry = IsisBeanTypeRegistry.current(); 

		final List<ObjectSpecification> specificationsFromRegistry = _Lists.newArrayList();

		// we use allServiceClasses() - obtained from servicesInjector - rather than reading from the
		// AppManifest.Registry.instance().getDomainServiceTypes(), because the former also has the fallback
		// services set up in IsisSessionFactoryBuilder beforehand.
		final List<ObjectSpecification> domainServiceSpecs =
				loadSpecificationsForBeans(
						streamBeans(), NatureOfService.DOMAIN,
						specificationsFromRegistry, IntrospectionState.NOT_INTROSPECTED
						);
		final List<ObjectSpecification> mixinSpecs =
				loadSpecificationsFor(
						typeRegistry.getMixinTypes().stream(), null,
						specificationsFromRegistry, IntrospectionState.NOT_INTROSPECTED
						);
		loadSpecificationsFor(
				CommonDtoUtils.VALUE_TYPES.stream(), null,
				specificationsFromRegistry, IntrospectionState.NOT_INTROSPECTED
				);
		loadSpecificationsFor(
				typeRegistry.getDomainObjectTypes().stream(), null,
				specificationsFromRegistry, IntrospectionState.NOT_INTROSPECTED
				);
		loadSpecificationsFor(
				typeRegistry.getViewModelTypes().stream(), null,
				specificationsFromRegistry, IntrospectionState.NOT_INTROSPECTED
				);
		loadSpecificationsFor(
				typeRegistry.getXmlElementTypes().stream(), null,
				specificationsFromRegistry, IntrospectionState.NOT_INTROSPECTED
				);

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

		//FIXME [2033] remove debug code ...
		//{
		//        	streamServiceClasses()
		//        	.forEach(service->probe.println("using service %s", service));
		//        	
		//        	
		//        	val metaModelService = _CDI.getSingleton(MetaModelService.class);
		//        	val jaxbService = _CDI.getSingleton(JaxbService.class);
		//        	
		//        	val metamodelDto =
		//        			metaModelService.exportMetaModel(
		//        					new MetaModelService.Config()
		//        							.withIgnoreNoop()
		//        							.withIgnoreAbstractClasses()
		//        							.withIgnoreBuiltInValueTypes()
		//        							.withIgnoreInterfaces()
		//        							.withPackagePrefix("domainapp")
		//        			);
		//			
		//			final String xml = jaxbService.toXml(metamodelDto);
		//			//probe.println(xml);
		//        }
	}

	@Override
	public void shutdown() {
		cache.clear();
		
		log.info("shutting down {}", this);
	}

	// -- VALIDATION

	private ValidationFailures validationFailures;

	private MetaModelDeficiencies validateThenGetDeficienciesIfAny() {
		final IntrospectionMode mode = CONFIG_PROPERTY_MODE.from(getConfiguration());
		if(!mode.isFullIntrospect(_Context.getEnvironment().getDeploymentType())) {
			log.info("Meta model validation skipped (full introspection of metamodel not configured)");
			return null;
		}

		ValidationFailures validationFailures = validate();
		return validationFailures.getDeficienciesIfAny();
	}

	@Override
	public ValidationFailures validate() {
		if(validationFailures == null) {
			validationFailures = new ValidationFailures();
			metaModelValidator.validate(validationFailures);
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
		
		val spec = internalLoadSpecification(type, null, upTo);
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

	private ObjectSpecification internalLoadSpecification(
			final Class<?> type,
			final NatureOfService natureFallback,
			final IntrospectionState upTo) {

		final Class<?> substitutedType = classSubstitutor.getClass(type);
		if (substitutedType == null) {
			return null;
		}

		final String typeName = substitutedType.getName();
		ObjectSpecification spec = cache.get(typeName);
		if (spec != null) {
			return spec;
		}

		//TODO[2033] don't block on long running code ... 'specSpi.introspectUpTo(upTo);'
		synchronized (cache) {
			
			spec = cache.get(typeName);
			if (spec != null) {
				return spec;
			}

			final ObjectSpecification specification = createSpecification(substitutedType, natureFallback);

			// put into the cache prior to introspecting, to prevent
			// infinite loops
			cache.cache(typeName, specification);

			final ObjectSpecificationAbstract specSpi = (ObjectSpecificationAbstract) specification;
			specSpi.introspectUpTo(upTo);

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
	public ObjectSpecification lookupBySpecId(ObjectSpecId objectSpecId) {
		if(!cache.isInitialized()) {
			throw new IllegalStateException("Internal cache not yet initialized");
		}
		final ObjectSpecification objectSpecification = cache.getByObjectType(objectSpecId);
		if(objectSpecification == null) {
			// fallback
			return loadSpecification(objectSpecId, IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
		}
		return objectSpecification;
	}


	
	// -- HELPER
	
	private IsisConfiguration getConfiguration() {
		return _Config.getConfiguration();
	}	

	private Collection<ObjectSpecification> allCachedSpecifications() {
		return cache.allSpecifications();
	}

	private Stream<BeanAdapter> streamBeans() {
		final ServiceRegistry registry = MetaModelContext.current().getServiceRegistry();
		return registry.streamRegisteredBeans();
	}
	
	/**
	 * Creates the appropriate type of {@link ObjectSpecification}.
	 */
	private ObjectSpecification createSpecification(
			final Class<?> cls,
			final NatureOfService fallback) {

		// ... and create the specs
		final ObjectSpecificationAbstract objectSpec;
		if (FreeStandingList.class.isAssignableFrom(cls)) {

			objectSpec = new ObjectSpecificationOnStandaloneList(facetProcessor, postProcessor);

		} else {

			final FacetedMethodsBuilderContext facetedMethodsBuilderContext =
					new FacetedMethodsBuilderContext(
							this, facetProcessor);

			final NatureOfService natureOfServiceIfAny = natureOfServiceFrom(cls, fallback);

			objectSpec = new ObjectSpecificationDefault(cls,
					facetedMethodsBuilderContext,
					facetProcessor, natureOfServiceIfAny, postProcessor);
		}

		return objectSpec;
	}

	private NatureOfService natureOfServiceFrom(
			final Class<?> type,
			final NatureOfService fallback) {
		final DomainService domainServiceIfAny = type.getAnnotation(DomainService.class);
		return domainServiceIfAny != null ? domainServiceIfAny.nature() : fallback;
	}
	
	private final static _Probe probe = _Probe.unlimited().label("SpecificationLoader");

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
		final List<Callable<Object>> callables = _Lists.newArrayList();
		for (final ObjectSpecification specification : specs) {
			Callable<Object> callable = new Callable<Object>() {
				@Override
				public Object call() {

					final ObjectSpecificationAbstract specSpi = (ObjectSpecificationAbstract) specification;
					specSpi.introspectUpTo(upTo);

					return null;
				}
				public String toString() {
					return String.format(
							"%s: #introspectUpTo( %s )",
							specification.getFullIdentifier(), upTo);
				}
			};
			callables.add(callable);
		}

		invokeAndWait(callables);
	}

	private void invokeAndWait(final List<Callable<Object>> callables) {
		final ThreadPoolSupport threadPoolSupport = ThreadPoolSupport.getInstance();
		final boolean parallelize = CONFIG_PROPERTY_PARALLELIZE.from(getConfiguration());

		final ThreadPoolExecutionMode executionModeFromConfig = parallelize
				? ThreadPoolExecutionMode.PARALLEL
						: ThreadPoolExecutionMode.SEQUENTIAL;

		final List<Future<Object>> futures = 
				threadPoolSupport.invokeAll(executionModeFromConfig, callables);
		threadPoolSupport.joinGatherFailures(futures);
	}

	private List<ObjectSpecification> loadSpecificationsFor(
			final Stream<Class<?>> domainTypes,
			final NatureOfService natureOfServiceFallback,
			final List<ObjectSpecification> appendTo,
			final IntrospectionState upTo) {

		return domainTypes
				.map(domainType->internalLoadSpecification(domainType, natureOfServiceFallback, upTo))
				.filter(_NullSafe::isPresent)
				.peek(appendTo::add)
				.collect(Collectors.toList());
	}

	private List<ObjectSpecification> loadSpecificationsForBeans (
			final Stream<BeanAdapter> beans,
			final NatureOfService natureOfServiceFallback,
			final List<ObjectSpecification> appendTo,
			final IntrospectionState upTo) {

		return beans
				.filter(BeanAdapter::isDomainService)
				.map(BeanAdapter::getBeanClass)    
				.map(domainType->internalLoadSpecification(domainType, natureOfServiceFallback, upTo))
				.filter(_NullSafe::isPresent)
				.peek(appendTo::add)
				.collect(Collectors.toList());
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
