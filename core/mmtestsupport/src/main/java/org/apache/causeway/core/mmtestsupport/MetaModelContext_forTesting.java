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
package org.apache.causeway.core.mmtestsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

import org.jspecify.annotations.NonNull;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.grid.GridMarshaller;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.layout.LayoutService;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.render.ObjectRenderService;
import org.apache.causeway.applib.services.render.PlaceholderRenderService;
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
import org.apache.causeway.commons.internal.ioc.SingletonBeanProvider;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.beans.CausewayBeanFactoryPostProcessor;
import org.apache.causeway.core.config.beans.CausewayBeanTypeClassifier;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.commons.ClassUtil;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContextFactory;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconService;
import org.apache.causeway.core.metamodel.facets.object.value.annotcfg.ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelInitFilter;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorDefault;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorForCollections;
import org.apache.causeway.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.causeway.core.metamodel.services.command.CommandDtoFactory;
import org.apache.causeway.core.metamodel.services.events.MetamodelEventService;
import org.apache.causeway.core.metamodel.services.grid.GridLoadingContext;
import org.apache.causeway.core.metamodel.services.grid.GridMarshallerXml;
import org.apache.causeway.core.metamodel.services.grid.GridServiceDefault;
import org.apache.causeway.core.metamodel.services.grid.GridObjectMemberResolver.FallbackLayoutDataSource;
import org.apache.causeway.core.metamodel.services.grid.spi.LayoutResourceLoaderDefault;
import org.apache.causeway.core.metamodel.services.layout.LayoutServiceDefault;
import org.apache.causeway.core.metamodel.services.message.MessageServiceNoop;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.impl.CausewayModuleCoreMetamodelConfigurationDefault;
import org.apache.causeway.core.metamodel.spec.impl._JUnitSupport;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.valuesemantics.BigDecimalValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.TreePathValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.URLValueSemantics;
import org.apache.causeway.core.metamodel.valuesemantics.UUIDValueSemantics;
import org.apache.causeway.core.metamodel.valuetypes.ValueSemanticsResolverDefault;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.core.security.authorization.manager.AuthorizationManager;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
public final class MetaModelContext_forTesting
implements MetaModelContext {

    public static MetaModelContext_forTestingBuilder builder() {
        return new MetaModelContext_forTestingBuilder() {
            @Override
            public MetaModelContext_forTesting build() {
                var mmc = super.build();
                MetaModelContextFactory.setTestContext(mmc);
                return mmc;
            }
        };
    }

    public static MetaModelContext buildDefault() {
        return MetaModelContext_forTesting.builder()
        .build();
    }

    private TestPropertyValues testPropertyValues;

    private ServiceInjector serviceInjector;
    private ServiceRegistry serviceRegistry;

    @Override
    public CausewayConfiguration getConfiguration() {
        return causewayConfigurationLazy.get();
    }
    private final _Lazy<CausewayConfiguration> causewayConfigurationLazy = _Lazy.threadSafe(()->{
        var causewayConfiguration = new ConfigurationTester(
            Optional.ofNullable(this.testPropertyValues)
                .orElseGet(TestPropertyValues::empty))
            .causewayConfiguration();
        return causewayConfiguration;
    });

    @Builder.Default
    private MetamodelEventService metamodelEventService =
        new MetamodelEventService(event->{
            System.out.printf("MetaModelContext_forTesting (logs event to console): %s%n", event);
        });

    @Builder.Default @Getter
    private CausewaySystemEnvironment systemEnvironment = new CausewaySystemEnvironment();

    @Builder.Default @Getter
    private ClassSubstitutorRegistry classSubstitutorRegistry =
        new ClassSubstitutorRegistry(List.of(
                //new ClassSubstitutorForDomainObjects(),
                new ClassSubstitutorForCollections(),
                new ClassSubstitutorDefault()
                ));

    /**
     * Whether to enable all post-processors, that are registered with the {@link ProgrammingModel}.
     * <p>
     * default: false
     */
    @Builder.Default
    private boolean enablePostprocessors = false;

    private ObjectManager objectManager;

    private WrapperFactory wrapperFactory;

    private SpecificationLoader specificationLoader;

    @Builder.Default
    Can<Function<MetaModelContext, MetaModelRefiner>> refiners = Can.empty();

    @Builder.Default
    private BiFunction<MetaModelContext, Can<MetaModelRefiner>, ProgrammingModel> programmingModelFactory =
        (mmc, refiners)->new CausewayModuleCoreMetamodelConfigurationDefault()
            .programmingModel(mmc, refiners.toList(), ProgrammingModelInitFilter.noop());

    @Getter
    private InteractionService interactionService;

    private TranslationService translationService;

    private InteractionContext authentication;

    @Getter
    private AuthorizationManager authorizationManager;

    @Getter
    private AuthenticationManager authenticationManager;

    @Builder.Default @Getter
    private TitleService titleService = new TitleServiceForTesting();

    @Getter
    private ObjectIconService objectIconService;

    @Getter
    private RepositoryService repositoryService;

    private FactoryService factoryService;

    @Getter
    private MemberExecutorService memberExecutor;

    @Getter
    private TransactionService transactionService;

    private TransactionState transactionState;

    private CausewayBeanTypeClassifier causewayBeanTypeClassifier;

    private CausewayBeanTypeRegistry causewayBeanTypeRegistry;

    @Builder.Default @Getter
    private PlaceholderRenderService placeholderRenderService = PlaceholderRenderService.fallback();

    @Builder.Default @Getter
    private ObjectRenderService objectRenderService = ObjectRenderService.fallback();

    @Singular @Getter
    private List<Object> singletons;

    @Singular
    private List<ValueSemanticsProvider<?>> valueSemantics;

    @Singular @Getter
    private List<SingletonBeanProvider> singletonProviders;

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

    Stream<SingletonBeanProvider> streamBeanAdapters() {
        return _Streams.concat(
                streamSingletons().map(SingletonBeanProvider::forTesting),
                singletonProviders.stream(),
                discoveredServices.stream(),
                Stream.of(
                    // support for lazy bean providers,
                    SingletonBeanProvider.forTestingLazy(GridService.class, this::getGridService),
                    SingletonBeanProvider.forTestingLazy(JaxbService.class, this::getJaxbService),
                    SingletonBeanProvider.forTestingLazy(MenuBarsService.class, this::getMenuBarsService),
                    SingletonBeanProvider.forTestingLazy(LayoutService.class, this::getLayoutService),
                    SingletonBeanProvider.forTestingLazy(SpecificationLoader.class, this::getSpecificationLoader)
                )
                );
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
                    getTranslationService());
        }
        return valueSemanticsResolver;
    }

    private final CausewayBeanFactoryPostProcessor causewayBeanFactoryPostProcessorForSpring =
            new CausewayBeanFactoryPostProcessor();

    public CausewayBeanTypeClassifier getCausewayBeanTypeClassifier() {
        if(causewayBeanTypeClassifier==null) {
            causewayBeanTypeClassifier = causewayBeanFactoryPostProcessorForSpring.getCausewayBeanTypeClassifier();
        }
        return causewayBeanTypeClassifier;
    }

    public CausewayBeanTypeRegistry getCausewayBeanTypeRegistry() {
        if(causewayBeanTypeRegistry==null) {
            causewayBeanTypeRegistry = CausewayBeanTypeRegistry.empty();
        }
        return causewayBeanTypeRegistry;
    }

    private final _Lazy<ProgrammingModel> programmingModelRef =
            _Lazy.threadSafe(()->initProgrammingModel());
    @Override
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

            specificationLoader = _JUnitSupport.specLoader(
                    configuration,
                    environment,
                    serviceRegistry,
                    programmingModel,
                    enablePostprocessors,
                    causewayBeanTypeClassifier,
                    causewayBeanTypeRegistry,
                    classSubstitutorRegistry);

        }
        return specificationLoader;
    }

    @Builder.Default @Getter
    private final MessageService messageService = new MessageServiceNoop();

    @Override
    public ManagedObject getHomePageAdapter() {
        // not supported
        return null;
    }

    @Override
    public ObjectManager getObjectManager() {
        if(objectManager==null) {
            objectManager = new ObjectManager(this);
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

    // -- LAYOUT TESTING SUPPORT

    @Getter(lazy = true)
    private final JaxbService jaxbService = JaxbService.simple();

    @Getter(lazy = true)
    private final MenuBarsService menuBarsService = createMenuBarsService();
    private final MenuBarsService createMenuBarsService() {
        return getSingletonProviders().stream()
                .filter(SingletonBeanProvider.satisfying(MenuBarsService.class))
                .findFirst()
                .map(provider->(MenuBarsService)provider.getInstanceElseFail())
                .orElseGet(MenuBarsService::forTesting);
    }

    @Getter(lazy = true)
    private final GridLoadingContext gridLoadingContext = createGridLoadingContext();
    private final GridLoadingContext createGridLoadingContext() {
        return new CausewayModuleCoreMetamodel().gridLoadingContext(
            getSystemEnvironment(),
            getConfiguration(),
            getMessageService(),
            ()->getSpecificationLoader(),
            List.of(getGridMarshaller()),
            List.of(new LayoutResourceLoaderDefault()),
            List.<FallbackLayoutDataSource>of());
    }

    @Getter(lazy = true)
    private final GridMarshaller gridMarshaller = createGridMarshaller();
    private final GridMarshaller createGridMarshaller() {
        return new GridMarshallerXml(getJaxbService());
    }

    @Getter(lazy = true)
    private final GridService gridService = createGridService();
    private final GridService createGridService() {
        return new GridServiceDefault(getGridLoadingContext());
    }

    @Getter(lazy = true)
    private final LayoutService layoutService = createLayoutService();
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

    record ServiceInstance(
        ObjectSpecification specification,
        Object pojo) {
    }

    @Builder.Default
    private final Set<SingletonBeanProvider> discoveredServices = _Sets.newHashSet();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void registerAsService(final ServiceInstance serviceInstance) {
        var spec = serviceInstance.specification();
        discoveredServices.add(SingletonBeanProvider.forTestingLazy(
                spec.logicalTypeName(),
                (Class)spec.getCorrespondingClass(),
                serviceInstance::pojo));
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
            map.put(serviceAdapter.objSpec().logicalTypeName(), serviceAdapter));
        return map;
    }

    private Optional<ServiceInstance> toServiceInstance(final SingletonBeanProvider managedBeanAdapter) {
        var servicePojo = managedBeanAdapter.getInstanceElseFail();

        if(ProgrammingModelConstants.TypeVetoMarker.anyMatchOn(managedBeanAdapter.beanClass())) {
            return Optional.empty();
        }
        return getSpecificationLoader()
            .specForType(servicePojo.getClass())
            .map(serviceSpec->new ServiceInstance(serviceSpec, servicePojo));
    }

    // -- RECURSIVE INITIALIZATION FIX

    private final List<Runnable> postConstructRunnables = new ArrayList<>();
    public void registerPostconstruct(final @NonNull Runnable postConstructRunnable) {
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

    @Getter
    private CommandDtoFactory commandDtoFactory;

}
