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
package org.apache.causeway.viewer.wicket.viewer.registries.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistrar;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistrar.ComponentFactoryList;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Implementation of {@link ComponentFactoryRegistry} that delegates to a
 * provided {@link ComponentFactoryRegistrar}.
 */
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class ComponentFactoryRegistryDefault
implements ComponentFactoryRegistry {

    public static final String LOGICAL_TYPE_NAME =
            CausewayModuleViewerWicketViewer.NAMESPACE + ".ComponentFactoryRegistryDefault";

    @Configuration
    public static class AutoConfiguration {
        @Bean
        @Named(LOGICAL_TYPE_NAME)
        @Order(PriorityPrecedence.MIDPOINT)
        @Qualifier("Default")
        public ComponentFactoryRegistryDefault componentFactoryRegistryDefault(
                final ComponentFactoryRegistrar componentFactoryRegistrar,
                final MetaModelContext metaModelContext
        ) {
            return new ComponentFactoryRegistryDefault(componentFactoryRegistrar, metaModelContext);
        }
    }

    private final ComponentFactoryRegistrar componentFactoryRegistrar;
    private final MetaModelContext metaModelContext;

    private final ListMultimap<UiComponentType, ComponentFactory> componentFactoriesByComponentType =
            _Multimaps.newListMultimap();
    private final Map<Class<? extends ComponentFactory>, ComponentFactory> componentFactoriesByType =
            new HashMap<>();

    @PostConstruct
    public void init() {
        registerComponentFactories(componentFactoryRegistrar);
    }

    // -- REGISTRATION

    /**
     * Registers the provided set of component factories.
     */
    protected void registerComponentFactories(final ComponentFactoryRegistrar componentFactoryRegistrar) {

        var componentFactories = new ComponentFactoryList();

        componentFactoryRegistrar.addComponentFactories(componentFactories);

        var commonContext = metaModelContext;

        for (var componentFactory : componentFactories) {
            registerComponentFactory(commonContext, componentFactory);
            componentFactoriesByType.put(componentFactory.getClass(), componentFactory);
        }

        ensureAllComponentTypesRegistered();
    }

    private void registerComponentFactory(
            final MetaModelContext commonContext,
            final ComponentFactory componentFactory) {

        // handle dependency injection for factories
        commonContext.getServiceInjector().injectServicesInto(componentFactory);
        if(componentFactory instanceof ComponentFactoryAbstract) {
            ((ComponentFactoryAbstract)componentFactory).setMetaModelContext(commonContext);
        }

        componentFactoriesByComponentType.putElement(componentFactory.getComponentType(), componentFactory);
    }

    private void ensureAllComponentTypesRegistered() {
        for (var componentType : UiComponentType.values()) {

            if(componentType.getOptionality().isOptional()) {
                continue;
            }

            if (componentFactoriesByComponentType.getOrElseEmpty(componentType).isEmpty()) {
                throw new IllegalStateException("No component factories registered for " + componentType);
            }
        }
    }

    // -- PUBLIC API

    @Override
    public Component addOrReplaceComponent(final MarkupContainer markupContainer, final UiComponentType uiComponentType, final IModel<?> model) {
        final Component component = createComponent(uiComponentType, model);
        markupContainer.addOrReplace(component);
        return component;
    }

    @Override
    public Component addOrReplaceComponent(final MarkupContainer markupContainer, final String id, final UiComponentType uiComponentType, final IModel<?> model) {
        final Component component = createComponent(id, uiComponentType, model);
        markupContainer.addOrReplace(component);
        return component;
    }

    @Override
    public Component createComponent(final UiComponentType uiComponentType, final IModel<?> model) {
        return findComponentFactoryElseFail(uiComponentType, model)
                .createComponent(model);
    }

    @Override
    public Component createComponent(final String id, final UiComponentType uiComponentType, final IModel<?> model) {
        return findComponentFactoryElseFail(uiComponentType, model)
                .createComponent(id, model);
    }

    @Override
    public Stream<ComponentFactory> streamComponentFactories(
            final UiComponentType uiComponentType,
            final @Nullable IModel<?> model) {

        // find all that apply, unless we find one that applies exclusively
        // in the exclusive case, we just return the exclusive one

        var exclusiveIfAny = _Refs.<ComponentFactory>objectRef(null);

        var allThatApply = componentFactoriesByComponentType.streamElements(uiComponentType)
                .filter(componentFactory->{
                    var advice = componentFactory.appliesTo(uiComponentType, model);
                    if(advice.appliesExclusively()) {
                        exclusiveIfAny.set(componentFactory);
                    }
                    return advice.applies();
                })
                // as an optimization, stop taking when we found an exclusive one
                .takeWhile(__->exclusiveIfAny.isNull())
                .collect(Collectors.toList());

        return (exclusiveIfAny.isNotNull()
                    ? Stream.of(exclusiveIfAny.getValueElseFail())
                    : allThatApply.stream()
                )
                .peek(componentFactory->logComponentResolving(model, uiComponentType, componentFactory));
    }

    @Override
    public Stream<ComponentFactory> streamComponentFactories(
            final ImmutableEnumSet<UiComponentType> uiComponentTypes,
            final @Nullable IModel<?> model) {
        return uiComponentTypes.stream()
                .flatMap(componentType->streamComponentFactories(componentType, model));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ComponentFactory> Optional<T> lookupFactory(final Class<T> factoryClass) {
        return Optional.ofNullable((T)componentFactoriesByType.get(factoryClass));
    }

    // -- DEBUG LOGGING

    private static void logComponentResolving(
            final IModel<?> model,
            final UiComponentType uiComponentType,
            final ComponentFactory componentFactory) {
        if(!log.isDebugEnabled()) return;
        log.debug("component type for model {} -> {} provided by {}",
                _Text.abbreviateClassOf(model),
                uiComponentType.name(),
                _Text.abbreviateClassOf(componentFactory));
    }

    // -- JUNIT SUPPORT

    static ComponentFactoryRegistryDefault forTesting(final List<ComponentFactory> componentFactories) {
        return forTesting(null, null, componentFactories);
    }

    static ComponentFactoryRegistryDefault forTesting(
            final ComponentFactoryRegistrar componentFactoryRegistrar,
            final MetaModelContext metaModelContext,
            final List<ComponentFactory> componentFactories) {
        var factory = new ComponentFactoryRegistryDefault(componentFactoryRegistrar, metaModelContext);
        _NullSafe.stream(componentFactories)
        .forEach(componentFactory->
            factory.componentFactoriesByComponentType.putElement(
                    componentFactory.getComponentType(),
                    componentFactory));
        return factory;
    }



}
