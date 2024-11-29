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

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;

import lombok.NonNull;

/**
 * Serializable key into the {@link ServiceRegistry} to lookup {@link ComponentFactory}s,
 * in order for the latter not having to be {@link Serializable}.
 */
public record ComponentFactoryKey(
    Class<? extends ComponentFactory> factoryClass,
    String id,
    String label,
    UiComponentType componentType,
    int orderOfAppearanceInUiDropdown,
    boolean isPageReloadRequiredOnTableViewActivation,
    String cssClass,
    LoadableDetachableModel<ComponentFactory> componentFactoryLazy
    ) implements Serializable {

    public ComponentFactory componentFactory() {
        return componentFactoryLazy.getObject();
    }

    public ComponentFactoryKey(final @NonNull ComponentFactory componentFactory) {
        this(componentFactory, componentFactory.getClass());
    }

    private ComponentFactoryKey(
        final ComponentFactory componentFactory,
        final Class<? extends ComponentFactory> factoryClass) {
        this(
            /*factoryClass*/
            factoryClass,
            /*id*/
            componentFactory.getName(),
            /*label*/
            _Casts.castTo(CollectionContentsAsFactory.class, componentFactory)
                .map(CollectionContentsAsFactory::getTitleLabel)
                .map(IModel::getObject)
                .orElseGet(()->componentFactory.getName()),
            /*componentType*/
            componentFactory.getComponentType(),
            /*orderOfAppearanceInUiDropdown*/
            componentFactory instanceof CollectionContentsAsFactory collectionContentsAsFactory
                ? collectionContentsAsFactory.orderOfAppearanceInUiDropdown()
                : Integer.MAX_VALUE,
            /*isPageReloadRequiredOnTableViewActivation*/
            componentFactory instanceof CollectionContentsAsFactory collectionContentsAsFactory
                ? collectionContentsAsFactory.isPageReloadRequiredOnTableViewActivation()
                : false,
            /*cssClass*/
            _Casts.castTo(CollectionContentsAsFactory.class, componentFactory)
                .map(CollectionContentsAsFactory::getCssClass)
                .map(IModel::getObject)
                .orElseGet(()->
                    _Strings.asLowerDashed.apply(componentFactory.getName())),
            /*componentFactoryLazy*/
            LoadableDetachableModel.of(()->MetaModelContext.instanceElseFail().getServiceRegistry()
                .lookupServiceElseFail(ComponentFactoryRegistry.class)
                .lookupFactoryElseFail(factoryClass)));

        componentFactoryLazy.setObject(componentFactory); // memoize
    }

}
