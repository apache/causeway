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

package org.apache.isis.viewer.wicket.viewer.registries.components;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactory.ApplicationAdvice;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistrar;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistrar.ComponentFactoryList;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.webapp.context.IsisWebAppCommonContext;

import lombok.val;

/**
 * Implementation of {@link ComponentFactoryRegistry} that delegates to a
 * provided {@link ComponentFactoryRegistrar}.
 */
@Service
@Named("isisWicketViewer.ComponentFactoryRegistryDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class ComponentFactoryRegistryDefault implements ComponentFactoryRegistry {

    @Inject private ComponentFactoryRegistrar componentFactoryRegistrar;
    @Inject private MetaModelContext metaModelContext;
    
    private ListMultimap<ComponentType, ComponentFactory> componentFactoriesByType;

    @PostConstruct
    public void init() {
        componentFactoriesByType = _Multimaps.newListMultimap();
        registerComponentFactories(componentFactoryRegistrar);
    }

    // ///////////////////////////////////////////////////////
    // Registration
    // ///////////////////////////////////////////////////////

    /**
     * Registers the provided set of component factories.
     */
    protected void registerComponentFactories(final ComponentFactoryRegistrar componentFactoryRegistrar) {

        val componentFactories = new ComponentFactoryList();
        
        componentFactoryRegistrar.addComponentFactories(componentFactories);

        val commonContext = IsisWebAppCommonContext.of(metaModelContext);
        
        for (val componentFactory : componentFactories) {
            registerComponentFactory(commonContext, componentFactory);
        }

        ensureAllComponentTypesRegistered();
    }

    private void registerComponentFactory(
            final IsisWebAppCommonContext commonContext, 
            final ComponentFactory componentFactory) {
        
        // handle dependency injection for factories
        commonContext.getServiceInjector().injectServicesInto(componentFactory);
        if(componentFactory instanceof ComponentFactoryAbstract) {
            ((ComponentFactoryAbstract)componentFactory).setCommonContext(commonContext);
        }
        
        componentFactoriesByType.putElement(componentFactory.getComponentType(), componentFactory);
    }

    private void ensureAllComponentTypesRegistered() {
        for (val componentType : ComponentType.values()) {
            final Collection<ComponentFactory> componentFactories = componentFactoriesByType.get(componentType);
            if (componentFactories.isEmpty()) {
                throw new IllegalStateException("No component factories registered for " + componentType);
            }
        }
    }

    // ///////////////////////////////////////////////////////
    // Public API
    // ///////////////////////////////////////////////////////


    @Override
    public Component addOrReplaceComponent(final MarkupContainer markupContainer, final ComponentType componentType, final IModel<?> model) {
        final Component component = createComponent(componentType, model);
        markupContainer.addOrReplace(component);
        return component;
    }

    @Override
    public Component addOrReplaceComponent(final MarkupContainer markupContainer, final String id, final ComponentType componentType, final IModel<?> model) {
        final Component component = createComponent(componentType, id, model);
        markupContainer.addOrReplace(component);
        return component;
    }

    @Override
    public Component createComponent(final ComponentType componentType, final IModel<?> model) {
        final ComponentFactory componentFactory = findComponentFactoryElseFailFast(componentType, model);
        final Component component = componentFactory.createComponent(model);
        return component;
    }

    @Override
    public Component createComponent(final ComponentType componentType, final String id, final IModel<?> model) {
        final ComponentFactory componentFactory = findComponentFactoryElseFailFast(componentType, model);
        final Component component = componentFactory.createComponent(id, model);
        return component;
    }

    @Override
    public List<ComponentFactory> findComponentFactories(final ComponentType componentType, final IModel<?> model) {
        val componentFactoryList = componentFactoriesByType.get(componentType);
        val matchingFactories = _Lists.<ComponentFactory>newArrayList();
        for (val componentFactory : componentFactoryList) {
            final ApplicationAdvice appliesTo = componentFactory.appliesTo(componentType, model);
            if (appliesTo.applies()) {
                matchingFactories.add(componentFactory);
            }
            if (appliesTo.exclusively()) {
                break;
            }
        }
        if (matchingFactories.isEmpty()) {
            // will just be one
            matchingFactories.addAll(componentFactoriesByType.get(ComponentType.UNKNOWN));
        }
        return matchingFactories;
    }

    @Override
    public ComponentFactory findComponentFactory(final ComponentType componentType, final IModel<?> model) {
        final Collection<ComponentFactory> componentFactories = findComponentFactories(componentType, model);
        return firstOrNull(componentFactories);
    }

    @Override
    public ComponentFactory findComponentFactoryElseFailFast(final ComponentType componentType, final IModel<?> model) {
        final ComponentFactory componentFactory = findComponentFactory(componentType, model);
        if (componentFactory == null) {
            throw new RuntimeException(String.format("could not find component for componentType = '%s'; model object is of type %s", componentType, model.getClass().getName()));
        }
        return componentFactory;
    }

    private static <T> T firstOrNull(final Collection<T> collection) {
        final Iterator<T> iterator = collection.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public Collection<ComponentFactory> listComponentFactories() {
        return componentFactoriesByType.streamElements()
                .collect(Collectors.toList());
    }

}
