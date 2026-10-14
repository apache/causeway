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
package org.apache.causeway.extensions.d3.applib.spi;

import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.RepositoryService;

/**
 * SPI for the D3 viewer extension, allowing domain applications to provide
 * graph data and action bindings per-object-instance.
 * <p>
 * Implementations are discovered via the {@link java.util.ServiceLoader} SPI mechanism
 * and can also be injected with {@link FactoryService}, {@link RepositoryService}, etc.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface D3GraphAdvisor {

    /**
     * Optionally returns advice for a given domain object (represented by its class).
     *
     * @param domainClass the domain class being rendered
     * @return advice, or {@code null} if not applicable
     */
    Advice advise(Class<?> domainClass);

    /**
     * Advice for rendering a D3 graph for a given domain class.
     */
    record Advice(
            /** The JSON graph data as a String (the D3 graph model). */
            String graphData,
            /** Optional custom initial width (overrides annotation/default). */
            Integer initialWidth,
            /** Optional custom initial height (overrides annotation/default). */
            Integer initialHeight,
            /** Whether node dragging is enabled. */
            boolean enableDragging,
            /** Whether zoom/pan is enabled. */
            boolean enableZoom,
            /** Optional member id of a domain action to invoke when a graph node is clicked;
             *  {@code null} means keep the annotation/config default, an empty string means none. */
            String clickAction) {}
}