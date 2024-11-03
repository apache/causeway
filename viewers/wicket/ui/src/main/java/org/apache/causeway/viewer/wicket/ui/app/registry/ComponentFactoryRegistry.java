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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactoryAbstract;

import lombok.extern.log4j.Log4j2;

/**
 * API for finding registered {@link ComponentFactory}s.
 * <p>
 * Ultimately all requests to locate {@link ComponentFactory}s are routed
 * through to an object implementing this interface.
 */
@Log4j2
public final class ComponentFactoryRegistry {

    private final ListMultimap<UiComponentType, ComponentFactory> componentFactoriesByComponentType =
            _Multimaps.newListMultimap();
    private final Map<Class<? extends ComponentFactory>, ComponentFactory> componentFactoriesByType =
            new HashMap<>();

    public ComponentFactoryRegistry(
            final ComponentFactoryList factoryList,
            final MetaModelContext mmc) {
        super();
        factoryList.forEach(compFactory->this.registerComponentFactory(mmc, compFactory));
        ensureAllComponentTypesRegistered();
    }

    /**
     * Finds the "best" {@link ComponentFactory} for given componentType.
     * <p>
     * Falls back to a {@link UiComponentType#UNKNOWN} lookup.
     */
    public ComponentFactory findComponentFactory(
            final UiComponentType uiComponentType, final @Nullable IModel<?> model) {
        return streamComponentFactories(uiComponentType, model)
            .findFirst()
            .orElseGet(()->streamComponentFactories(UiComponentType.UNKNOWN, model)
                    .findFirst()
                    .orElse(null));
    }

    public ComponentFactory findComponentFactoryElseFail(
            final UiComponentType uiComponentType, final @Nullable IModel<?> model) {
        return streamComponentFactories(uiComponentType, model)
                .findFirst()
                .orElseThrow(()->new RuntimeException(String.format(
                        "could not find component for componentType = '%s'; "
                        + "model object is of type %s; "
                        + "model object='%s'",
                        uiComponentType, model.getClass().getName(), model.getObject())));
    }

    /**
     * As per
     * {@link #addOrReplaceComponent(MarkupContainer, UiComponentType, IModel)},
     * but with the wicket id derived from the {@link UiComponentType}.
     */
    public Component addOrReplaceComponent(final MarkupContainer markupContainer, final UiComponentType uiComponentType, final IModel<?> model) {
        final Component component = createComponent(uiComponentType, model);
        markupContainer.addOrReplace(component);
        return component;
    }

    /**
     * {@link #createComponent(String, UiComponentType, IModel) Creates} the
     * relevant {@link Component} for the provided arguments, and adds to the
     * provided {@link MarkupContainer}; the wicket id is as specified.
     * <p>
     * If none can be found, will fail fast.
     */
    public Component addOrReplaceComponent(final MarkupContainer markupContainer, final String id, final UiComponentType uiComponentType, final IModel<?> model) {
        final Component component = createComponent(id, uiComponentType, model);
        markupContainer.addOrReplace(component);
        return component;
    }

    /**
     * As per {@link #createComponent(String, UiComponentType, IModel)}, but with
     * the wicket id derived from the {@link UiComponentType}.
     *
     * @see #createComponent(String, UiComponentType, IModel)
     */
    public Component createComponent(final UiComponentType uiComponentType, final IModel<?> model) {
        return findComponentFactoryElseFail(uiComponentType, model)
                .createComponent(model);
    }

    /**
     * Create the {@link Component} matching the specified {@link UiComponentType}
     * and {@link IModel} to the provided {@link MarkupContainer}; the id is
     * specified explicitly.
     *
     * <p>
     * If none can be found, will fail fast.
     */
    public Component createComponent(final String id, final UiComponentType uiComponentType, final IModel<?> model) {
        return findComponentFactoryElseFail(uiComponentType, model)
                .createComponent(id, model);
    }

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

    public Stream<ComponentFactory> streamComponentFactories(
            final ImmutableEnumSet<UiComponentType> uiComponentTypes,
            final @Nullable IModel<?> model) {
        return uiComponentTypes.stream()
                .flatMap(componentType->streamComponentFactories(componentType, model));
    }

    @SuppressWarnings("unchecked")
    public <T extends ComponentFactory> Optional<T> lookupFactory(final Class<T> factoryClass) {
        return Optional.ofNullable((T)componentFactoriesByType.get(factoryClass));
    }

    public <T extends ComponentFactory> T lookupFactoryElseFail(final Class<T> factoryClass) {
        return lookupFactory(factoryClass)
            .orElseThrow(()->
                new NoSuchElementException("Could not locate component factory of type '" + factoryClass + "'"));
    }

    // -- HELPER

    private void registerComponentFactory(
            final MetaModelContext commonContext,
            final ComponentFactory componentFactory) {
        componentFactoriesByType.put(componentFactory.getClass(), componentFactory);

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

}