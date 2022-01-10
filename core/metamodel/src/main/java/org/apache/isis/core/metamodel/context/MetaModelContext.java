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

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
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
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

import lombok.val;

/**
 *
 * @since 2.0
 *
 */
public interface MetaModelContext {

    IsisSystemEnvironment getSystemEnvironment();

    /**
     *
     * Configuration 'beans' with meta-data (IDE-support).
     *
     * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html">spring.io</a>
     *
     */
    IsisConfiguration getConfiguration();

    ObjectManager getObjectManager();

    WrapperFactory getWrapperFactory();

    ServiceInjector getServiceInjector();

    ServiceRegistry getServiceRegistry();

    SpecificationLoader getSpecificationLoader();

    public default Optional<ObjectSpecification> specForType(final @Nullable Class<?> type) {
        return getSpecificationLoader().specForType(type);
    }

    public default ObjectSpecification specForTypeElseFail(final @Nullable Class<?> type) {
        return getSpecificationLoader().specForTypeElseFail(type);
    }

    TranslationService getTranslationService();

    AuthorizationManager getAuthorizationManager();

    AuthenticationManager getAuthenticationManager();

    InteractionProvider getInteractionProvider();

    TitleService getTitleService();

    ObjectIconService getObjectIconService();

    RepositoryService getRepositoryService();

    FactoryService getFactoryService();

    MemberExecutorService getMemberExecutor();

    TransactionService getTransactionService();

    ManagedObject getHomePageAdapter();

    Stream<ManagedObject> streamServiceAdapters();

    ManagedObject lookupServiceAdapterById(String serviceId);

    /**
     * Requires that there is AT LEAST one implementation of the service, and returns it.
     *
     * <p>
     *     If there is more than one implementation, then the one with the &quot;highest&quot;
     *     priority (either annotated with {@link org.springframework.context.annotation.Primary},
     *     else with encountered with earliest {@link org.apache.isis.applib.annotation.PriorityPrecedence precedence})
     *     is used instead.
     * </p>
     *
     * @param type
     * @param <T>
     */
    <T> T getSingletonElseFail(Class<T> type);

    /**
     * Recovers an object (graph) from given {@code bookmark}.
     * Also resolves injection-points for the result.
     */
    default Optional<ManagedObject> loadObject(final @Nullable Bookmark bookmark) {
        if(bookmark==null) {
            return Optional.empty();
        }
        val specLoader = getSpecificationLoader();
        val objManager = getObjectManager();
        return specLoader
                .specForLogicalTypeName(bookmark.getLogicalTypeName())
                .map(spec->objManager.loadObject(
                        ObjectLoader.Request.of(spec, bookmark)));
    }

    // -- EXTRACTORS

    public static MetaModelContext from(final ManagedObject adapter) {
        return adapter.getSpecification().getMetaModelContext();
    }




}
