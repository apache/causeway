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

import java.util.stream.Stream;

import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.render.ObjectRenderService;
import org.apache.causeway.applib.services.render.PlaceholderRenderService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.title.TitleService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.xactn.TransactionService;
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

public interface HasMetaModelContext extends MetaModelContext  {

    // -- INTERFACE

    default MetaModelContext getMetaModelContext() {
        return MetaModelContext.instanceElseFail();
    }

    // -- DELEGATING

    @Override
    default CausewaySystemEnvironment getSystemEnvironment() {
        return getMetaModelContext().getSystemEnvironment();
    }

    @Override
    default CausewayConfiguration getConfiguration() {
        return getMetaModelContext().getConfiguration();
    }

    @Override
    default ProgrammingModel getProgrammingModel() {
        return getMetaModelContext().getProgrammingModel();
    }

    @Override
    default ServiceInjector getServiceInjector() {
        return getMetaModelContext().getServiceInjector();
    }

    @Override
    default ServiceRegistry getServiceRegistry() {
        return getMetaModelContext().getServiceRegistry();
    }

    @Override
    default FactoryService getFactoryService() {
        return getMetaModelContext().getFactoryService();
    }

    @Override
    default MemberExecutorService getMemberExecutor() {
        return getMetaModelContext().getMemberExecutor();
    }

    @Override
    default SpecificationLoader getSpecificationLoader() {
        return getMetaModelContext().getSpecificationLoader();
    }

    @Override
    default TranslationService getTranslationService() {
        return getMetaModelContext().getTranslationService();
    }

    @Override
    default AuthorizationManager getAuthorizationManager() {
        return getMetaModelContext().getAuthorizationManager();
    }

    @Override
    default AuthenticationManager getAuthenticationManager() {
        return getMetaModelContext().getAuthenticationManager();
    }

    @Override
    default TitleService getTitleService() {
        return getMetaModelContext().getTitleService();
    }

    @Override
    default RepositoryService getRepositoryService() {
        return getMetaModelContext().getRepositoryService();
    }

    @Override
    default ManagedObject getHomePageAdapter() {
        return getMetaModelContext().getHomePageAdapter();
    }

    @Override
    default TransactionService getTransactionService() {
        return getMetaModelContext().getTransactionService();
    }

    @Override
    default ObjectIconService getObjectIconService() {
        return getMetaModelContext().getObjectIconService();
    }

    @Override
    default MessageService getMessageService() {
        return getMetaModelContext().getMessageService();
    }

    @Override
    default ObjectManager getObjectManager() {
        return getMetaModelContext().getObjectManager();
    }

    @Override
    default WrapperFactory getWrapperFactory() {
        return getMetaModelContext().getWrapperFactory();
    }

    @Override
    default ObjectRenderService getObjectRenderService() {
        return getMetaModelContext().getObjectRenderService();
    }

    @Override
    default PlaceholderRenderService getPlaceholderRenderService() {
        return getMetaModelContext().getPlaceholderRenderService();
    }

    @Override
    default WebAppContextPath getWebAppContextPath() {
        return getMetaModelContext().getWebAppContextPath();
    }

    @Override
    default MenuBarsService getMenuBarsService() {
        return getMetaModelContext().getMenuBarsService();
    }

    @Override
    default InteractionService getInteractionService() {
        return getMetaModelContext().getInteractionService();
    }

    @Override
    default CommandDtoFactory getCommandDtoFactory() {
        return getMetaModelContext().getCommandDtoFactory();
    }

    // -- SERVICE SUPPORT

    @Override
    default Stream<ManagedObject> streamServiceAdapters() {
        return getMetaModelContext().streamServiceAdapters();
    }

    @Override
    default ManagedObject lookupServiceAdapterById(final String serviceId) {
        return getMetaModelContext().lookupServiceAdapterById(serviceId);
    }

}
