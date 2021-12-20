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
package org.apache.isis.core.runtime.context;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.viewer.web.WebAppContextPath;
import org.apache.isis.core.interaction.session.MessageBroker;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMementoService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 *
 * @since 2.0
 *
 */
public class IsisAppCommonContext implements HasMetaModelContext {

    /**
     * Can be bootstrapped from a {@link MetaModelContext}
     */
    public static IsisAppCommonContext of(final MetaModelContext metaModelContext) {
        val webAppCommonContext = new IsisAppCommonContext();
        webAppCommonContext.metaModelContext = metaModelContext;
        return webAppCommonContext;
    }

    @Getter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    @Getter(lazy = true)
    private final WebAppContextPath webAppContextPath = lookupServiceElseFail(WebAppContextPath.class);

    @Getter(lazy = true)
    private final MenuBarsService menuBarsService = lookupServiceElseFail(MenuBarsService.class);

    @Getter(lazy = true)
    private final InteractionLayerTracker interactionLayerTracker = lookupServiceElseFail(InteractionLayerTracker.class);

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ObjectMementoService mementoService = lookupServiceElseFail(ObjectMementoService.class);

    public Optional<MessageBroker> getMessageBroker() {
        return getMetaModelContext().getServiceRegistry().lookupService(MessageBroker.class);
    }

    // -- SHORTCUTS

    public <T> Optional<T> lookupService(final Class<T> serviceClass) {
        return getMetaModelContext().getServiceRegistry().lookupService(serviceClass);
    }

    public <T> T lookupServiceElseFail(final Class<T> serviceClass) {
        return getMetaModelContext().getServiceRegistry().lookupServiceElseFail(serviceClass);
    }

    public <T> T lookupServiceElseFallback(final Class<T> serviceClass, final Supplier<T> fallback) {
        return getMetaModelContext().getServiceRegistry().lookupService(serviceClass)
                .orElseGet(fallback);
    }

    public <T> T loadServiceIfAbsent(final Class<T> type, final @Nullable T instanceIfAny) {
        return instanceIfAny==null
                ? lookupServiceElseFail(type)
                : instanceIfAny;
    }

    public <T> T injectServicesInto(final T pojo) {
        return getMetaModelContext().getServiceInjector().injectServicesInto(pojo);
    }

    public ObjectMemento mementoFor(final ManagedObject adapter) {
        return getMementoService().mementoForObject(adapter);
    }

    public ObjectMemento mementoForParameter(@NonNull final ManagedObject adapter) {
        return getMementoService().mementoForParameter(adapter);
    }

    public ObjectMemento mementoForBookmark(final Bookmark bookmark) {
        return getMementoService().mementoForBookmark(bookmark);
    }

    public ManagedObject reconstructObject(final ObjectMemento memento) {
        return getMementoService().reconstructObject(memento);
    }

    // -- FOR THOSE THAT IMPLEMENT BY DELEGATION

    public static interface HasCommonContext {

        IsisAppCommonContext getCommonContext();

        default IsisConfiguration getConfiguration() {
            return getCommonContext().getConfiguration();
        }

        default ServiceRegistry getServiceRegistry() {
            return getCommonContext().getServiceRegistry();
        }

        default SpecificationLoader getSpecificationLoader() {
            return getCommonContext().getSpecificationLoader();
        }

        default ObjectMementoService getMementoService() {
            return getCommonContext().getMementoService();
        }

        default ServiceInjector getServiceInjector() {
            return getCommonContext().getServiceInjector();
        }

        default ObjectManager getObjectManager() {
            return getCommonContext().getObjectManager();
        }

    }


}
