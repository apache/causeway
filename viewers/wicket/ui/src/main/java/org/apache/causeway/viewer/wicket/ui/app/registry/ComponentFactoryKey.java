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
package org.apache.causeway.viewer.wicket.ui.app.registry;

import java.io.Serializable;
import java.util.function.Supplier;

import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Serializable key into the {@link ServiceRegistry} to lookup {@link ComponentFactory}s,
 * in order for the latter not having to be {@link Serializable}.
 */
public final class ComponentFactoryKey implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter @Accessors(fluent=true)
    private final Class<? extends ComponentFactory> factoryClass;
    @Getter @Accessors(fluent=true)
    private final String id;
    @Getter @Accessors(fluent=true)
    private final String label;
    @Getter @Accessors(fluent=true)
    private final UiComponentType componentType;
    @Getter @Accessors(fluent=true)
    private int orderOfAppearanceInUiDropdown;
    @Getter @Accessors(fluent=true)
    private boolean isPageReloadRequiredOnTableViewActivation;
    @Getter @Accessors(fluent=true)
    private String cssClass;

    // access only through resolve(...)
    private transient ComponentFactory componentFactory;

    public ComponentFactoryKey(final @NonNull ComponentFactory componentFactory) {
        this.factoryClass = componentFactory.getClass();
        this.componentFactory = componentFactory;
        this.id = componentFactory.getName();
        this.label = _Casts.castTo(CollectionContentsAsFactory.class, componentFactory)
                .map(CollectionContentsAsFactory::getTitleLabel)
                .map(IModel::getObject)
                .orElseGet(()->componentFactory.getName());
        this.componentType = componentFactory.getComponentType();
        this.orderOfAppearanceInUiDropdown =
                componentFactory instanceof CollectionContentsAsFactory
                    ? ((CollectionContentsAsFactory) componentFactory).orderOfAppearanceInUiDropdown()
                    : Integer.MAX_VALUE;
        this.isPageReloadRequiredOnTableViewActivation =
                componentFactory instanceof CollectionContentsAsFactory
                    ? ((CollectionContentsAsFactory) componentFactory).isPageReloadRequiredOnTableViewActivation()
                    : false;

        this.cssClass = _Casts.castTo(CollectionContentsAsFactory.class, componentFactory)
                .map(CollectionContentsAsFactory::getCssClass)
                .map(IModel::getObject)
                .orElseGet(()->
                    _Strings.asLowerDashed.apply(componentFactory.getName()));
    }

    public ComponentFactory resolve(final @NonNull Supplier<ServiceRegistry> serviceRegistrySupplier) {
        return componentFactory != null
                ? componentFactory
                : (this.componentFactory = componentFactoryRegistry(serviceRegistrySupplier).lookupFactoryElseFail(factoryClass));
    }
    
    ComponentFactoryRegistry componentFactoryRegistry(final @NonNull Supplier<ServiceRegistry> serviceRegistrySupplier) {
        return serviceRegistrySupplier.get().lookupServiceElseFail(ComponentFactoryRegistry.class);
    }

}
