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

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.grid.GridService.LayoutKey;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.collections._Maps.ConcurrentMapWrapper;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * Cache for {@link BSGrid} instances,.
 *
 * @since 4.0
 */
@AllArgsConstructor
@Getter @Accessors(fluent = true)
@EqualsAndHashCode
@ToString
@Log4j2
final class GridCache {

	private final ConcurrentMapWrapper<LayoutKey, Try<BSGrid>> gridsByKey;

    public GridCache(final GridLoadingContext gridLoadingContext) {
        this(new ConcurrentMapWrapper<>(new ConcurrentHashMap<>()));
    }

    /**
     * To support metamodel invalidation/rebuilding of spec.
     */
    public void remove(final Class<?> domainClass) {
        gridsByKey.map().entrySet().removeIf(entry->entry.getKey().domainClass().equals(domainClass));
    }

    public Try<BSGrid> computeIfAbsent(final LayoutKey layoutKey, final Function<LayoutKey, Try<BSGrid>> factory) {
        return gridsByKey.computeIfAbsent(layoutKey, factory);
    }
    
    public void clear() {
    	gridsByKey.map().clear();
    }

}
