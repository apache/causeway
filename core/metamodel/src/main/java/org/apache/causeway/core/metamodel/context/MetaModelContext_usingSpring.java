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
package org.apache.causeway.core.metamodel.context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.homepage.HomePageResolverService;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.title.TitleService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.ioc._IocContainer;
import org.apache.causeway.commons.internal.ioc._ManagedBeanAdapter;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconService;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.core.security.authorization.manager.AuthorizationManager;

import lombok.Getter;
import lombok.val;


class MetaModelContext_usingSpring implements MetaModelContext {

    private final _IocContainer iocContainer;
    public MetaModelContext_usingSpring(final _IocContainer iocContainer) {
        this.iocContainer = iocContainer;
    }

    @Getter(lazy=true)
    private final CausewaySystemEnvironment systemEnvironment =
    getSingletonElseFail(CausewaySystemEnvironment.class);

    @Getter(lazy=true)
    private final CausewayConfiguration configuration =
    getSingletonElseFail(CausewayConfiguration.class);

    @Getter(lazy=true)
    private final ServiceInjector serviceInjector =
    getSingletonElseFail(ServiceInjector.class);

    @Getter(lazy=true)
    private final FactoryService factoryService =
    getSingletonElseFail(FactoryService.class);

    @Getter(lazy=true)
    private final ServiceRegistry serviceRegistry =
    getSingletonElseFail(ServiceRegistry.class);

    @Getter(lazy=true)
    private final SpecificationLoader specificationLoader =
    getSingletonElseFail(SpecificationLoader.class);

    @Getter(lazy=true)
    private final TranslationService translationService =
    getSingletonElseFail(TranslationService.class);

    @Getter(lazy=true)
    private final AuthorizationManager authorizationManager =
    getSingletonElseFail(AuthorizationManager.class);

    @Getter(lazy=true)
    private final AuthenticationManager authenticationManager =
    getSingletonElseFail(AuthenticationManager.class);

    @Getter(lazy=true)
    private final InteractionProvider interactionProvider =
    getSingletonElseFail(InteractionProvider.class);

    @Getter(lazy=true)
    private final ObjectIconService objectIconService =
    getSingletonElseFail(ObjectIconService.class);

    @Getter(lazy=true)
    private final PlaceholderRenderService placeholderRenderService =
            getDefault(PlaceholderRenderService.class)
            .orElseGet(PlaceholderRenderService::fallback);

    @Getter(lazy=true)
    private final TitleService titleService =
    getSingletonElseFail(TitleService.class);

    @Getter(lazy=true)
    private final RepositoryService repositoryService =
    getSingletonElseFail(RepositoryService.class);

    @Getter(lazy=true)
    private final TransactionService transactionService =
    getSingletonElseFail(TransactionService.class);

    @Getter(lazy=true)
    private final HomePageResolverService homePageResolverService =
    getSingletonElseFail(HomePageResolverService.class);

    @Getter(lazy=true)
    private final ObjectManager objectManager =
    getSingletonElseFail(ObjectManager.class);

    @Getter(lazy=true)
    private final WrapperFactory wrapperFactory =
    getSingletonElseFail(WrapperFactory.class);

    @Getter(lazy=true)
    private final MemberExecutorService memberExecutor =
    getSingletonElseFail(MemberExecutorService.class);

    @Getter(lazy = true)
    private final WebAppContextPath webAppContextPath =
    getSingletonElseFail(WebAppContextPath.class);

    @Getter(lazy = true)
    private final MenuBarsService menuBarsService =
    getSingletonElseFail(MenuBarsService.class);

    @Getter(lazy = true)
    private final MessageService messageService =
    getSingletonElseFail(MessageService.class);

    @Getter(lazy = true)
    private final InteractionService interactionService =
    getSingletonElseFail(InteractionService.class);

    @Override
    public final ManagedObject getHomePageAdapter() {
        final Object pojo = getHomePageResolverService().getHomePage();
        return getObjectManager().adapt(pojo);
    }

    // -- SERVICE SUPPORT

    @Override
    public Stream<ManagedObject> streamServiceAdapters() {
        return objectAdaptersForBeansOfKnownSort.get().values().stream();
    }

    @Override
    public ManagedObject lookupServiceAdapterById(final String serviceId) {
        return objectAdaptersForBeansOfKnownSort.get().get(serviceId);
    }

    // -- HELPER

    private <T> T getSingletonElseFail(final Class<T> type) {
        return iocContainer.getSingletonElseFail(type);
    }

    private <T> Optional<T> getDefault(final Class<T> type) {
        return iocContainer.select(type).getFirst();
    }

    private final _Lazy<Map<String, ManagedObject>> objectAdaptersForBeansOfKnownSort =
            _Lazy.threadSafe(this::collectBeansOfKnownSort);

    private Map<String, ManagedObject> collectBeansOfKnownSort() {

        return getServiceRegistry()
                .streamRegisteredBeans()
                .map(this::toManagedObject)
                .collect(Collectors.toMap(
                        service->service.getSpecification().getLogicalTypeName(),
                        v->v,
                        (o,n)->n,
                        LinkedHashMap::new));
    }

    private ManagedObject toManagedObject(final _ManagedBeanAdapter managedBeanAdapter) {
        val servicePojo = managedBeanAdapter.getInstance().getFirst()
                .orElseThrow(()->_Exceptions.unrecoverable(
                        "Cannot get service instance of type '%s'",
                        managedBeanAdapter.getBeanClass()));
        return getSpecificationLoader()
                .specForType(servicePojo.getClass())
                .map(serviceSpec->ManagedObject.service(serviceSpec, servicePojo))
                .orElseThrow(()->_Exceptions.unrecoverable(
                        "Cannot wrap vetoed service of type '%s'",
                        managedBeanAdapter.getBeanClass()));
    }

}
