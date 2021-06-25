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
import java.util.stream.Stream;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.metamodel.execution.MemberExecutorService;
import org.apache.isis.core.metamodel.facets.object.icon.ObjectIconService;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
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

    default InteractionProvider getInteractionProvider() {
        return getMetaModelContext().getInteractionProvider();
    }

    default TitleService getTitleService() {
        return getMetaModelContext().getTitleService();
    }

    default Optional<ObjectSpecification> specForType(final Class<?> type) {
        return getMetaModelContext().specForType(type);
    }

    default ObjectSpecification specForTypeElseFail(final Class<?> type) {
        return getMetaModelContext().specForTypeElseFail(type);
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

    default ObjectManager getObjectManager() {
        return getMetaModelContext().getObjectManager();
    }

    default WrapperFactory getWrapperFactory() {
        return getMetaModelContext().getWrapperFactory();
    }

    // -- ADVANCED SHORTCUTS

    default ManagedObject lookupServiceAdapterById(final String serviceId) {
        return getMetaModelContext().lookupServiceAdapterById(serviceId);
    }

    default <T> T getSingletonElseFail(final Class<T> type) {
        return getMetaModelContext().getSingletonElseFail(type);
    }

    default Stream<ManagedObject> streamServiceAdapters() {
        return getMetaModelContext().streamServiceAdapters();
    }

}
