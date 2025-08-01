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

import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.homepage.HomePageResolverService;
import org.apache.causeway.applib.services.i18n.TranslationService;
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
import org.apache.causeway.commons.internal.ioc.SpringContextHolder;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.core.metamodel.execution.MemberExecutorService;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconService;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.services.command.CommandDtoFactory;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.core.security.authorization.manager.AuthorizationManager;

public record MmcRecord(
    CausewaySystemEnvironment systemEnvironment,
    CausewayConfiguration configuration,
    ProgrammingModel programmingModel,
    ServiceInjector serviceInjector,
    FactoryService factoryService,
    ServiceRegistry serviceRegistry,
    SpecificationLoader specificationLoader,
    TranslationService translationService,
    AuthorizationManager authorizationManager,
    AuthenticationManager authenticationManager,
    ObjectIconService objectIconService,
    PlaceholderRenderService placeholderRenderService,
    TitleService titleService,
    RepositoryService repositoryService,
    TransactionService transactionService,
    HomePageResolverService homePageResolverService,
    ObjectManager objectManager,
    WrapperFactory wrapperFactory,
    MemberExecutorService memberExecutor,
    WebAppContextPath webAppContextPath,
    MenuBarsService menuBarsService,
    MessageService messageService,
    InteractionService interactionService,
    CommandDtoFactory commandDtoFactory,
    ManagedObject homePageAdapter) {

    MmcRecord(SpringContextHolder iocContainer) {
        this(
            iocContainer.getSingletonElseFail(CausewaySystemEnvironment.class),
            iocContainer.getSingletonElseFail(CausewayConfiguration.class),
            null,//iocContainer.getSingletonElseFail(ProgrammingModel.class),
            null,//iocContainer.getSingletonElseFail(ServiceInjector.class),
            null,//iocContainer.getSingletonElseFail(FactoryService.class),
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

}
