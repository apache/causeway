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
package org.apache.causeway.core.metamodel._testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.grid.GridLoaderService;
import org.apache.causeway.applib.services.grid.GridMarshallerService;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.layout.LayoutService;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.title.TitleService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.services.xactn.TransactionState;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.collections._Streams;
import org.apache.causeway.commons.internal.ioc._SingletonBeanProvider;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.beans.CausewayBeanFactoryPostProcessorForSpring;
import org.apache.causeway.core.config.beans.CausewayBeanTypeClassifier;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistryDefault;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.core.metamodel.commons.ClassUtil;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconService;
import org.apache.causeway.core.metamodel.facets.object.value.annotcfg.ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManagerDefault;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.causeway.core.metamodel.progmodels.dflt.ProgrammingModelFacetsJava11;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorForCollections;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;
import org.apache.causeway.core.metamodel.services.grid.GridLoaderServiceDefault;
import org.apache.causeway.core.metamodel.services.grid.GridServiceDefault;
import org.apache.causeway.core.metamodel.services.grid.bootstrap.GridMarshallerServiceBootstrap;
import org.apache.causeway.core.metamodel.services.grid.bootstrap.GridSystemServiceBootstrap;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResourceLoaderDefault;
import org.apache.causeway.core.metamodel.services.layout.LayoutServiceDefault;
import org.apache.causeway.core.metamodel.services.message.MessageServiceNoop;
import org.apache.causeway.core.metamodel.services.title.TitleServiceDefault;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoaderDefault;
import org.apache.causeway.core.metamodel.valuesemantics.BigDecimalValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.TreePathValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.URLValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.UUIDValueSemantics;
import org.apache.causeway.core.metamodel.valuetypes.ValueSemanticsResolverDefault;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.core.security.authorization.manager.AuthorizationManager;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

@Builder @Getter
public final class MetaModelContext_forTesting
extends MetaModelContext {

    public static MetaModelContext_forTestingBuilder builder() {
        return new MetaModelContext_forTestingBuilder() {
            @Override
            public MetaModelContext_forTesting build() {
                var mmc = super.build();
                MetaModelContext.setOrReplace(mmc);
                return mmc;
            }
        };
    }

    public static MetaModelContext buildDefault() {
        return MetaModelContext_forTesting.builder()
        .build();
    }

    private ServiceInjector serviceInjector;
    private ServiceRegistry serviceRegistry;

    @Builder.Default
    private MetamodelEventService metamodelEventService =
        MetamodelEventService
        .builder()
        .publisher(event->{
            System.out.printf("MetaModelContext_forTesting (logs event to console): %s%n", event);
        })
        .build();

    @Builder.Default
    private CausewaySystemEnvironment systemEnvironment = newCausewaySystemEnvironment();

    @Builder.Default
    private CausewayConfiguration configuration = newCausewayConfiguration();

    @Builder.Default
    private ClassSubstitutorRegistry classSubstitutorRegistry =
        new ClassSubstitutorRegistry(List.of(
                //new ClassSubstitutorForDomainObjects(),
                new ClassSubstitutorForCollections(),
                new ClassSubstitutorDefault()
                ));

    private ObjectManager objectManager;

    private WrapperFactory wrapperFactory;

    private SpecificationLoader specificationLoader;

    @Builder.Default
    Can<Function<MetaModelContext, MetaModelRefiner>> refiners = Can.empty();

    @Builder.Default
    private BiFunction<MetaModelContext, Can<MetaModelRefiner>, ProgrammingModel> programmingModelFactory =
        (mmc, refiners)->new ProgrammingModelFacetsJava11(mmc, refiners);

    private InteractionService interactionService;

    private TranslationService translationService;

    private InteractionContext authentication;

    private AuthorizationManager authorizationManager;

    private AuthenticationManager authenticationManager;

    @Builder.Default
    private TitleService titleService = new TitleServiceDefault(null, null);

    private ObjectIconService objectIconService;

    private RepositoryService repositoryService;

    private FactoryService factoryService;

    private MemberExecutorService memberExecutor;

    private TransactionService transactionService;

    private TransactionState transactionState;

    private CausewayBeanTypeClassifier causewayBeanTypeClassifier;

    private CausewayBeanTypeRegistry causewayBeanTypeRegistry;

    @Builder.Default
    private PlaceholderRenderService placeholderRenderService = PlaceholderRenderService.fallback();

    //private Map<String, ManagedObject> serviceAdaptersById;

    @Singular
    private List<Object> singletons;

    @Singular
    private List<ValueSemanticsProvider<?>> valueSemantics;

    @Singular
    private List<_SingletonBeanProvider> singletonProviders;

    // -- SERVICE SUPPORT

    @Override
    public Stream<ManagedObject> streamServiceAdapters() {
        return objectAdaptersForBeansOfKnownSort.get().values().stream();
    }

    @Override
    public ManagedObject lookupServiceAdapterById(final String serviceId) {
        return objectAdaptersForBeansOfKnownSort.get().get(serviceId);
    }

    // -- LOOKUP

    private Stream<Object> streamSingletons() {

        var fields = Stream.of(
                getConfiguration(),
                getObjectManager(),
                getWrapperFactory(),
                getCausewayBeanTypeClassifier(),
                getCausewayBeanTypeRegistry(),
                systemEnvironment,
                classSubstitutorRegistry,
                serviceInjector,
                serviceRegistry,
                metamodelEventService,
                messageService,
//                specificationLoader,
                interactionService,
                getTranslationService(),
                authentication,
                authorizationManager,
                authenticationManager,
                titleService,
                repositoryService,
                transactionService,
                transactionState,
                getMemberExecutor(),
                getValueSemanticsResolver(),
                new BigDecimalValueSemantics(),
                new URLValueSemantics(),
                new UUIDValueSemantics(),
                new TreePathValueSemantics(),
                this);

        return Stream.concat(
                fields,
                getSingletons().stream())
                .filter(_NullSafe::isPresent);
    }

    Stream<_SingletonBeanProvider> streamBeanAdapters() {
        return _Streams.concat(
                streamSingletons().map(_SingletonBeanProvider::forTesting),
                singletonProviders.stream(),
                discoveredServices.stream(),
                Stream.of(
                    // support for lazy bean providers,
                    _SingletonBeanProvider.forTestingLazy(GridLoaderService.class, this::getGridLoaderService),
                    _SingletonBeanProvider.forTestingLazy(GridService.class, this::getGridService),
                    _SingletonBeanProvider.forTestingLazy(JaxbService.class, this::getJaxbService),
                    _SingletonBeanProvider.forTestingLazy(MenuBarsService.class, this::getMenuBarsService),
                    _SingletonBeanProvider.forTestingLazy(LayoutService.class, this::getLayoutService),
                    _SingletonBeanProvider.forTestingLazy(SpecificationLoader.class, this::getSpecificationLoader)
                )
                );
    }

    private static CausewaySystemEnvironment newCausewaySystemEnvironment() {
        var env = new CausewaySystemEnvironment();
        env.setUnitTesting(true);
        return env;
    }

    private static CausewayConfiguration newCausewayConfiguration() {
        var properties = _Maps.<String, String>newHashMap();
        var config = CausewayConfiguration.builder().environment(new AbstractEnvironment() {
            @Override
            public String getProperty(final String key) {
                return properties.get(key);
            }
        }).build();
        return config;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        if(serviceRegistry==null) {
            serviceRegistry = new ServiceRegistry_forTesting(this);
        }
        return serviceRegistry;
    }

    @Override
    public ServiceInjector getServiceInjector() {
        if(serviceInjector==null) {
            serviceInjector = new ServiceInjector_forTesting(this);
        }
        return serviceInjector;
    }

    @Override
    public FactoryService getFactoryService() {
        if(factoryService==null) {
            factoryService = new FactoryService_forTesting(this);
        }
        return factoryService;
    }

    @Override
    public TranslationService getTranslationService() {
        if(translationService==null) {
            translationService = new TranslationService_forTesting();
        }
        return translationService;
    }

    private ValueSemanticsResolver valueSemanticsResolver;
    private ValueSemanticsResolver getValueSemanticsResolver(){
        if(valueSemanticsResolver==null) {
            valueSemanticsResolver = new ValueSemanticsResolverDefault(valueSemantics,
                    getTranslationService(), this::getSpecificationLoader);
        }
        return valueSemanticsResolver;
    }

    private final CausewayBeanFactoryPostProcessorForSpring causewayBeanFactoryPostProcessorForSpring =
            new CausewayBeanFactoryPostProcessorForSpring();

    public CausewayBeanTypeClassifier getCausewayBeanTypeClassifier() {
        if(causewayBeanTypeClassifier==null) {
            causewayBeanTypeClassifier = causewayBeanFactoryPostProcessorForSpring.getCausewayBeanTypeClassifier();
        }
        return causewayBeanTypeClassifier;
    }

    public CausewayBeanTypeRegistry getCausewayBeanTypeRegistry() {
        if(causewayBeanTypeRegistry==null) {
            causewayBeanTypeRegistry = new CausewayBeanTypeRegistryDefault(Can.empty());
        }
        return causewayBeanTypeRegistry;
    }

    private final _Lazy<ProgrammingModel> programmingModelRef =
            _Lazy.threadSafe(()->initProgrammingModel());
    public ProgrammingModel getProgrammingModel() {
        return programmingModelRef.get();
    }
    private final ProgrammingModel initProgrammingModel() {
        var metamodelRefiners = refiners.map(factory->factory.apply(this));
        var programmingModel = programmingModelFactory.apply(this, metamodelRefiners);

        ((ProgrammingModelAbstract)programmingModel).init(new ProgrammingModelInitFilterDefault());
        return programmingModel;
    }

    @Override
    public SpecificationLoader getSpecificationLoader() {
        if(specificationLoader==null) {

            var configuration = requireNonNull(getConfiguration());
            var environment = requireNonNull(getSystemEnvironment());
            var serviceRegistry = requireNonNull(getServiceRegistry());
            @SuppressWarnings("unused")
            var serviceInjector = requireNonNull(getServiceInjector());
            var programmingModel = requireNonNull(getProgrammingModel());
            var causewayBeanTypeClassifier = requireNonNull(getCausewayBeanTypeClassifier());
            var causewayBeanTypeRegistry = requireNonNull(getCausewayBeanTypeRegistry());
            var classSubstitutorRegistry = requireNonNull(getClassSubstitutorRegistry());

            specificationLoader = SpecificationLoaderDefault.getInstance(
                    configuration,
                    environment,
                    serviceRegistry,
                    programmingModel,
                    causewayBeanTypeClassifier,
                    causewayBeanTypeRegistry,
                    classSubstitutorRegistry);

        }
        return specificationLoader;
    }

    @Builder.Default
    private final MessageService messageService = new MessageServiceNoop();

    @Override
    public ManagedObject getHomePageAdapter() {
        // not supported
        return null;
    }

    @Override
    public ObjectManager getObjectManager() {
        if(objectManager==null) {
            objectManager = ObjectManagerDefault.forTesting(this);
        }
        return objectManager;
    }

    @Override
    public WrapperFactory getWrapperFactory() {
        if(wrapperFactory==null) {
            wrapperFactory = new WrapperFactory_forTesting();
        }
        return wrapperFactory;
    }

    @Override
    public WebAppContextPath getWebAppContextPath() {
        return new WebAppContextPath();
    }

    public void runWithConfigProperties(final Consumer<Map<String, String>> setup, final Runnable runnable) {
        var properties = _Maps.<String, String>newHashMap();
        setup.accept(properties);

        var currentConfigBackup = this.configuration;
        try {

            this.configuration = CausewayConfiguration.builder().environment(new AbstractEnvironment() {
                @Override
                public String getProperty(final String key) {
                    return properties.get(key);
                }
            }).build();

            runnable.run();
        } finally {
            this.configuration = currentConfigBackup;
        }

    }

    // -- LAYOUT TESTING SUPPORT

    @Getter(lazy = true)
    private final JaxbService jaxbService = new JaxbService.Simple();

    @Getter(lazy = true)
    private final MenuBarsService menuBarsService = createMenuBarsService();
    private final MenuBarsService createMenuBarsService() {
        return getSingletonProviders().stream()
                .filter(_SingletonBeanProvider.satisfying(MenuBarsService.class))
                .findFirst()
                .map(provider->(MenuBarsService)provider.getInstanceElseFail())
                .orElseGet(MenuBarsService::forTesting);
    }

    @Getter(lazy = true)
    private final GridMarshallerService gridMarshallerService = createGridMarshallerService();
    //XXX lombok issue: won't compile if inlined
    private final GridMarshallerService createGridMarshallerService() {
        return new GridMarshallerServiceBootstrap(getJaxbService());
    }

    @Getter(lazy = true)
    private final GridLoaderService gridLoaderService = createGridLoaderService();
    //XXX lombok issue: won't compile if inlined
    private final GridLoaderService createGridLoaderService() {
        return new GridLoaderServiceDefault(getMessageService(), Can.of(new LayoutResourceLoaderDefault()), /*support reloading*/true);
    }

    @Getter(lazy = true)
    private final GridService gridService = createGridService();
    //XXX lombok issue: won't compile if inlined
    private final GridService createGridService() {
        return new GridServiceDefault(
            getGridLoaderService(),
            getGridMarshallerService(),
            List.of(
                    new GridSystemServiceBootstrap(
                            this,
                            getTranslationService(),
                            getJaxbService(),
                            getMessageService(),
                            getSystemEnvironment(),
                            List.of())
                            .setMarshaller(getGridMarshallerService())
                    )); // support reloading
    }

    @Getter(lazy = true)
    private final LayoutService layoutService = createLayoutService();
    //XXX lombok issue: won't compile if inlined
    private final LayoutService createLayoutService() {
        return new LayoutServiceDefault(
                getSpecificationLoader(),
                getGridService(),
                getMenuBarsService());
    }

    // -- SERVICE REGISTRY HELPER

    private final _Lazy<Map<String, ManagedObject>> objectAdaptersForBeansOfKnownSort =
            _Lazy.threadSafe(this::collectBeansOfKnownSort);

    public void clearRegisteredBeans() {
        objectAdaptersForBeansOfKnownSort.clear();
    }

    @lombok.Value(staticConstructor = "of")
    static class ServiceInstance {
        ObjectSpecification specification;
        Object pojo;
    }

    @Builder.Default
    private final Set<_SingletonBeanProvider> discoveredServices = _Sets.newHashSet();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void registerAsService(final ServiceInstance serviceInstance) {
        var spec = serviceInstance.getSpecification();
        discoveredServices.add(_SingletonBeanProvider.forTestingLazy(
                spec.getLogicalTypeName(),
                (Class)spec.getCorrespondingClass(),
                serviceInstance::getPojo));
    }

    private final Map<String, ManagedObject> collectBeansOfKnownSort() {

        var map = _Maps.<String, ManagedObject>newLinkedHashMap();

        // first pass: introspect them all
        var services = getServiceRegistry()
            .streamRegisteredBeans()
            .map(this::toServiceInstance)
            .map(op->op.orElse(null))
            .filter(_NullSafe::isPresent)
            .peek(this::registerAsService)
            .collect(Can.<ServiceInstance>toCan());

        // reload registered beans
        ((ServiceRegistry_forTesting)getServiceRegistry()).invalidateRegisteredBeans();

        // second pass: adapt service objects
        services.stream()
        .map(service->ManagedObject.service(service.specification, service.pojo))
        .forEach(serviceAdapter->
            map.put(serviceAdapter.getSpecification().getLogicalTypeName(), serviceAdapter));
        return map;
    }

    private Optional<ServiceInstance> toServiceInstance(final _SingletonBeanProvider managedBeanAdapter) {
        var servicePojo = managedBeanAdapter.getInstanceElseFail();

        if(ProgrammingModelConstants.TypeExcludeMarker.anyMatchOn(managedBeanAdapter.getBeanClass())) {
            return Optional.empty();
        }
        return getSpecificationLoader()
            .specForType(servicePojo.getClass())
            .map(serviceSpec->ServiceInstance.of(serviceSpec, servicePojo));
    }

    // -- RECURSIVE INITIALIZATION FIX

    private final List<Runnable> postConstructRunnables = new ArrayList<>();
    public void registerPostconstruct(@NonNull final Runnable postConstructRunnable) {
        postConstructRunnables.add(postConstructRunnable);
    }
    public void runPostconstruct() {
        try {
            postConstructRunnables.stream()
            .forEach(Runnable::run);
        } finally {
            postConstructRunnables.clear();
        }
    }

    // -- VALUE SEMANTICS

    /**
     * Allows to register value-semantics.
     */
    public <T> MetaModelContext_forTesting withValueSemantics(final ValueSemanticsAbstract<T> valueSemantics) {
        var valueClass = valueSemantics.getCorrespondingClass();
        var valueSpec = getSpecificationLoader().loadSpecification(valueClass);
        ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory
            .installValueFacet(valueClass, Can.of(valueSemantics), valueSpec);

        if(ClassUtils.isPrimitiveWrapper(valueClass)) {
            var primitiveType = ClassUtil.unboxPrimitiveIfNecessary(valueClass);
            var primitiveTypeSpec = getSpecificationLoader().loadSpecification(primitiveType);
            ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory
            .installValueFacet(valueClass, Can.of(valueSemantics), primitiveTypeSpec);
        }
        serviceInjector.injectServicesInto(valueSemantics);
        return this;
    }

}
