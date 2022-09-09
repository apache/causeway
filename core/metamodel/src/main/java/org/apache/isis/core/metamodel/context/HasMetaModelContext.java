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
package org.apache.isis.core.metamodel.context;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.locale.UserLocale;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.placeholder.PlaceholderRenderService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.viewer.web.WebAppContextPath;
import org.apache.isis.core.metamodel.execution.MemberExecutorService;
import org.apache.isis.core.metamodel.facets.object.icon.ObjectIconService;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.services.message.MessageBroker;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

@FunctionalInterface
public interface HasMetaModelContext {

    // -- INTERFACE

    MetaModelContext getMetaModelContext();

    // -- SHORTCUTS

    default IsisSystemEnvironment getSystemEnvironment() {
        return getMetaModelContext().getSystemEnvironment();
    }

    default IsisConfiguration getConfiguration() {
        return getMetaModelContext().getConfiguration();
    }

    default ServiceInjector getServiceInjector() {
        return getMetaModelContext().getServiceInjector();
    }

    default ServiceRegistry getServiceRegistry() {
        return getMetaModelContext().getServiceRegistry();
    }

    default FactoryService getFactoryService() {
        return getMetaModelContext().getFactoryService();
    }

    default MemberExecutorService getMemberExecutor() {
        return getMetaModelContext().getMemberExecutor();
    }

    default SpecificationLoader getSpecificationLoader() {
        return getMetaModelContext().getSpecificationLoader();
    }

    default TranslationService getTranslationService() {
        return getMetaModelContext().getTranslationService();
    }

    default AuthorizationManager getAuthorizationManager() {
        return getMetaModelContext().getAuthorizationManager();
    }

    default AuthenticationManager getAuthenticationManager() {
        return getMetaModelContext().getAuthenticationManager();
    }

    default TitleService getTitleService() {
        return getMetaModelContext().getTitleService();
    }

    default RepositoryService getRepositoryService() {
        return getMetaModelContext().getRepositoryService();
    }

    default ManagedObject getHomePageAdapter() {
        return getMetaModelContext().getHomePageAdapter();
    }

    default TransactionService getTransactionService() {
        return getMetaModelContext().getTransactionService();
    }

    default ObjectIconService getObjectIconService() {
        return getMetaModelContext().getObjectIconService();
    }

    default MessageService getMessageService() {
        return getMetaModelContext().getMessageService();
    }

    default ObjectManager getObjectManager() {
        return getMetaModelContext().getObjectManager();
    }

    default WrapperFactory getWrapperFactory() {
        return getMetaModelContext().getWrapperFactory();
    }

    default PlaceholderRenderService getPlaceholderRenderService() {
        return getMetaModelContext().getPlaceholderRenderService();
    }

    default WebAppContextPath getWebAppContextPath() {
        return getMetaModelContext().getWebAppContextPath();
    }

    default MenuBarsService getMenuBarsService() {
        return getMetaModelContext().getMenuBarsService();
    }

    default InteractionService getInteractionService() {
        return getMetaModelContext().getInteractionService();
    }

    default Optional<UserLocale> currentUserLocale() {
        return getInteractionService().currentInteractionContext()
                .map(InteractionContext::getLocale);
    }

    // -- SPEC SHORTCUTS

    default Optional<ObjectSpecification> specForType(final @Nullable Class<?> type) {
        return getSpecificationLoader().specForType(type);
    }

    default ObjectSpecification specForTypeElseFail(final @Nullable Class<?> type) {
        return getSpecificationLoader().specForTypeElseFail(type);
    }

    // -- SERVICE SHORTCUTS

    public default <T> Optional<T> lookupService(final Class<T> serviceClass) {
        return getMetaModelContext().getServiceRegistry().lookupService(serviceClass);
    }

    public default <T> T lookupServiceElseFail(final Class<T> serviceClass) {
        return getMetaModelContext().getServiceRegistry().lookupServiceElseFail(serviceClass);
    }

    public default <T> T lookupServiceElseFallback(final Class<T> serviceClass, final Supplier<T> fallback) {
        return getMetaModelContext().getServiceRegistry().lookupService(serviceClass)
                .orElseGet(fallback);
    }

    public default <T> T loadServiceIfAbsent(final Class<T> type, final @Nullable T instanceIfAny) {
        return instanceIfAny==null
                ? lookupServiceElseFail(type)
                : instanceIfAny;
    }

    public default <T> T injectServicesInto(final T pojo) {
        return getMetaModelContext().getServiceInjector().injectServicesInto(pojo);
    }

    // -- ADVANCED SHORTCUTS

    public default Optional<MessageBroker> getMessageBroker() {
        // session scoped!
        return getMetaModelContext().getServiceRegistry().lookupService(MessageBroker.class);
    }

    default ManagedObject lookupServiceAdapterById(final String serviceId) {
        return getMetaModelContext().lookupServiceAdapterById(serviceId);
    }

    default Stream<ManagedObject> streamServiceAdapters() {
        return getMetaModelContext().streamServiceAdapters();
    }

}
