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
package org.apache.isis.viewer.wicket.ui.app.registry;

import java.util.stream.Stream;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;

/**
 * API for finding registered {@link ComponentFactory}s.
 *
 * <p>
 * Ultimately all requests to locate {@link ComponentFactory}s are routed
 * through to an object implementing this interface.
 */
public interface ComponentFactoryRegistry {

    Stream<ComponentFactory> streamComponentFactories(
            ComponentType componentType, @Nullable IModel<?> model);

    Stream<ComponentFactory> streamComponentFactories(
            ImmutableEnumSet<ComponentType> componentTypes,
            @Nullable IModel<?> model);

    /**
     * Finds the "best" {@link ComponentFactory} for given componentType.
     * <p>
     * Falls back to a {@link ComponentType#UNKNOWN} lookup.
     */
    default ComponentFactory findComponentFactory(
            final ComponentType componentType, final @Nullable IModel<?> model) {
        return streamComponentFactories(componentType, model)
            .findFirst()
            .orElseGet(()->streamComponentFactories(ComponentType.UNKNOWN, model)
                    .findFirst()
                    .orElse(null));
    }

    default ComponentFactory findComponentFactoryElseFail(
            final ComponentType componentType, final @Nullable IModel<?> model) {
        return streamComponentFactories(componentType, model)
                .findFirst()
                .orElseThrow(()->new RuntimeException(String.format(
                        "could not find component for componentType = '%s'; "
                        + "model object is of type %s; "
                        + "model object='%s'",
                        componentType, model.getClass().getName(), model.getObject())));
    }

    /**
     * As per
     * {@link #addOrReplaceComponent(MarkupContainer, ComponentType, IModel)},
     * but with the wicket id derived from the {@link ComponentType}.
     */
    Component addOrReplaceComponent(MarkupContainer markupContainer, ComponentType componentType, IModel<?> model);

    /**
     * {@link #createComponent(ComponentType, String, IModel) Creates} the
     * relevant {@link Component} for the provided arguments, and adds to the
     * provided {@link MarkupContainer}; the wicket id is as specified.
     *
     * <p>
     * If none can be found, will fail fast.
     */
    Component addOrReplaceComponent(MarkupContainer markupContainer, String id, ComponentType componentType, IModel<?> model);

    /**
     * As per {@link #createComponent(ComponentType, String, IModel)}, but with
     * the wicket id derived from the {@link ComponentType}.
     *
     * @see #createComponent(ComponentType, String, IModel)
     */
    Component createComponent(ComponentType componentType, IModel<?> model);

    /**
     * Create the {@link Component} matching the specified {@link ComponentType}
     * and {@link IModel} to the provided {@link MarkupContainer}; the id is
     * specified explicitly.
     *
     * <p>
     * If none can be found, will fail fast.
     */
    Component createComponent(ComponentType componentType, String id, IModel<?> model);



}