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

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.factory.FactoryService;
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
import org.apache.causeway.commons.internal.exceptions._Exceptions;
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

/**
 * @since 2.0
 */
@Programmatic
public interface MetaModelContext extends MetaModelContextShortcuts {

    // -- INSTANCE (SINGLETON)

    static Optional<MetaModelContext> instance() {
        return MetaModelContextSingletonHolder.instance();
    }
    @Nullable
    static MetaModelContext instanceNullable() {
        return instance().orElse(null);
    }
    static MetaModelContext instanceElseFail() {
        return instance()
                .orElseThrow(()->_Exceptions.noSuchElement("MetaModelContext not yet or no longer available."));
    }

    // -- UTILITY

    static TranslationService translationServiceOrFallback() {
        return instance()
            .map(MetaModelContext::getTranslationService)
            .orElseGet(TranslationService::identity);
    }

    // -- SHORTCUTS

    @Override
    default MetaModelContext mmc() {
        return this;
    }

    // -- BEANS

    CausewaySystemEnvironment getSystemEnvironment();
    CausewayConfiguration getConfiguration();
    ProgrammingModel getProgrammingModel();
    ServiceInjector getServiceInjector();
    ServiceRegistry getServiceRegistry();
    FactoryService getFactoryService();
    MemberExecutorService getMemberExecutor();
    SpecificationLoader getSpecificationLoader();
    TranslationService getTranslationService();
    AuthorizationManager getAuthorizationManager();
    AuthenticationManager getAuthenticationManager();
    TitleService getTitleService();
    RepositoryService getRepositoryService();
    ManagedObject getHomePageAdapter();
    TransactionService getTransactionService();
    ObjectIconService getObjectIconService();
    MessageService getMessageService();
    ObjectManager getObjectManager();
    WrapperFactory getWrapperFactory();
    PlaceholderRenderService getPlaceholderRenderService();
    WebAppContextPath getWebAppContextPath();
    MenuBarsService getMenuBarsService();
    InteractionService getInteractionService();
    CommandDtoFactory getCommandDtoFactory();

}
