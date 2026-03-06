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
package org.apache.causeway.core.metamodel.services.grid;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Provider;

import org.apache.causeway.applib.layout.resource.LayoutResourceLoader;
import org.apache.causeway.applib.services.grid.GridMarshaller;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Instantiated by {@link CausewayModuleCoreMetamodel} (Spring managed bean).
 */
@AllArgsConstructor
@Getter @Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public final class GridLoadingContext {

	private final CausewaySystemEnvironment causewaySystemEnvironment;
	private final CausewayConfiguration causewayConfiguration;
	private final MessageService messageService;
	private final Provider<SpecificationLoader> specLoaderProvider;
	private final Map<CommonMimeType, GridMarshaller> marshallersByMime;
	private final Can<LayoutResourceLoader> layoutResourceLoaders;
    /**
     * Whether dynamic reloading of layouts is enabled.
     *
     * <p> The default implementation enables reloading for prototyping mode,
     * disables in production
     */
	private final boolean supportsReloading;

    /** Factory also to be used for JUnit tests. */
    public static GridLoadingContext create(
        final CausewaySystemEnvironment causewaySystemEnvironment,
        final CausewayConfiguration causewayConfiguration,
        final MessageService messageService,
        final Provider<SpecificationLoader> specLoaderProvider,
        final List<GridMarshaller> registeredMarshallers,
        final List<LayoutResourceLoader> layoutResourceLoaders) {

        var marshallers = Can.ofCollection(registeredMarshallers);
        var marshallersByMime = new HashMap<CommonMimeType, GridMarshaller>();

        marshallers.forEach(marshaller->
            marshaller.supportedFormats().stream()
                .forEach(mime->
                    marshallersByMime.computeIfAbsent(mime, __->marshaller)));

        return new GridLoadingContext(
            causewaySystemEnvironment,
            causewayConfiguration,
            messageService,
            specLoaderProvider,
            Collections.unmodifiableMap(marshallersByMime),
            Can.ofCollection(layoutResourceLoaders),
            causewaySystemEnvironment.isPrototyping());
    }

    public EnumSet<CommonMimeType> supportedFormats() {
        return EnumSet.copyOf(marshallersByMime().keySet());
    }

    public Optional<GridMarshaller> gridMarshaller(final CommonMimeType format) {
        return Optional.ofNullable(marshallersByMime.get(format));
    }

}
