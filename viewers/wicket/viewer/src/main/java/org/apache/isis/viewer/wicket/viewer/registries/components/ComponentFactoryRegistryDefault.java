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

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistrar;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistrar.ComponentFactoryList;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Implementation of {@link ComponentFactoryRegistry} that delegates to a
 * provided {@link ComponentFactoryRegistrar}.
 */
@Service
@Named("isis.viewer.wicket.ComponentFactoryRegistryDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class ComponentFactoryRegistryDefault
implements ComponentFactoryRegistry {

    @Inject private ComponentFactoryRegistrar componentFactoryRegistrar;
    @Inject private MetaModelContext metaModelContext;

    private final ListMultimap<ComponentType, ComponentFactory> componentFactoriesByType =
            _Multimaps.newListMultimap();

    @PostConstruct
    public void init() {
        registerComponentFactories(componentFactoryRegistrar);
    }

    // -- REGISTRATION

    /**
     * Registers the provided set of component factories.
     */
    protected void registerComponentFactories(final ComponentFactoryRegistrar componentFactoryRegistrar) {

        val componentFactories = new ComponentFactoryList();

        componentFactoryRegistrar.addComponentFactories(componentFactories);

        val commonContext = IsisAppCommonContext.of(metaModelContext);

        for (val componentFactory : componentFactories) {
            registerComponentFactory(commonContext, componentFactory);
        }

        ensureAllComponentTypesRegistered();
    }

    private void registerComponentFactory(
            final IsisAppCommonContext commonContext,
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

            if(componentType.getOptionality().isOptional()) {
                continue;
            }

            if (componentFactoriesByType.getOrElseEmpty(componentType).isEmpty()) {
                throw new IllegalStateException("No component factories registered for " + componentType);
            }
        }
    }

    // -- PUBLIC API

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
        return findComponentFactoryElseFail(componentType, model)
                .createComponent(model);
    }

    @Override
    public Component createComponent(final ComponentType componentType, final String id, final IModel<?> model) {
        return findComponentFactoryElseFail(componentType, model)
                .createComponent(id, model);
    }

    @Override
    public Stream<ComponentFactory> streamComponentFactories(
            final ComponentType componentType,
            final @Nullable IModel<?> model) {
        return componentFactoriesByType.streamElements(componentType)
                .filter(componentFactory->componentFactory.appliesTo(componentType, model).applies())
                .peek(componentFactory->logComponentResolving(model, componentType, componentFactory));
    }

    @Override
    public Stream<ComponentFactory> streamComponentFactories(
            final ImmutableEnumSet<ComponentType> componentTypes,
            final @Nullable IModel<?> model) {
        return componentTypes.stream()
                .flatMap(componentType->streamComponentFactories(componentType, model));
    }

    // -- DEBUG LOGGING

    private static void logComponentResolving(
            final IModel<?> model,
            final ComponentType componentType,
            final ComponentFactory componentFactory) {
        if(!log.isDebugEnabled()) return;
        log.debug("component type for model {} -> {} provided by {}",
                _Text.abbreviateClassOf(model),
                componentType.name(),
                _Text.abbreviateClassOf(componentFactory));
    }

    // -- JUNIT SUPPORT

    static ComponentFactoryRegistryDefault forTesting(final List<ComponentFactory> componentFactories) {
        val factory = new ComponentFactoryRegistryDefault();
        _NullSafe.stream(componentFactories)
        .forEach(componentFactory->
            factory.componentFactoriesByType.putElement(
                    componentFactory.getComponentType(),
                    componentFactory));
        return factory;
    }



}
