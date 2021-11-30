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
package org.apache.isis.core.metamodel._testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

import org.springframework.core.env.AbstractEnvironment;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.grid.GridLoaderService;
import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Streams;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.ioc._ManagedBeanAdapter;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.beans.IsisBeanFactoryPostProcessorForSpring;
import org.apache.isis.core.config.beans.IsisBeanTypeClassifier;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistryDefault;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.execution.MemberExecutorService;
import org.apache.isis.core.metamodel.facets.object.icon.ObjectIconService;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.ObjectManagerDefault;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.isis.core.metamodel.progmodels.dflt.ProgrammingModelFacetsJava11;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;
import org.apache.isis.core.metamodel.services.grid.GridLoaderServiceDefault;
import org.apache.isis.core.metamodel.services.grid.GridReaderUsingJaxb;
import org.apache.isis.core.metamodel.services.grid.GridServiceDefault;
import org.apache.isis.core.metamodel.services.grid.bootstrap3.GridSystemServiceBootstrap;
import org.apache.isis.core.metamodel.services.layout.LayoutServiceDefault;
import org.apache.isis.core.metamodel.services.message.MessageServiceNoop;
import org.apache.isis.core.metamodel.services.title.TitleServiceDefault;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderDefault;
import org.apache.isis.core.metamodel.valuesemantics.BigDecimalValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.URLValueSemantics;
import org.apache.isis.core.metamodel.valuesemantics.UUIDValueSemantics;
import org.apache.isis.core.metamodel.valuetypes.ValueSemanticsResolverDefault;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.val;

@Builder @Getter
public final class MetaModelContext_forTesting
implements MetaModelContext {

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
        .build();

    @Builder.Default
    private IsisSystemEnvironment systemEnvironment = newIsisSystemEnvironment();

    @Builder.Default
    private IsisConfiguration configuration = newIsisConfiguration();

    @Builder.Default
    private ClassSubstitutorRegistry classSubstitutorRegistry = new ClassSubstitutorRegistry(Collections.emptyList());

    private ObjectManager objectManager;

    private WrapperFactory wrapperFactory;

    private SpecificationLoader specificationLoader;

    @Builder.Default
    private Function<MetaModelContext, ProgrammingModel> programmingModelFactory = ProgrammingModelFacetsJava11::new;

    private InteractionProvider interactionProvider;

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

    private IsisBeanTypeClassifier isisBeanTypeClassifier;

    private IsisBeanTypeRegistry isisBeanTypeRegistry;

    //private Map<String, ManagedObject> serviceAdaptersById;

    @Singular
    private List<Object> singletons;

    @Singular
    private List<ValueSemanticsProvider<?>> valueSemantics;

    @Singular
    private List<_ManagedBeanAdapter> singletonProviders;

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

    @Override
    public <T> T getSingletonElseFail(final Class<T> type) {
        return getSystemEnvironment().ioc().getSingletonElseFail(type);
    }

    private Stream<Object> streamSingletons() {

        val fields = Stream.of(
                getConfiguration(),
                getObjectManager(),
                getWrapperFactory(),
                getIsisBeanTypeClassifier(),
                getIsisBeanTypeRegistry(),
                systemEnvironment,
                classSubstitutorRegistry,
                serviceInjector,
                serviceRegistry,
                metamodelEventService,
                messageService,
//                specificationLoader,
                interactionProvider,
                getTranslationService(),
                authentication,
                authorizationManager,
                authenticationManager,
                titleService,
                repositoryService,
                transactionService,
                transactionState,
                getValueSemanticsResolver(),
                new ObjectMementoService_forTesting(),
                new BigDecimalValueSemantics(),
                new URLValueSemantics(),
                new UUIDValueSemantics(),
                this);

        return Stream.concat(
                fields,
                getSingletons().stream())
                .filter(_NullSafe::isPresent);
    }

    Stream<_ManagedBeanAdapter> streamBeanAdapters() {
        return _Streams.concat(
                streamSingletons().map(_ManagedBeanAdapter::forTesting),
                Stream.of(
                    // support for lazy bean providers,
                    _ManagedBeanAdapter.forTestingLazy(GridLoaderService.class, this::getGridLoaderService),
                    _ManagedBeanAdapter.forTestingLazy(GridService.class, this::getGridService),
                    _ManagedBeanAdapter.forTestingLazy(JaxbService.class, this::getJaxbService),
                    _ManagedBeanAdapter.forTestingLazy(MenuBarsService.class, this::getMenuBarsService),
                    _ManagedBeanAdapter.forTestingLazy(LayoutService.class, this::getLayoutService),
                    _ManagedBeanAdapter.forTestingLazy(SpecificationLoader.class, this::getSpecificationLoader)
                ),
                singletonProviders.stream());
    }

    private static IsisSystemEnvironment newIsisSystemEnvironment() {
        val env = new IsisSystemEnvironment();
        env.setUnitTesting(true);
        return env;
    }

    private static IsisConfiguration newIsisConfiguration() {
        val properties = _Maps.<String, String>newHashMap();
        val config = new IsisConfiguration(new AbstractEnvironment() {
            @Override
            public String getProperty(final String key) {
                return properties.get(key);
            }
        });
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
            valueSemanticsResolver = new ValueSemanticsResolverDefault(valueSemantics, getTranslationService());
        }
        return valueSemanticsResolver;
    }

    private final IsisBeanFactoryPostProcessorForSpring isisBeanFactoryPostProcessorForSpring =
            new IsisBeanFactoryPostProcessorForSpring();

    public IsisBeanTypeClassifier getIsisBeanTypeClassifier() {
        if(isisBeanTypeClassifier==null) {
            isisBeanTypeClassifier = isisBeanFactoryPostProcessorForSpring.getIsisBeanTypeClassifier();
        }
        return isisBeanTypeClassifier;
    }

    public IsisBeanTypeRegistry getIsisBeanTypeRegistry() {
        if(isisBeanTypeRegistry==null) {
            isisBeanTypeRegistry = new IsisBeanTypeRegistryDefault(Can.empty());
        }
        return isisBeanTypeRegistry;
    }

    private final _Lazy<ProgrammingModel> programmingModelRef =
            _Lazy.threadSafe(()->initProgrammingModel());
    public ProgrammingModel getProgrammingModel() {
        return programmingModelRef.get();
    }
    private final ProgrammingModel initProgrammingModel() {
        val programmingModel = programmingModelFactory.apply(this);
        ((ProgrammingModelAbstract)programmingModel).init(new ProgrammingModelInitFilterDefault());
        return programmingModel;
    }

    @Override
    public SpecificationLoader getSpecificationLoader() {
        if(specificationLoader==null) {

            val configuration = requireNonNull(getConfiguration());
            val environment = requireNonNull(getSystemEnvironment());
            val serviceRegistry = requireNonNull(getServiceRegistry());
            @SuppressWarnings("unused")
            val serviceInjector = requireNonNull(getServiceInjector());
            val programmingModel = requireNonNull(getProgrammingModel());
            val isisBeanTypeClassifier = requireNonNull(getIsisBeanTypeClassifier());
            val isisBeanTypeRegistry = requireNonNull(getIsisBeanTypeRegistry());

            specificationLoader = SpecificationLoaderDefault.getInstance(
                    configuration,
                    environment,
                    serviceRegistry,
                    programmingModel,
                    isisBeanTypeClassifier,
                    isisBeanTypeRegistry);

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


    public void runWithConfigProperties(final Consumer<Map<String, String>> setup, final Runnable runnable) {
        val properties = _Maps.<String, String>newHashMap();
        setup.accept(properties);

        val currentConfigBackup = this.configuration;
        try {

            this.configuration = new IsisConfiguration(new AbstractEnvironment() {
                @Override
                public String getProperty(final String key) {
                    return properties.get(key);
                }
            });

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
                .filter(provider->provider.isCandidateFor(MenuBarsService.class))
                .findFirst()
                .map(provider->(MenuBarsService)provider.getInstance().getFirstOrFail())
                .orElseGet(MenuBarsService::forTesting);
    }

    @Getter(lazy = true)
    private final GridReaderUsingJaxb gridReader = createGridReader();
    //XXX lombok issue: won't compile if inlined
    private final GridReaderUsingJaxb createGridReader() {
        return new GridReaderUsingJaxb(getJaxbService(), getServiceRegistry());
    }

    @Getter(lazy = true)
    private final GridLoaderService gridLoaderService = createGridLoaderService();
    //XXX lombok issue: won't compile if inlined
    private final GridLoaderService createGridLoaderService() {
        return new GridLoaderServiceDefault(getGridReader(), getMessageService(), /*support reloading*/true);
    }

    @Getter(lazy = true)
    private final GridService gridService = createGridService();
    //XXX lombok issue: won't compile if inlined
    private final GridService createGridService() {
        return new GridServiceDefault(
            getGridLoaderService(), _Lists.of(
                    new GridSystemServiceBootstrap(this::getGridReader,
                            getSpecificationLoader(),
                            getTranslationService(),
                            getJaxbService(),
                            getMessageService(),
                            getSystemEnvironment()))); // support reloading
    }

    @Getter(lazy = true)
    private final LayoutService layoutService = createLayoutService();
    //XXX lombok issue: won't compile if inlined
    private final LayoutService createLayoutService() {
        return new LayoutServiceDefault(
                getSpecificationLoader(),
                getJaxbService(),
                getGridService(),
                getMenuBarsService());
    }

    // -- SERVICE REGISTRY HELPER

    final _Lazy<Map<String, ManagedObject>> objectAdaptersForBeansOfKnownSort =
            _Lazy.threadSafe(this::collectBeansOfKnownSort);


    private final Map<String, ManagedObject> collectBeansOfKnownSort() {

        return getServiceRegistry()
                .streamRegisteredBeans()
                .map(this::toManagedObject)
                .collect(Collectors.toMap(
                        serviceAdapter->serviceAdapter.getSpecification().getFullIdentifier(),
                        v->v, (o,n)->n, LinkedHashMap::new));
    }

    private final ManagedObject toManagedObject(final _ManagedBeanAdapter managedBeanAdapter) {

        val servicePojo = managedBeanAdapter.getInstance().getFirst()
                .orElseThrow(()->_Exceptions.unrecoverableFormatted(
                        "Cannot get service instance of type '%s'",
                        managedBeanAdapter.getBeanClass()));

        return ManagedObject.lazy(getSpecificationLoader(), servicePojo);

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
}
